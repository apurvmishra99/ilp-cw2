package uk.ac.ed.inf.aqmaps;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

public class GeometryHelpers {

    private static final double NORTH_LAT = 55.946233;
    private static final double SOUTH_LAT = 55.942617;
    private static final double EAST_LNG = -3.184319;
    private static final double WEST_LNG = -3.192473;
    private static ArrayList<ArrayList<Point2D>> polygonPointsArr;

    public GeometryHelpers(ArrayList<ArrayList<Point2D>> polygonPointsArr) {
        super();
        GeometryHelpers.polygonPointsArr = polygonPointsArr;
    }

    public static int polygonLineIntersects(Line2D.Double line) {

        for (ArrayList<Point2D> polygonPoints : polygonPointsArr) {
            for (int i = 0; i < polygonPoints.size() - 1; i++) {
                var polygonSide = new Line2D.Double(
                        polygonPoints.get(i).getX(), polygonPoints.get(i).getY(),
                        polygonPoints.get(i + 1).getX(), polygonPoints.get(i + 1).getY());
                if (line.intersectsLine(polygonSide)) {
//                    System.out.println(polygonSide.getP1().toString() + ", " + polygonSide.getP2().toString());
                    var movementAngle = findMovementAngle(
                            polygonSide.getX1(), polygonSide.getY1(),
                            polygonSide.getX2(), polygonSide.getY2());
                    return movementAngle;
                }
            }
            var polygonSide = new Line2D.Double(
                    polygonPoints.get(polygonPoints.size() - 1).getX(),
                    polygonPoints.get(polygonPoints.size() - 1).getY(),
                    polygonPoints.get(0).getX(), polygonPoints.get(0).getY());
            if (line.intersectsLine(polygonSide)) {
                var movementAngle = findMovementAngle(
                        polygonSide.getX1(), polygonSide.getY1(),
                        polygonSide.getX2(), polygonSide.getY2());
                return movementAngle;
            }
        }
        return -1;
    }

    public static int findMovementAngle(Double X1, Double Y1, Double X2, Double Y2) {
        var dX = X2 - X1;
        var dY = Y2 - Y1;
        var radians = Math.atan2(dY, dX);
        if (radians < 0) {
            radians += 2 * Math.PI;
        }
        var degrees = (int) Math.round(radians * 180 / Math.PI);
        var movementAngle = (int) (Math.round(degrees / 10.0) * 10);
        return movementAngle;
    }

    public static boolean inPlayArea(Point2D targetCoords) {
        return (targetCoords.getY() > SOUTH_LAT
                && targetCoords.getY() < NORTH_LAT
                && targetCoords.getX() < EAST_LNG
                && targetCoords.getX() > WEST_LNG);
    }

    public static ArrayList<ArrayList<Point2D>> getPolygonPointsArr() {
        return polygonPointsArr;
    }

    public static void setPolygonPointsArr(ArrayList<ArrayList<Point2D>> polygonPointsArr) {
        GeometryHelpers.polygonPointsArr = polygonPointsArr;
    }

    public static double getNorthLat() {
        return NORTH_LAT;
    }

    public static double getSouthLat() {
        return SOUTH_LAT;
    }

    public static double getEastLng() {
        return EAST_LNG;
    }

    public static double getWestLng() {
        return WEST_LNG;
    }

}
