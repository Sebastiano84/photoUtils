package com.seb.photo;

import com.beust.jcommander.JCommander;
import com.seb.photo.command.DeleteDuplicates;
import com.seb.photo.command.PhotoCommand;
import com.seb.photo.command.SortByDate;
import com.seb.photo.command.SortByYearAndMonth;

public class Main {

  public static void main(String[] args) {
    PhotoCommand sortByDate = new SortByDate();
    PhotoCommand deleteDuplicates = new DeleteDuplicates();
    PhotoCommand sortByYearAndMonth = new SortByYearAndMonth();
    JCommander jCommander = JCommander.newBuilder()
        .addCommand("sortByDate", sortByDate)
        .addCommand("sortByYearAndMonth", sortByYearAndMonth)
        .addCommand("deleteDuplicates", deleteDuplicates)
        .build();
    jCommander.parse(args);
    jCommander.getCommands().get(jCommander.getParsedCommand()).getObjects().forEach(obj -> {
      if (obj instanceof PhotoCommand) {
        ((PhotoCommand) obj).run();
      }
    });
  }
}
