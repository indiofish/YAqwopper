package com.slowfrog;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class Qwopper {

  /** Tolerance for color comparison. */
  private static final int RGB_TOLERANCE = 3;

  /** Distance between two colors. */
  private static int colorDistance(int rgb1, int rgb2) {
    int dr = Math.abs(((rgb1 & 0xff0000) >> 16) - ((rgb2 & 0xff0000) >> 16));
    int dg = Math.abs(((rgb1 & 0xff00) >> 8) - ((rgb2 & 0xff00) >> 8));
    int db = Math.abs((rgb1 & 0xff) - (rgb2 & 0xff));
    return dr + dg + db;
  }

  /** Checks if a color matches another within a given tolerance. */
  private static boolean colorMatches(int ref, int other) {
    return colorDistance(ref, other) < RGB_TOLERANCE;
  }

  /**
   * Checks if from a given x,y position we can find the pattern what identifies
   * the blue border of the message box.
   */
  private static boolean matchesBlueBorder(BufferedImage img, int x, int y) {
    int refColor = 0x9dbcd0;
    return ((y > 4) && (y < img.getHeight() - 4) && (x < img.getWidth() - 12) &&
            colorMatches(img.getRGB(x, y), refColor) &&
            colorMatches(img.getRGB(x + 4, y), refColor) &&
            colorMatches(img.getRGB(x + 8, y), refColor) &&
            colorMatches(img.getRGB(x + 12, y), refColor) &&
            colorMatches(img.getRGB(x, y + 4), refColor) &&
            !colorMatches(img.getRGB(x, y - 4), refColor) && !colorMatches(
        img.getRGB(x + 4, y + 4), refColor));
  }

  /**
   * From a position that matches the blue border, slide left and top until the
   * corner is found.
   */
  private static int[] slideTopLeft(BufferedImage img, int x, int y) {
    int ax = x;
    int ay = y;

    OUTER_LOOP:

    while (ax >= 0) {
      --ax;
      if (matchesBlueBorder(img, ax, ay)) {
        continue;
      } else {
        ++ax;
        while (ay >= 0) {
          --ay;
          if (matchesBlueBorder(img, ax, ay)) {
            continue;
          } else {
            ++ay;
            break OUTER_LOOP;
          }
        }
      }
    }
    return new int[] { ax, ay };
  }

  /** Look for the origin of the game area on screen. */
  private static int[] findOrigin() throws AWTException {
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    Robot rob = new Robot();
    BufferedImage shot = rob.createScreenCapture(new Rectangle(dim));
    for (int x = 0; x < dim.width; x += 4) {
      for (int y = 0; y < dim.height; y += 4) {
        if (matchesBlueBorder(shot, x, y)) {
          int[] corner = slideTopLeft(shot, x, y);
          return new int[] { corner[0] - 124, corner[1] - 103 };
        }
      }
    }
    throw new RuntimeException(
        "Origin not found. Make sure the game is open and fully visible.");
  }

  public static void main(String[] args) {
    try {
      int[] origin = findOrigin();
      System.out.printf("Origin: %d,%d", origin[0], origin[1]);
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }
}
