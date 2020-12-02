package uk.ac.ed.inf.aqmaps;

import java.awt.geom.Point2D;
import java.util.Comparator;

/**
 * The Distance comparator.
 */
public class DistanceComparator implements Comparator<Sensor> {

    /**
     * The Ref point to calculate distance of sensors from.
     */
    private final Point2D refPoint;

    /**
     * Instantiates a new Distance comparator.
     *
     * @param refPoint the ref point
     */
    public DistanceComparator(Point2D refPoint) {
        super();
        this.refPoint = refPoint;
    }

    /**
     * Compare the distance of two sensors from refPoint.
     *
     * @param s1 the first sensor
     * @param s2 the second sensor
     * @return a value in [-1, 0, 1] showing which sensor is further
     */
    @Override
    public int compare(Sensor s1, Sensor s2) {
        var distP1 = refPoint.distance(s1.getCoord());
        var distP2 = refPoint.distance(s2.getCoord());
        return Double.compare(distP1, distP2);
    }
}
