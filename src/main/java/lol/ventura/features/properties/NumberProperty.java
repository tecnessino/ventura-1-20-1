package lol.ventura.features.properties;

import lol.ventura.foundation.property.Property;

public class NumberProperty extends Property<Double> {
    public final double min, max, inc;

    public NumberProperty(final String name, final double value, final double min, final double max, final double inc) {
        super(name, value);
        this.min = min;
        this.max = max;
        this.inc = inc;
    }

    @Override
    public void setValue(Double value) {
        if (min > value) value = min;
        if (max < value) value = max;
        super.setValue(value);
    }

    @Override
    public boolean setValueFromString(String value) {
        try {
            this.setValue(Double.valueOf(value));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public String getValueAsString() {
        return String.valueOf(getValue());
    }

    private boolean isNumber(String input) {
        String numbers = "0123456789";
        for (char c : input.toCharArray()) {
            if (c == '.') {
                continue;
            }
            if (!numbers.contains(String.valueOf(c))) {
                return false;
            }
        }
        return true;
    }

    private String truncateDots(String input) {
        if (!input.contains(".")) {
            return input;
        }
        return String.valueOf((int) Math.ceil(Double.parseDouble(input)));
    }
}