package utility;

import java.util.Comparator;

public class ApproximateComparator implements Comparator<Double> {
    private double precision;

    public ApproximateComparator(double precision) {
        this.precision = precision;
    }

    public int compare(Double d1, Double d2) {
        return Math.abs(d1 - d2) <= precision ? 0 : Double.compare(d1, d2);
    }
}
