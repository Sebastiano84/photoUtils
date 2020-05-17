package com.seb.photo.command;

import static java.util.stream.Collectors.groupingBy;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.seb.photo.Patterns;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.javatuples.Pair;


@Parameters(commandNames = "deleteDuplicates")
public class DeleteDuplicates implements PhotoCommand {

  @Parameter
  private String inputDir;

  @Parameter(names = "--outputDir")
  private String outputDir;


  public void run() {
    Collection<File> allImages = findImages(inputDir);

    List<Pair<String, String>> collect = allImages.stream().collect(groupingBy(FileUtils::sizeOf)).entrySet().stream()
        .filter(e -> e.getValue().size() > 1).flatMap(e -> e.getValue().stream())
        .collect(groupingBy(f -> {
          try {
            return FileUtils.checksumCRC32(f);
          } catch (IOException ex) {
            throw new RuntimeException(ex);
          }
        })).entrySet().stream()
        .flatMap(e -> {
          List<File> files = e.getValue();
          if (files.size() > 1) {
            for (int i = 0; i < files.size(); i++) {
              try {
                System.out.printf("%02d - %010d\t%s%n", i, FileUtils.checksumCRC32(files.get(i)), files.get(i));
              } catch (IOException ex) {
                ex.printStackTrace();
              }
            }

            Optional<Integer> oldestFile = findOldestFile(files);
            System.out.printf("Oldest is %s: %n", oldestFile);
            return Stream.of(removeByIndex(files, oldestFile.orElse(-1)));
          } else {
            return Stream.empty();
          }
        }).flatMap(e -> e.stream().map(f -> new Pair<>(Paths.get(outputDir).resolve(f.getName()).toFile().getAbsolutePath(), f.getAbsolutePath())))
        .collect(Collectors.toList());
    collect.forEach(e -> {
      try {
        FileUtils.moveFile(new File(e.getValue1()), new File(e.getValue0()));
      } catch (IOException ex) {
        System.out.println("Error with " + e.getValue1());
      }
    });
  }

  public <T> List<T> removeByIndex(List<T> list, int index) {
    System.out.println("Selected " + index);
    if (index == -1) {
      return Collections.emptyList();
    }
    return Stream.concat(list.subList(0, index).stream(), list.subList(index + 1, list.size()).stream()).collect(Collectors.toList());

  }

  private Optional<Integer> findOldestFile(List<File> value) {
    return value.stream().filter(f -> Patterns.findDate(f).isPresent()).sorted(Comparator.comparing(f -> Patterns.findDate(f).get()))
        .findFirst().map(v -> value.indexOf(v));
  }

}
