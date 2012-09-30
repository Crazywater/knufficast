package de.knufficast.util;

/**
 * A callback that can be used to signalize either success or failure.
 * 
 * @author crazywater
 * 
 * @param <A>
 *          parameter to the success function
 * @param <B>
 *          parameter to the failure function
 */
public interface BooleanCallback<A, B> {
  public void success(A a);

  public void fail(B error);
}