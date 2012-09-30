package de.knufficast.util.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * An interface for file readers/writers on Android.
 * 
 * @author crazywater
 * 
 */
public interface FileUtil {
  public FileOutputStream write(String filename, boolean append)
      throws FileNotFoundException;

  public File resolveFile(String filename);

  public FileInputStream read(String filename) throws FileNotFoundException;
}
