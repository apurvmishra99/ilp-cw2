package uk.ac.ed.inf.aqmaps;

import java.awt.geom.Point2D;
import java.util.Comparator;

public class DistanceComparator implements Comparator<Sensor> {

  private final Point2D refPoint;

  public DistanceComparator(Point2D refPoint) {
    super();
    this.refPoint = refPoint;
  }

  @Override
  public int compare(Sensor s1, Sensor s2) {
    var distP1 = refPoint.distance(s1.getCoord());
    var distP2 = refPoint.distance(s2.getCoord());
    return Double.compare(distP1, distP2);
  }
}
