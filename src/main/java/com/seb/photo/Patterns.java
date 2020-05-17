package com.seb.photo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public enum Patterns {
  P1("Untitled_\\d{4}-\\w{3}-\\d{2}_\\d{2}_\\d{2}_\\d{2} \\(\\d{4}-\\d{2}-\\d{2}T.*", file ->
      Optional.of(DateTimeFormatter.ofPattern("yyyyMMdd").format(DateTimeFormatter.ofPattern("yyyy-MM-dd").parse(file.getName().substring(31, 41)))),
      1),
  P2("IMG(_|-)\\d{8}(_|-).*", f -> {
    return Optional.of(f.getName().substring(4, 12));
  }, 2),

  NOT_WELL_TESTED("\\d{8}_\\d{6}.*.jpg", f -> {
    return Optional.of(f.getName().substring(0, 8));
  }, 3),
  NOT_WELL_TESTED1((file) -> true, f -> List.of(Patterns.getLastModificationDateTime(f).get(), Patterns.getCreationDateTime(f).get()).stream().min(Comparator.comparing(e -> e)), 4);

  private final Function<File, Optional<String>> extractor;
  private final Predicate<File> regexMather;
  private final int order;

  Patterns(Predicate<File> regexMather, Function<File, Optional<String>> extractor, int order) {
    this.regexMather = regexMather;
    this.extractor = extractor;
    this.order = order;
  }

  Patterns(String regex, Function<File, Optional<String>> extractor, int order) {
    this.regexMather = file -> file.getName().matches(regex);
    this.extractor = extractor;
    this.order = order;
  }


  static Optional<String> getCreationDateTime(File file) {
    try {
      LocalDateTime localDateTime = Files.readAttributes(file.toPath(),
          BasicFileAttributes.class).creationTime()
          .toInstant()
          .atZone(ZoneId.systemDefault())
          .toLocalDateTime();
      return Optional.of(String.format("%s%02d%02d", localDateTime.getYear(), localDateTime.getMonthValue(), localDateTime.getDayOfMonth()));
    } catch (IOException e) {
      e.printStackTrace();
      return Optional.empty();
    }
  }

  static Optional<String> getLastModificationDateTime(File file) {
    try {
      LocalDateTime localDateTime = Files.readAttributes(file.toPath(),
          BasicFileAttributes.class).lastModifiedTime()
          .toInstant()
          .atZone(ZoneId.systemDefault())
          .toLocalDateTime();
      return Optional.of(String.format("%s%02d%02d", localDateTime.getYear(), localDateTime.getMonthValue(), localDateTime.getDayOfMonth()));
    } catch (IOException e) {
      e.printStackTrace();
      return Optional.empty();
    }
  }


  public static Optional<String> findDate(File file) {
    return Stream.of(Patterns.values()).sorted(Comparator.comparingInt(o -> o.order)).filter(value -> value.regexMather.test(file)).findFirst()
        .map(value -> value.extractor.apply(file).orElse("date_not_found"));
  }

}
