package lol.ventura.features.properties;

import lol.ventura.foundation.property.Property;

public class BooleanProperty extends Property<Boolean> {
    public BooleanProperty(final String name, final boolean value){
        super(name,value);
    }

    @Override
    public boolean setValueFromString(final String value){
        try {
            this.setValue(Boolean.parseBoolean(value));
            return true;
        } catch (final Exception ex){
            ex.printStackTrace();
            return false;
        }
    }

    public void toggle()
    {
        value = !value;
    }

    @Override
    public String getValueAsString(){
        return this.getValue().toString();
    }
}
