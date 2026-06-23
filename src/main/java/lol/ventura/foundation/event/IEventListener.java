package lol.ventura.foundation.event;

@FunctionalInterface
public interface IEventListener<T extends IEvent> {
    void invoke(final T event);
}