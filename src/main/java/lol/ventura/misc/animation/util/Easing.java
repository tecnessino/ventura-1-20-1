package lol.ventura.misc.animation.util;

@FunctionalInterface
public interface Easing {
    /**
     * Easing's method
     *
     * @param value
     * @return animation formula
     */
    double ease(double value);
}