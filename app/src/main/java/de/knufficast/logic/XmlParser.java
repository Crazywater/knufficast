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
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public abstract class XmlParser {
  private String encoding;
  private Stack<String> currentTags;

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
  protected void parseFrom(InputStream xml) throws XmlPullParserException,
      IOException {
    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
    factory.setNamespaceAware(true);
    XmlPullParser xpp = factory.newPullParser();
    xpp.setInput(xml, null);
    doParse(xpp);
  }
  
  protected void parseFrom(Reader xml) throws XmlPullParserException,
      IOException {
    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
    factory.setNamespaceAware(true);
    XmlPullParser xpp = factory.newPullParser();
    xpp.setInput(xml);
    doParse(xpp);
  }
  
  private void doParse(XmlPullParser xpp) throws XmlPullParserException,
      IOException {
    currentTags = new Stack<String>();
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
          String tagName = xpp.getName().toLowerCase();
          onOpenTag(tagName);
          openTag(tagName, attributes);
        } else if (eventType == XmlPullParser.END_TAG) {
          String expected = currentTags.peek();
          String got = xpp.getName().toLowerCase();
          if (!expected.equals(got)) {
            throw new XmlPullParserException("Malformed XML: Closing tag "
                + got + ", expected closing " + expected);
          }
          onCloseTag(got);
          closeTag(got);
        } else if (eventType == XmlPullParser.TEXT) {
          onTagText(xpp.getText());
          tagText(xpp.getText());
        }
        eventType = xpp.next();
      }
    } catch (NullPointerException e) {
      e.printStackTrace();
      throw new XmlPullParserException("Malformed XML: " + e.getMessage());
    }
  }

  protected String getEncoding() {
    return encoding;
  }

  protected abstract void openTag(String tag, Map<String, String> attributes);

  protected abstract void closeTag(String tag);

  protected abstract void tagText(String text);

  private void onOpenTag(String tag) {
    currentTags.push(tag);
  }

  private void onCloseTag(String tag) {
    currentTags.pop();
  }

  private void onTagText(String text) {

  }

  protected String getCurrentTag() {
    return currentTags.peek();
  }

  protected String getParentTag() {
    String current = currentTags.pop();
    String parent = currentTags.peek();
    currentTags.push(current);
    return parent;
  }
}
