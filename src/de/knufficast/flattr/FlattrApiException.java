package de.knufficast.flattr;

public class FlattrApiException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public FlattrApiException(String message) {
    super(message);
  }
}
