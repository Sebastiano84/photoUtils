package com.seb.photo.command;

import static java.util.stream.Collectors.groupingBy;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.DateFormatSymbols;
import java.util.Arrays;
import org.apache.commons.io.FileUtils;
import org.javatuples.Pair;


@Parameters(commandNames = "sortByYearAndMonth")
public class SortByYearAndMonth implements PhotoCommand {

  @Parameter
  private String inputDir;

  @Parameter(names = "--outputDir")
  private String outputDir;

  public void run() {
    Arrays.stream(new File(inputDir).listFiles(pathname -> pathname.isDirectory()))
        .collect(groupingBy(
            f -> {
              try {
                return new Pair<>(f.getName().substring(0, 4),
                    String.format("%02d", Integer.parseInt(f.getName().substring(4, 6))) + "_" + getMonth(
                        Integer.parseInt(f.getName().substring(4, 6))));
              } catch (Exception e) {
                System.err.println(f.getName());
                return new Pair<>("failed", "failed");
              }
            })).forEach((t, l) -> {
      l.stream().forEach(f1 -> Arrays.stream(f1.listFiles()).forEach(f -> {
        if (outputDir != null) {
          try {
            FileUtils.moveFileToDirectory(f, Paths.get(outputDir).resolve(t.getValue0()).resolve(t.getValue1()).toFile(), true);
          } catch (IOException e) {
            System.out.println("Already exists " + f + " in " + Paths.get(outputDir).resolve(t.getValue0()).resolve(t.getValue1()).toFile());
            FileUtils.deleteQuietly(f);
          }
        }
      }));
    });
  }

  private static String getMonth(int month) {
    return new DateFormatSymbols().getMonths()[month - 1];
  }


}
