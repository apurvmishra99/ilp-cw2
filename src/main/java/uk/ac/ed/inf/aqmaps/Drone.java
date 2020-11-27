package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.Point;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Drone {
    private final int seed;
    private final Random RNG;
    private final ArrayList<ArrayList<Point2D>> noFlyZonesArrayList;
    private final Path flightPathLogFile;
    private int movesLeft;
    private ArrayList<Sensor> toVisit;
    private ArrayList<Sensor> visitedSensors;
    private Point2D startingPosition;
    private Point2D currentPosition;
    private int prevMovementAngle;

    public Drone(
            int seed,
            int movesLeft,
            ArrayList<Sensor> toVisit,
            ArrayList<ArrayList<Point2D>> noFlyZonesArrayList,
            Point2D startingPosition,
            Path flightPathLogFile) {
        super();
        this.seed = seed;
        this.RNG = new Random(seed);
        this.movesLeft = movesLeft;
        this.toVisit = toVisit;
        this.visitedSensors = new ArrayList<>();
        this.startingPosition = startingPosition;
        this.currentPosition = startingPosition;
        this.prevMovementAngle = -1;
        this.noFlyZonesArrayList = noFlyZonesArrayList;
        this.flightPathLogFile = flightPathLogFile;
    }

    public ArrayList<Point> collectReadings() {
        GeometryHelpers.setPolygonPointsArr(noFlyZonesArrayList);
        var pointList = new ArrayList<Point>();
        // add starting position
        pointList.add(Point.fromLngLat(this.startingPosition.getX(), this.startingPosition.getY()));
        var nearestSensor = this.findNearestSensor();
        var movementAngle = this.selectMovementAngle(nearestSensor);

        while (this.movesLeft > 0) {
            if (this.toVisit.size() > 0) {
                var nextCoords = this.nextPosition(movementAngle);
                var moveLine = new Line2D.Double(this.currentPosition, nextCoords);

                var intersectAngle = GeometryHelpers.polygonLineIntersects(moveLine);
                var boundsAngle = GeometryHelpers.inPlayArea(nextCoords);

                if (intersectAngle == -1 && boundsAngle) {
                    var prevCoord = this.currentPosition;
                    // move to next position
                    this.setCurrentPosition(nextCoords);
                    // check and do stuff if close to sensor
                    var loc = this.closeToSensor();
                    // set prev movement angle
                    this.setPrevMovementAngle(movementAngle);
                    // add movement to point list
                    pointList.add(Point.fromLngLat(this.currentPosition.getX(), this.currentPosition.getY()));
                    // log the movement
                    logToFile(prevCoord, loc);
                    // update for next move
                    if (toVisit.size() > 0) {
                        nearestSensor = this.findNearestSensor();
                        movementAngle = this.selectMovementAngle(nearestSensor);
                    }
                } else if (intersectAngle != -1) {
                    movementAngle = intersectAngle;
                    continue;
                } else {
                    var randomNum = this.RNG.nextInt(36);
                    movementAngle = randomNum * 10;
                    continue;
                }
            } else {
                if (this.visitedSensors.size() == 34) {
                    this.visitedSensors.remove(33);
                    System.out.println("Done!");
                    break;
                }
                var startDummySensor = new Sensor("null", 0.0, "");
                startDummySensor.setCoord(this.startingPosition);
                this.toVisit.add(startDummySensor);
                movementAngle = this.selectMovementAngle(startDummySensor);
            }
            // decrement the number of moves
            this.movesLeft = this.movesLeft - 1;
        }
        return pointList;
    }

    private Sensor findNearestSensor() {
        return Collections.min(toVisit, new DistanceComparator(this.currentPosition));
    }

    private int selectMovementAngle(Sensor nearestSensor) {
        var nearestSensorCoord = nearestSensor.getCoord();
        var movementAngle =
                GeometryHelpers.findMovementAngle(
                        this.currentPosition.getX(), this.currentPosition.getY(),
                        nearestSensorCoord.getX(), nearestSensorCoord.getY());
        var oppMovementAngle = (movementAngle + 180) % 360;
        if (prevMovementAngle == oppMovementAngle) {
            // If the drone is stuck in an oscillation a random direction is selected
            var randomNum = this.RNG.nextInt(36);
            movementAngle = randomNum * 10;
        }

        return movementAngle;
    }

    private Point2D.Double nextPosition(int movementAngle) {
        var radianAngle = Math.toRadians(movementAngle);
        // It moves by a distance of 0.0003 degrees
        var p =
                new Point2D.Double(
                        this.currentPosition.getX() + 0.0003 * Math.cos(radianAngle),
                        this.currentPosition.getY() + 0.0003 * Math.sin(radianAngle));
//        System.out.println(currentPosition.toString() + " -> " + p.toString());
        return p;
    }

    private String closeToSensor() {
        var sensor = findNearestSensor();
        var dist = this.currentPosition.distance(sensor.getCoord());
        if ((sensor.getLocation().equals("null") && dist < 0.0003) || dist < 0.0002) {
            sensor.setVisited(true);
            toVisit.remove(sensor);
            visitedSensors.add(sensor);
            return sensor.getLocation();
        }
        return "null";
    }

    private void logToFile(Point2D prevCoord, String loc) {
        var logMessage =
                String.format(
                        "%d, %f, %f, %d, %f, %f, %s\n",
                        150 - this.movesLeft + 1,
                        prevCoord.getX(),
                        prevCoord.getY(),
                        this.prevMovementAngle,
                        this.currentPosition.getX(),
                        this.currentPosition.getY(),
                        loc);
        try {
            Files.writeString(this.flightPathLogFile, logMessage, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getMovesLeft() {
        return movesLeft;
    }

    public void setMovesLeft(int movesLeft) {
        this.movesLeft = movesLeft;
    }

    public ArrayList<Sensor> getToVisit() {
        return toVisit;
    }

    public void setToVisit(ArrayList<Sensor> toVisit) {
        this.toVisit = toVisit;
    }

    public Point2D getStartingPosition() {
        return startingPosition;
    }

    public void setStartingPosition(Point2D startingPosition) {
        this.startingPosition = startingPosition;
    }

    public Point2D getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(Point2D currentPosition) {
        this.currentPosition = currentPosition;
    }

    public ArrayList<Sensor> getVisitedSensors() {
        return visitedSensors;
    }

    public void setVisitedSensors(ArrayList<Sensor> visitedSensors) {
        this.visitedSensors = visitedSensors;
    }

    public int getPrevMovementAngle() {
        return prevMovementAngle;
    }

    public void setPrevMovementAngle(int prevMovementAngle) {
        this.prevMovementAngle = prevMovementAngle;
    }

    public int getSeed() {
        return seed;
    }

    public Random getRNG() {
        return RNG;
    }

    public Path getFlightPathLogFile() {
        return flightPathLogFile;
    }
}
