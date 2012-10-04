package de.knufficast.util;

public class TimeUtil {
  /**
   * Zero-pad integers if <10
   */
  private String pad(int toPad) {
    if (toPad < 10) {
      return "0" + toPad;
    } else {
      return "" + toPad;
    }
  }

  public String formatTime(int milliseconds) {
    int hours = milliseconds / (1000 * 60 * 60);
    milliseconds %= 1000 * 60 * 60;
    int minutes = milliseconds / (1000 * 60);
    milliseconds %= 1000 * 60;
    int seconds = milliseconds / 1000;
    if (hours > 0) {
      return hours + ":" + pad(minutes) + ":" + pad(seconds);
    } else {
      return minutes + ":" + pad(seconds);
    }
  }
}
