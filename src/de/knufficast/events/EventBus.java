package de.knufficast.events;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.util.Log;

/**
 * Sends events to registered listeners.
 * 
 * @author crazywater
 */
public class EventBus {
  private Map<Class<?>, Set<Listener<?>>> listeners = new HashMap<Class<?>, Set<Listener<?>>>();
  
  /**
   * Listen to an event.
   * 
   * @param eventClass
   *          the class of events to listen to
   * @param listener
   *          the function to invoke upon the event
   */
  public <T extends Event> void addListener(Class<T> eventClass,
      Listener<? super T> listener) {
    if (listeners.get(eventClass) == null) {
      listeners.put(eventClass, new HashSet<Listener<?>>());
    }
    listeners.get(eventClass).add(listener);
  }

  /**
   * Stop listening to an event.
   * 
   * @param eventClass
   *          the class of events to listen to
   * @param listener
   *          the function which shall no longer be invoked upon the event
   */
  public <T extends Event> void removeListener(Class<T> eventClass,
      Listener<? super T> listener) {
    if (listeners.get(eventClass) == null) {
      return;
    }
    listeners.get(eventClass).remove(listener);
  }

  /**
   * Invoke an event and process all listeners on this event.
   * 
   * @param event
   *          the class of the event to fire
   */
  public <T extends Event> void fireEvent(T event) {
    Class<?> eventClass = event.getClass();
    Set<Listener<?>> ourListeners = listeners.get(eventClass);
    if (ourListeners != null) {
      for (Listener<?> listener : ourListeners) {
        try {
          @SuppressWarnings("unchecked")
          Listener<? super T> casted = (Listener<? super T>) listener;
          casted.onEvent(event);
        } catch (ClassCastException e) {
          Log.e("EventBus",
              "Could not dispatch event because of class cast exception");
        }
      }
    }
  }
}
