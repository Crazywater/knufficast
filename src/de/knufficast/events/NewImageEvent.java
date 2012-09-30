package de.knufficast.events;

/**
 * An event that is fired by the @{ImageCache} to notify listeners that a new
 * image has been downloaded.
 * 
 * @author crazywater
 * 
 */
public class NewImageEvent implements Event {
  private String url;

  public NewImageEvent(String url) {
    this.url = url;
  }

  /**
   * The url of the image that has been downloaded.
   */
  public String getUrl() {
    return url;
  }
}
