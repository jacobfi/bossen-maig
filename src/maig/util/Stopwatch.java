package maig.util;

public class Stopwatch {
    private final long start;
    public Stopwatch() {
        start = System.nanoTime();
    }
    public double elapsed() {
        long now = System.nanoTime();
        return (now - start) / 1000000000.0;
    }

    @Override
    public String toString() {
        return String.format("%.4f", elapsed());
    }
}
