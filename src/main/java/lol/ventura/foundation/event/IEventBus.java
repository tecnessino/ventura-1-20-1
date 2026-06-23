package lol.ventura.foundation.event;

public interface IEventBus {
    void register(final Object object);

    void unregister(final Object object);

    void emit(final IEvent event);

    static IEventBus standard() {
        return new EventBus();
    }
}
