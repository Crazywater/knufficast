/*******************************************************************************
 * Copyright 2012 Crazywater
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
