package de.knufficast.logic;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import de.knufficast.logic.model.Feed;

/**
 * A thin layer around {@link XmlParser} that extracts further information about
 * the feed and feeds them in.
 * 
 * @author crazywater
 * 
 */
public class FeedDownloader {
  public List<Feed> getFeeds(HttpURLConnection connection) throws IOException,
      XmlPullParserException {

    long timestamp = connection.getDate();
    String eTag = connection.getHeaderField("ETag");

    XmlParser parser = new XmlParser();
    parser.parseFrom(connection.getInputStream(), connection.getURL()
        .toString(), timestamp, eTag);
    return parser.getFeeds();
  }
}
