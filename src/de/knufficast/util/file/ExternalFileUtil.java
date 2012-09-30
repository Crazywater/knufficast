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
public class ExternalFileUtil implements FileUtil {
  private Context context;

  public ExternalFileUtil(Context context) {
    this.context = context;
  }

  public FileOutputStream write(String filename, boolean append)
      throws FileNotFoundException {
    File file = resolveFile(filename);
    return new FileOutputStream(file, append);
  }

  public File resolveFile(String filename) {
    return new File(context.getExternalFilesDir(null), filename);
  }

  public FileInputStream read(String filename) throws FileNotFoundException {
    File file = resolveFile(filename);
    if (!file.exists()) {
      throw new FileNotFoundException();
    }
    return new FileInputStream(file);
  }
}
