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
package de.knufficast.logic;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import de.knufficast.logic.xml.XMLFeed;

/**
 * A thin layer around {@link RssParser} that extracts further information about
 * the feed and feeds them in.
 * 
 * @author crazywater
 * 
 */
public class FeedDownloader {
  public List<XMLFeed> getFeeds(HttpURLConnection connection)
      throws IOException,
      XmlPullParserException {

    long timestamp = connection.getDate();
    String eTag = connection.getHeaderField("ETag");
    RssParser parser = new RssParser();
    parser.parse(connection.getInputStream(), connection.getURL()
        .toString(), timestamp, eTag);
    return parser.getFeeds();
  }
}
