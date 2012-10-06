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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import de.knufficast.logic.model.Episode;
import de.knufficast.logic.model.Feed;

/**
 * A parser for XML podcast feeds. Produces {@link Feed} objects.
 * 
 * @author crazywater
 * 
 */
public class XmlParser {
  private Stack<String> currentTags;
  private List<Feed> feeds;
  private String feedUrl;
  private String encoding;
  private long timestamp;
  private String eTag;

  private Feed feed;
  private Episode episode;

  private static final String FEED_TAG = "channel";
  private static final String ENCLOSURE_TAG = "enclosure";
  private static final String DESCRIPTION_TAG = "description";
  private static final String TITLE_TAG = "title";
  private static final String GUID_TAG = "guid";
  private static final String EPISODE_TAG = "item";
  private static final String LINK_TAG = "link";
  private static final String IMAGE_TAG = "image";
  private static final String LOCATION_ATTRIBUTE = "href";
  private static final String URL_ATTRIBUTE = "url";
  private static final String REL_ATTRIBUTE = "rel";

  /**
   * Parses an XML input. Resulting feeds can be retrieved with
   * {@link #getFeeds}.
   * 
   * @param xml
   *          the input stream of XML data
   * @param feedUrl
   *          the URL to set as feedUrl (used for identification purposes)
   * @param timestamp
   *          the timestamp when the feed was downloaded
   * @param eTag
   *          the eTag header the server delivers. Can be used for caching
   *          purposes, can also be null.
   * @throws XmlPullParserException
   *           for malformed XML
   * @throws IOException
   *           for connection problems
   */
  public void parseFrom(InputStream xml, String feedUrl, long timestamp,
      String eTag)
      throws XmlPullParserException, IOException {
    feeds = new ArrayList<Feed>();
    currentTags = new Stack<String>();
    this.feedUrl = feedUrl;
    this.timestamp = timestamp;
    this.eTag = eTag;

    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
    factory.setNamespaceAware(true);
    XmlPullParser xpp = factory.newPullParser();
    xpp.setInput(xml, null);
    int eventType = xpp.getEventType();
    try {
      encoding = xpp.getInputEncoding();
      while (eventType != XmlPullParser.END_DOCUMENT) {
        if (eventType == XmlPullParser.START_TAG) {
          Map<String, String> attributes = new HashMap<String, String>();
          for (int i = 0; i < xpp.getAttributeCount(); i++) {
            attributes.put(xpp.getAttributeName(i).toLowerCase(),
                xpp.getAttributeValue(i));
          }
          openTag(xpp.getName().toLowerCase(), attributes);
        } else if (eventType == XmlPullParser.END_TAG) {
          String expected = currentTags.peek();
          String got = xpp.getName().toLowerCase();
          if (!expected.equals(got)) {
            throw new XmlPullParserException("Malformed XML: Closing tag "
                + got + ", expected closing " + expected);
          }
          closeTag(got);
        } else if (eventType == XmlPullParser.TEXT) {
          tagText(xpp.getText());
        }
        eventType = xpp.next();
      }
    } catch (NullPointerException e) {
      e.printStackTrace();
      throw new XmlPullParserException("Malformed XML: " + e.getMessage());
    }
  }

  /**
   * Returns the parsed feeds from a previous call to {@link #parseFrom} or null
   * if no call was made.
   */
  public List<Feed> getFeeds() {
    return feeds;
  }

  /**
   * Called upon parsing an opening tag.
   * 
   * @param tag
   *          the tag name
   * @param attributes
   *          the attributes of the tag (name->value) mapping
   */
  private void openTag(String tag, Map<String, String> attributes) {
    currentTags.push(tag);
    if (tag.equals(FEED_TAG)) {
      feed = new Feed();
      feed.setFeedUrl(feedUrl);
      feed.setEncoding(encoding);
      feed.setLastUpdated(timestamp);
      feed.setETag(eTag);
    } else if (tag.equals(EPISODE_TAG)) {
      episode = new Episode();
      episode.setFeedUrl(feedUrl);
    } else if (tag.equals(IMAGE_TAG)) {
      String location = attributes.get(LOCATION_ATTRIBUTE);
      if (location != null) {
        if (FEED_TAG.equals(getParentTag())) {
          feed.setImgUrl(location);
        } else if (EPISODE_TAG.equals(getParentTag())) {
          episode.setImgUrl(location);
        }
      }
    } else if (tag.equals(ENCLOSURE_TAG) && EPISODE_TAG.equals(getParentTag())) {
      episode.setDataUrl(attributes.get(URL_ATTRIBUTE));
    } else if (tag.equals(LINK_TAG) && EPISODE_TAG.equals(getParentTag())) {
      if ("payment".equals(attributes.get(REL_ATTRIBUTE))) {
        String paymentLocation = attributes.get(LOCATION_ATTRIBUTE);
        if (paymentLocation != null && paymentLocation.contains("flattr")) {
          episode.setFlattrUrl(paymentLocation);
        }
      }
    }
  }

  /**
   * Called upon parsing a closing tag
   * 
   * @param tag
   *          the tag name
   */
  private void closeTag(String tag) {
    currentTags.pop();
    if (tag.equals(FEED_TAG)) {
      feeds.add(feed);
      feed = null;
    }
    if (tag.equals(EPISODE_TAG)) {
      feed.addEpisode(episode);
      episode = null;
    }
  }

  /**
   * Called upon parsing text between an opening and a closing tag. Parent tags
   * can be found in {@link #currentTags}, but include the current tag at the
   * top.
   * 
   * @param text
   *          the text that is encountered
   */
  private void tagText(String text) {
    String tag = currentTags.peek();
    if (tag.equals(TITLE_TAG) && EPISODE_TAG.equals(getParentTag())) {
      episode.setTitle(text);
    } else if (tag.equals(GUID_TAG) && EPISODE_TAG.equals(getParentTag())) {
      episode.setGuid(text);
    } else if (tag.equals(TITLE_TAG) && FEED_TAG.equals(getParentTag())) {
      feed.setTitle(text);
    } else if (tag.equals(DESCRIPTION_TAG)
        && EPISODE_TAG.equals(getParentTag())) {
      episode.setDescription(text);
    } else if (tag.equals(DESCRIPTION_TAG) && FEED_TAG.equals(getParentTag())) {
      feed.setDescription(text);
    }
  }

  /**
   * Returns the parent tag of the currently processing tag.
   */
  private String getParentTag() {
    String current = currentTags.pop();
    String parent = currentTags.peek();
    currentTags.push(current);
    return parent;
  }
}
