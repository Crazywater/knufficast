package de.knufficast.util.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class PodcastFileUtil implements FileUtil {
  private final Context context;

  public PodcastFileUtil(Context context) {
    this.context = context;
  }

  @Override
  public FileOutputStream write(String filename, boolean append)
      throws FileNotFoundException {
    throw new RuntimeException("Method not supported");
  }

  @Override
  public File resolveFile(String filename) {
    return new File(
        context.getExternalFilesDir(Environment.DIRECTORY_PODCASTS), filename);
  }

  @Override
  public FileInputStream read(String filename) throws FileNotFoundException {
    File file = resolveFile(filename);
    if (!file.exists()) {
      Log.d("PodcastFileUtil", "read(String '" + filename
          + "') - File doesnt exists[" + file.getAbsolutePath() + "]");
      throw new FileNotFoundException();
    }
    return new FileInputStream(file);
  }

}
