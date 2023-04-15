// package com.fsck.k9.digitalSignEcnryption.utils;
package utils;

import java.io.IOException;
import java.nio.file.*;

public class File {
  public static String read(String fileName) {
    String content = "";

    try {
      content = new String(Files.readAllBytes(Paths.get(fileName)));
    }
    catch (IOException e) {
      e.printStackTrace();
    }

    return content;
  }

  public static byte[] readBytes(String fileName) {
    byte[] content = null;
    
    try {
      content = Files.readAllBytes(Paths.get(fileName));
    }
    catch (IOException e) {
      e.printStackTrace();
    }

    return content;
  }
}