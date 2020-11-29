package uk.ac.ed.inf.aqmaps;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 * The type Geometry helpers.
 */
public class GeometryHelpers {

    /**
     * The constant NORTH_LAT.
     */
    private static final double NORTH_LAT = 55.946233;
    /**
     * The constant SOUTH_LAT.
     */
    private static final double SOUTH_LAT = 55.942617;
    /**
     * The constant EAST_LNG.
     */
    private static final double EAST_LNG = -3.184319;
    /**
     * The constant WEST_LNG.
     */
    private static final double WEST_LNG = -3.192473;
    /**
     * The Polygon points arr.
     */
    private static ArrayList<ArrayList<Point2D>> polygonPointsArr;

    /**
     * Instantiates a new Geometry helpers.
     *
     * @param polygonPointsArr the polygon points arr
     */
    public GeometryHelpers(ArrayList<ArrayList<Point2D>> polygonPointsArr) {
        super();
        GeometryHelpers.polygonPointsArr = polygonPointsArr;
    }

    /**
     * Polygon line intersects int.
     *
     * @param line the line
     * @return the int
     */
    public static int polygonLineIntersects(Line2D.Double line) {

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
     * Find movement angle int.
     *
     * @param X1 the x 1
     * @param Y1 the y 1
     * @param X2 the x 2
     * @param Y2 the y 2
     * @return the int
     */
    public static int findMovementAngle(Double X1, Double Y1, Double X2, Double Y2) {
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
     * In play area boolean.
     *
     * @param targetCoords the target coords
     * @return the boolean
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

    /**
     * Gets north lat.
     *
     * @return the north lat
     */
    public static double getNorthLat() {
        return NORTH_LAT;
    }

    /**
     * Gets south lat.
     *
     * @return the south lat
     */
    public static double getSouthLat() {
        return SOUTH_LAT;
    }

    /**
     * Gets east lng.
     *
     * @return the east lng
     */
    public static double getEastLng() {
        return EAST_LNG;
    }

    /**
     * Gets west lng.
     *
     * @return the west lng
     */
    public static double getWestLng() {
        return WEST_LNG;
    }

}
