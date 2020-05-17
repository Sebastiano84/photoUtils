package com.seb.photo.command;

import java.io.File;
import java.util.Collection;
import org.apache.commons.io.FileUtils;

public interface PhotoCommand {

  default Collection<File> findImages(String inputDir) {
    Collection<File> files = FileUtils.listFiles(new File(inputDir), new String[]{"JPG", "jpg", "jpeg", "png", "PNG"}, true);
    System.out.printf("Found %s images%n", files.size());
    return files;
  }

  void run();
}
