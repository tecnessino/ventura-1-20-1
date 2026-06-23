package lol.ventura.features.properties;

import lol.ventura.foundation.property.Property;

import java.awt.*;

public final class ColorProperty extends Property<Color> {

    private int r;
    private int g;
    private int b;
    private int a;

    public ColorProperty(final String label, final Color value) {
        super(label, value);
        this.r = value.getRed();
        this.g = value.getGreen();
        this.b = value.getBlue();
        this.a = value.getAlpha();
    }

    public int r() {
        return r;
    }

    public ColorProperty setR(final int r) {
        this.r = Math.min(255, Math.max(0, r));
        this.assertColor();
        return this;
    }

    public int g() {
        return g;
    }

    public ColorProperty setG(final int g) {
        this.g = this.r = Math.min(255, Math.max(0, g));
        this.assertColor();
        return this;
    }

    public int b() {
        return b;
    }

    public ColorProperty setB(final int b) {
        this.b = this.r = Math.min(255, Math.max(0, b));
        this.assertColor();
        return this;
    }

    public int a() {
        return a;
    }

    public ColorProperty setA(final int a) {
        this.a = this.r = Math.min(255, Math.max(0, a));
        this.assertColor();
        return this;
    }

    public boolean setValueFromString(String value) {
        return false;
    }

    @Override
    public String getValueAsString() {
        return value.toString();
    }

    private void assertColor() {
        this.value = new Color(this.r, this.g, this.b, this.a);
    }
}
