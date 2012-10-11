package de.knufficast.logic.model;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xmlpull.v1.XmlPullParserException;

import de.knufficast.logic.XmlParser;
import de.knufficast.util.SetUtil;

/**
 * A class to post-process feeds after downloading them. For now only used to
 * whitelist the HTML tags used in the description.
 * 
 * @author crazywater
 * 
 */
public class FeedPostProcessor extends XmlParser {
  private XMLEpisode episode;
  private StringBuilder stringBuilder;
  
  private Set<String> whitelist = SetUtil.hash("p", "a", "li", "ol", "ul");

  public void process(List<XMLFeed> feeds) {
    for (XMLFeed feed : feeds) {
      for (XMLEpisode episode : feed.getEpisodes()) {
        this.episode = episode;
        processEpisode();
      }
    }
  }

  private void processEpisode() {
    try {
      String content = episode.getContent();
      if (!"".equals(content)) {
        stringBuilder = new StringBuilder();
        parseFrom(new StringReader(content));
        episode.setContent(stringBuilder.toString());
      }
    } catch (XmlPullParserException e) {
      episode.setContent("");
    } catch (IOException e) {
      episode.setContent("");
    }
  }

  @Override
  protected void openTag(String tag, Map<String, String> attributes) {
    if (whitelist.contains(tag)) {
      stringBuilder.append("<" + tag);
      for (Map.Entry<String, String> entry : attributes.entrySet()) {
        stringBuilder.append(" " + entry.getKey() + "=" + "'"
            + entry.getValue() + "'");
      }
      stringBuilder.append(">");
    } else if ("".equals(episode.getImgUrl()) && "img".equals(tag)) {
      if (attributes.containsKey("src")) {
        episode.setImgUrl(attributes.get("src"));
      }
    }
  }

  @Override
  protected void closeTag(String tag) {
    if (whitelist.contains(tag)) {
      stringBuilder.append("</");
      stringBuilder.append(tag);
      stringBuilder.append(">");
    }
  }

  @Override
  protected void tagText(String text) {
    String tag = getCurrentTag();
    if (whitelist.contains(tag)) {
      stringBuilder.append(text);
    }
  }
}
