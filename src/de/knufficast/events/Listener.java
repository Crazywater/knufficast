package de.knufficast.events;

/**
 * Interface of an event listener that can be subscribed at the {@link EventBus}
 * .
 * 
 * @author crazywater
 * 
 * @param <T>
 *          the event class to which this listener responds
 */
public interface Listener<T extends Event> {
  public void onEvent(T event);
}
