package de.knufficast.events;

public class FlattrStatusEvent implements Event {
  private int stringResource;

  public FlattrStatusEvent(int stringResource) {
    this.stringResource = stringResource;
  }

  public int getStringResource() {
    return stringResource;
  }
}
