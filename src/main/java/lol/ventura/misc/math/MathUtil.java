package lol.ventura.misc.math;

import lombok.experimental.UtilityClass;

import java.util.concurrent.ThreadLocalRandom;

@UtilityClass
public class MathUtil {
    public float random(float min, float max) {
        return (float) (Math.random() * (max - min) + min);
    }

    public double ekkoree(double min, double max) {
        if (min == max) {
            return min;
        } else if (min > max) {
            final double d = min;
            min = max;
            max = d;
        }

        return ThreadLocalRandom.current().nextDouble(min, max);
    }
}
