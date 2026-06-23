package lol.ventura.foundation.event.subscription;

import lol.ventura.foundation.event.IEvent;
import lol.ventura.foundation.event.IEventListener;

public final class ImmutableSubscription {
    private final Object object;

    private final IEventListener<IEvent> listener;

    public ImmutableSubscription(final Object object, final IEventListener<IEvent> listener) {
        this.object = object;
        this.listener = listener;
    }

    public final Object getObject() {
        return object;
    }

    public final IEventListener<IEvent> getListener() {
        return listener;
    }
}