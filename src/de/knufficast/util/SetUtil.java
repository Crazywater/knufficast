package de.knufficast.util;

import java.util.HashSet;

public class SetUtil {
  public static <T> HashSet<T> hash(T... args) {
    HashSet<T> result = new HashSet<T>();
    for (T t : args) {
      result.add(t);
    }
    return result;
  }
}
