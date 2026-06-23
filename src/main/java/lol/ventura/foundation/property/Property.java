package lol.ventura.foundation.property;

import lombok.Getter;
import lombok.Setter;

public abstract class Property<T> {
    @Getter
    protected final String name;

    @Getter @Setter
    protected T value;

    public boolean isExpanded = true;

    protected Property(final String name, final T value) {
        this.name = name;
        this.value = value;
    }


    public final Class<?> getClassType() {
        return value.getClass();
    }

    public abstract boolean setValueFromString(final String value);

    public abstract String getValueAsString();
}
