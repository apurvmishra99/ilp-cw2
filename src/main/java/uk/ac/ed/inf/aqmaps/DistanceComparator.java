package uk.ac.ed.inf.aqmaps;

import java.awt.geom.Point2D;
import java.util.Comparator;

/**
 * The type Distance comparator.
 */
public class DistanceComparator implements Comparator<Sensor> {

    /**
     * The Ref point.
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
     * Compare int.
     *
     * @param s1 the s 1
     * @param s2 the s 2
     * @return the int
     */
    @Override
    public int compare(Sensor s1, Sensor s2) {
        var distP1 = refPoint.distance(s1.getCoord());
        var distP2 = refPoint.distance(s2.getCoord());
        return Double.compare(distP1, distP2);
    }
}
