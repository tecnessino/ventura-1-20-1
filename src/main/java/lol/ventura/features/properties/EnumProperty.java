package lol.ventura.features.properties;

import lol.ventura.foundation.property.Property;
import lol.ventura.misc.StringUtil;

public final class EnumProperty<T extends Enum<?>> extends Property<T> {

    private final T[] values;

    public EnumProperty(final String name, final T value) {
        super(name, value);
        this.values = getEnumConstants();
    }

    @Override @SuppressWarnings("unchecked")
    public boolean setValueFromString(final String value) {
        for (Enum<?> enumConstant : this.value.getClass().getEnumConstants()) {
            if (enumConstant.name().equalsIgnoreCase(value)) {
                try {
                    this.setValue((T) enumConstant);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }


    public T[] getValues() {
        return values;
    }

    public void setValue(final int index) {
        setValue(values[index]);
    }

    @SuppressWarnings("unchecked")
    public T[] getEnumConstants() {
        return (T[]) value.getClass().getEnumConstants();
    }

    @Override
    public String getValueAsString() {
        return StringUtil.upperSnakeCaseToPascal(value.toString());
    }
}