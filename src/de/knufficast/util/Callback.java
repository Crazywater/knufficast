package de.knufficast.util;

/**
 * A simple callback interface.
 * 
 * @author crazywater
 * 
 * @param <A>
 *          Parameter to the callback
 */
public interface Callback<A> {
  public void call(A a);
}
