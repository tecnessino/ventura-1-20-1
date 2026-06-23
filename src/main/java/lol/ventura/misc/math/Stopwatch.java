package lol.ventura.misc.math;

public class Stopwatch {
    private long start;

    public Stopwatch() {
        this.reset();
    }

    public boolean elapsed(long period) {
        return (System.currentTimeMillis()-period) > start;
    }

    public boolean elapsedDouble(double period) {
        return (System.currentTimeMillis()-period) > start;
    }

    public void reset() {
        start = System.currentTimeMillis();
    }
}