package lol.ventura.foundation.event;

import lol.ventura.foundation.Service;
import lol.ventura.foundation.event.subscription.ImmutableSubscription;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EventBus extends Service implements IEventBus {
    @Getter @Setter
    private static EventBus instance;

    private final Map<Type, Set<ImmutableSubscription>> subscriptions;

    public EventBus() {
        this.subscriptions = new HashMap<>();
    }

    @Override
    public final void register(final Object object) {
        for (final Field field : object.getClass().getDeclaredFields()) {
            try {
                if (field.getType() == IEventListener.class) {
                    field.setAccessible(true);
                    final Type type = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                    this.subscriptions.computeIfAbsent(type, subscribers -> new HashSet<>()).add(new ImmutableSubscription(object, (IEventListener<IEvent>) field.get(object)));
                }
            } catch (final Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void unregister(final Object object) {
        this.subscriptions.values().forEach(subscribe -> subscribe.removeIf(subscriber -> subscriber.getObject().equals(object)));
    }

    @Override
    public void emit(final IEvent event) {
        if (this.subscriptions.get(event.getClass()) != null) {
            this.subscriptions.get(event.getClass()).forEach(subscriber -> subscriber.getListener().invoke(event));
        }
    }

}
