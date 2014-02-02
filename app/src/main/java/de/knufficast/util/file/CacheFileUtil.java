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
package de.knufficast.util.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import android.content.Context;

/**
 * An implementation of {@link FileUtil} that writes files to external, private
 * storage.
 * 
 * @author crazywater
 * 
 */
public class CacheFileUtil implements FileUtil {
  private Context context;

  public CacheFileUtil(Context context) {
    this.context = context;
  }

  public FileOutputStream write(String filename, boolean append)
      throws FileNotFoundException {
    File file = resolveFile(filename);
    return new FileOutputStream(file, append);
  }

  public File resolveFile(String filename) {
    return new File(context.getCacheDir(), filename);
  }

  public FileInputStream read(String filename) throws FileNotFoundException {
    if (filename == null || "".equals(filename)) {
      throw new FileNotFoundException();
    }
    File file = resolveFile(filename);
    if (!file.exists()) {
      throw new FileNotFoundException();
    }
    return new FileInputStream(file);
  }
}
