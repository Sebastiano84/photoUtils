package com.seb.photo.command;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.seb.photo.Patterns;
import java.nio.file.Paths;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;

@Parameters
public class SortByDate implements PhotoCommand {

  @Parameter
  private String inputDir;

  @Parameter(names = "--outputDir")
  private String outputDir;

  public void run() {
    System.out.println(
        findImages(inputDir).stream().filter(f -> Patterns.findDate(f).isPresent()).collect(groupingBy(f -> Patterns.findDate(f).get()))
            .entrySet().stream().flatMap(e -> e.getValue().stream().flatMap(f -> {
          try {
            if (outputDir != null) {
              FileUtils.moveFileToDirectory(f, Paths.get(outputDir).resolve(e.getKey()).toFile(), true);
            }
            return Stream.of(e.getKey() + " " + f.getName());
          } catch (Exception ex) {
            return Stream.empty();
          }
        })).collect(joining("\n")));
  }
}
