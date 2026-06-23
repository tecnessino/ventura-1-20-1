package lol.ventura.features.properties;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import lol.ventura.foundation.property.Property;
import lol.ventura.misc.StringUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MultiProperty<T extends Enum<?>> extends Property<List<T>> {
    @Getter
    private boolean allowMultiselect;

    private final Class<? extends Enum> enumClass;

    @AllArgsConstructor
    public class Entry {
        public String text;
        public T value;


    }

    @SafeVarargs
    public MultiProperty(String name, boolean allowMultiselect, T... values) {
        super(name, allowMultiselect ? new ArrayList<>(List.of(values)) : new ArrayList<>(List.of(List.of(values).get(0))));

        this.allowMultiselect = allowMultiselect;
        this.enumClass = values[0].getClass();
    }


    public void toggleWildcard(Enum<?> value)
    {
        if(allowMultiselect)
        {
            if(this.value.contains(value))
                this.value.remove(value);
            else
                this.value.add((T) value);
        } else {
            this.value.clear();
            this.value.add((T) value);
        }
    }

    public void toggle(T value)
    {
        if(allowMultiselect)
        {
            if(this.value.contains(value))
                this.value.remove(value);
            else
                this.value.add(value);
        } else {
            this.value.clear();
            this.value.add(value);
        }
    }

    public boolean isEnabled(T value)
    {
        return this.value.contains(value);
    }

    public boolean isEnabledWildcard(Enum<?> value)
    {
        return this.value.contains(value);
    }

    public T[] getEnumConstants() {
        return (T[]) enumClass.getEnumConstants();
    }

    public List<String> getEnumConstantsString() {
        return Arrays.stream(enumClass.getEnumConstants()).map(v -> StringUtil.upperSnakeCaseToPascal(v.toString())).toList();
    }

    public List<Entry> getEnumConstantsPaired() {
        return Arrays.stream(enumClass.getEnumConstants()).map(v -> new Entry(StringUtil.upperSnakeCaseToPascal(v.toString()), (T) v)).toList();
    }

    public T getSingleValue() {
        if(this.value.isEmpty())
            return null;

        return this.value.get(0);
    }

    public JsonArray serialize()
    {
        JsonArray array = new JsonArray();
        value.stream().map(v -> v.toString()).forEach(array::add);
        return array;
    }

    public void load(JsonArray array)
    {
        if(array == null)
            return;

        for(JsonElement element : array)
        {
            String name = element.getAsString();
            var xd = Arrays.stream(enumClass.getEnumConstants()).filter((en) -> en.toString().equalsIgnoreCase(name)).findFirst().get();

            if(!isEnabledWildcard(xd))
                toggleWildcard(xd);
        }
    }

    @Override
    public boolean setValueFromString(String value) {
        return false;
    }

    @Override
    public String getValueAsString() {
        return "";
    }
}
