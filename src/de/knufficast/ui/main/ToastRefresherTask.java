package de.knufficast.ui.main;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;
import de.knufficast.R;
import de.knufficast.logic.model.Feed;
import de.knufficast.util.BooleanCallback;
import de.knufficast.watchers.UpdaterService;

/**
 * An {@link AsyncTask} that posts {@link Toast}s for progress information.
 * 
 * @author crazywater
 * 
 */
public class ToastRefresherTask extends AsyncTask<Void, String, Boolean> {
  private static final String SUCCESS = "1";
  private static final String FAILURE = "0";
  private static final int SUCCESS_DURATION = Toast.LENGTH_SHORT;
  private static final int FAILURE_DURATION = Toast.LENGTH_LONG;

  private final Context context;

  public ToastRefresherTask(Context context) {
    this.context = context;
  }

  private BooleanCallback<Feed, Feed> refresherCallback = new BooleanCallback<Feed, Feed>() {
    @Override
    public void success(Feed feed) {
      publishProgress(feed.getTitle(), SUCCESS);
    }

    @Override
    public void fail(Feed feed) {
      publishProgress(feed.getTitle(), FAILURE);
    }
  };

  @Override
  protected Boolean doInBackground(Void... unused) {
    UpdaterService updater = new UpdaterService(refresherCallback);
    return updater.refreshAll();
  }

  @Override
  protected void onProgressUpdate(String... progress) {
    if (progress[1] == SUCCESS) {
      Toast.makeText(context,
          context.getString(R.string.refreshed_feed_success, progress[0]),
          SUCCESS_DURATION).show();
    } else {
      Toast.makeText(context,
          context.getString(R.string.refreshed_feed_error, progress[0]),
          FAILURE_DURATION).show();
    }
  }

  @Override
  protected void onPreExecute() {
    Toast.makeText(context, context.getString(R.string.refreshing_feeds),
        SUCCESS_DURATION).show();
  }

  @Override
  protected void onPostExecute(Boolean result) {
    if (result) {
      Toast.makeText(context,
          context.getString(R.string.refreshed_feeds_success_all),
          SUCCESS_DURATION).show();
    } else {
      Toast.makeText(context,
          context.getString(R.string.refreshed_feeds_error_some),
          FAILURE_DURATION).show();
    }
  }
}
