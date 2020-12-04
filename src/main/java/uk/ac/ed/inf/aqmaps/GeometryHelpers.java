package uk.ac.ed.inf.aqmaps;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 * The utility class with helpers to deal with shapes and boundaries on the map.
 */
public class GeometryHelpers {

    /**
     * The constant radius of drones movement.
     */
    public static final double MOVEMENT_RADIUS = 0.0003;

    /**
     * The constant distance from which a reading can be taken.
     */
    public static final double READING_DISTANCE = 0.0002;

    /**
     * The constant number of total number of sensors on the map.
     */
    public static final int NUM_OF_SENSORS = 33;

    /**
     * The constant NORTH_LAT.
     */
    public static final double NORTH_LAT = 55.946233;
    /**
     * The constant SOUTH_LAT.
     */
    public static final double SOUTH_LAT = 55.942617;
    /**
     * The constant EAST_LNG.
     */
    public static final double EAST_LNG = -3.184319;
    /**
     * The constant WEST_LNG.
     */
    public static final double WEST_LNG = -3.192473;
    /**
     * The list of lists of points defining the no-fly zones.
     */
    private static ArrayList<ArrayList<Point2D>> polygonPointsArr;

    /**
     * Instantiates a new Geometry helpers.
     *
     * @param polygonPointsArr the list of lists of points defining the no-fly zones.
     */
    public GeometryHelpers(ArrayList<ArrayList<Point2D>> polygonPointsArr) {
        super();
        GeometryHelpers.polygonPointsArr = polygonPointsArr;
    }

    /**
     * This method checks if a line intersects with any of the no-fly zones.
     * If it doesn't we return -1, else we find the slope of the line we intersected
     * and return it.
     *
     * @param line the line
     * @return the angle of new movement or -1
     */
    public static int polygonLineIntersects(Line2D line) {

        for (ArrayList<Point2D> polygonPoints : polygonPointsArr) {
            for (int i = 0; i < polygonPoints.size() - 1; i++) {
                var polygonSide = new Line2D.Double(
                        polygonPoints.get(i).getX(), polygonPoints.get(i).getY(),
                        polygonPoints.get(i + 1).getX(), polygonPoints.get(i + 1).getY());
                if (polygonSide.intersectsLine(line)) {
                    return findMovementAngle(
                            polygonSide.getX1(), polygonSide.getY1(),
                            polygonSide.getX2(), polygonSide.getY2());
                }
            }
            var polygonSide = new Line2D.Double(
                    polygonPoints.get(polygonPoints.size() - 1).getX(),
                    polygonPoints.get(polygonPoints.size() - 1).getY(),
                    polygonPoints.get(0).getX(), polygonPoints.get(0).getY());
            if (polygonSide.intersectsLine(line)) {
                return findMovementAngle(
                        polygonSide.getX1(), polygonSide.getY1(),
                        polygonSide.getX2(), polygonSide.getY2());
            }
        }
        return -1;
    }

    /**
     * Find a movement angle (in degrees )from point A to B.
     * The returned angle is rounded off to its nearest 10.
     *
     * @param X1 the x coordinate of point A
     * @param Y1 the y coordinate of point A
     * @param X2 the x coordinate of point B
     * @param Y2 the y coordinate of point B
     * @return the angle between them rounded to the nearest 10
     */
    public static int findMovementAngle(double X1, double Y1, double X2, double Y2) {
        var dX = X2 - X1;
        var dY = Y2 - Y1;
        var radians = Math.atan2(dY, dX);
        if (radians < 0) {
            radians += 2 * Math.PI;
        }
        var degrees = (int) Math.round(radians * 180 / Math.PI);
        return (int) (Math.round(degrees / 10.0) * 10) % 360;
    }

    /**
     * This method checks if a point is in the play area.
     *
     * @param targetCoords the point to check
     * @return true if inside else false
     */
    public static boolean inPlayArea(Point2D targetCoords) {
        return (targetCoords.getY() > SOUTH_LAT
                && targetCoords.getY() < NORTH_LAT
                && targetCoords.getX() < EAST_LNG
                && targetCoords.getX() > WEST_LNG);
    }

    /**
     * Gets polygon points arr.
     *
     * @return the polygon points arr
     */
    public static ArrayList<ArrayList<Point2D>> getPolygonPointsArr() {
        return polygonPointsArr;
    }

    /**
     * Sets polygon points arr.
     *
     * @param polygonPointsArr the polygon points arr
     */
    public static void setPolygonPointsArr(ArrayList<ArrayList<Point2D>> polygonPointsArr) {
        GeometryHelpers.polygonPointsArr = polygonPointsArr;
    }

}
