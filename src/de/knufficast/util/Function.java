package de.knufficast.util;

/**
 * A simple interface for a function object.
 * 
 * @author crazywater
 * 
 * @param <A>
 *          input type
 * @param <B>
 *          output type
 */
public interface Function<A, B> {
  public B call(A a);
}
