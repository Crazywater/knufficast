package de.knufficast.util.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import android.content.Context;

/**
 * An implementation of {@link FileUtil} that writes files to internal, private
 * storage.
 * 
 * @author crazywater
 * 
 */
public class InternalFileUtil implements FileUtil {
  private Context context;

  public InternalFileUtil(Context context) {
    this.context = context;
  }

  public FileOutputStream write(String filename, boolean append)
      throws FileNotFoundException {
    int mode = Context.MODE_PRIVATE;
    if (append) {
      mode |= Context.MODE_APPEND;
    }
    return context.openFileOutput(filename, mode);
  }

  public File resolveFile(String filename) {
    return new File(context.getFilesDir(), filename);
  }

  public FileInputStream read(String filename) throws FileNotFoundException {
    return context.openFileInput(filename);
  }
}
