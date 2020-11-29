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

/**
 * The type Drone.
 */
public class Drone {
    /**
     * The Seed.
     */
    private final int seed;
    /**
     * The Rng.
     */
    private final Random RNG;
    /**
     * The No fly zones array list.
     */
    private final ArrayList<ArrayList<Point2D>> noFlyZonesArrayList;
    /**
     * The Flight path log file.
     */
    private final Path flightPathLogFile;
    /**
     * The Moves left.
     */
    private int movesLeft;
    /**
     * The To visit.
     */
    private ArrayList<Sensor> toVisit;
    /**
     * The Visited sensors.
     */
    private ArrayList<Sensor> visitedSensors;
    /**
     * The Starting position.
     */
    private Point2D startingPosition;
    /**
     * The Current position.
     */
    private Point2D currentPosition;
    /**
     * The Prev movement angle.
     */
    private int prevMovementAngle;

    /**
     * Instantiates a new Drone.
     *
     * @param seed                the seed
     * @param movesLeft           the moves left
     * @param toVisit             the to visit
     * @param noFlyZonesArrayList the no fly zones array list
     * @param startingPosition    the starting position
     * @param flightPathLogFile   the flight path log file
     */
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

    /**
     * Collect readings array list.
     *
     * @return the array list
     */
    public ArrayList<Point> collectReadings() {
        GeometryHelpers.setPolygonPointsArr(noFlyZonesArrayList);
        var pointList = new ArrayList<Point>();
        // add starting position
        pointList.add(Point.fromLngLat(this.startingPosition.getX(), this.startingPosition.getY()));

        // track if the drone is continuously intersecting the no-fly zones
        var intersectionLoopCount = 0;

        var nearestSensor = this.findNearestSensor();
        var movementAngle = this.selectMovementAngle(nearestSensor);

        while (this.movesLeft > 0) {
            if (this.toVisit.size() > 0) {
                var nextCoords = this.nextPosition(movementAngle);
                var moveLine = new Line2D.Double(this.currentPosition, nextCoords);

                // if intersectAngle == -1 then the move does not go over a no fly zone
                // else we get the slope of the side we intersect to change our angle to
                var intersectAngle = GeometryHelpers.polygonLineIntersects(moveLine);
                var inPlayArea = GeometryHelpers.inPlayArea(nextCoords);

                if (intersectAngle == -1 && inPlayArea) {
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
                } else if (intersectAngle != -1 && inPlayArea) {
                    if (intersectAngle == movementAngle || this.prevMovementAngle == (intersectAngle + 180) % 360 || (++intersectionLoopCount) > 4) {
                        movementAngle = this.randomMovementAngle();
                        intersectionLoopCount = 0;
                    } else {
                        movementAngle = intersectAngle;
                        intersectionLoopCount++;
                    }
                    continue;
                } else {
                    movementAngle = this.randomMovementAngle();
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

    /**
     * Find nearest sensor sensor.
     *
     * @return the sensor
     */
    private Sensor findNearestSensor() {
        return Collections.min(toVisit, new DistanceComparator(this.currentPosition));
    }

    /**
     * Select movement angle int.
     *
     * @param nearestSensor the nearest sensor
     * @return the int
     */
    private int selectMovementAngle(Sensor nearestSensor) {
        var nearestSensorCoord = nearestSensor.getCoord();
        var movementAngle =
                GeometryHelpers.findMovementAngle(
                        this.currentPosition.getX(), this.currentPosition.getY(),
                        nearestSensorCoord.getX(), nearestSensorCoord.getY());
        var oppMovementAngle = (movementAngle + 180) % 360;
        if (this.prevMovementAngle == oppMovementAngle) {
            // If the drone is stuck in an oscillation a random direction is selected
            movementAngle = this.randomMovementAngle();
        }

        return movementAngle;
    }

    /**
     * Next position point 2 d . double.
     *
     * @param movementAngle the movement angle
     * @return the point 2 d . double
     */
    private Point2D.Double nextPosition(int movementAngle) {
        var radianAngle = Math.toRadians(movementAngle);
        // It moves by a distance of 0.0003 degrees
        return new Point2D.Double(
                this.currentPosition.getX() + 0.0003 * Math.cos(radianAngle),
                this.currentPosition.getY() + 0.0003 * Math.sin(radianAngle));
    }

    /**
     * Close to sensor string.
     *
     * @return the string
     */
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

    /**
     * Log to file.
     *
     * @param prevCoord the prev coord
     * @param loc       the loc
     */
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

    /**
     * Random movement angle int.
     *
     * @return the int
     */
    private int randomMovementAngle() {
        return this.RNG.nextInt(36) * 10;
    }

    /**
     * Gets moves left.
     *
     * @return the moves left
     */
    public int getMovesLeft() {
        return movesLeft;
    }

    /**
     * Sets moves left.
     *
     * @param movesLeft the moves left
     */
    public void setMovesLeft(int movesLeft) {
        this.movesLeft = movesLeft;
    }

    /**
     * Gets to visit.
     *
     * @return the to visit
     */
    public ArrayList<Sensor> getToVisit() {
        return toVisit;
    }

    /**
     * Sets to visit.
     *
     * @param toVisit the to visit
     */
    public void setToVisit(ArrayList<Sensor> toVisit) {
        this.toVisit = toVisit;
    }

    /**
     * Gets starting position.
     *
     * @return the starting position
     */
    public Point2D getStartingPosition() {
        return startingPosition;
    }

    /**
     * Sets starting position.
     *
     * @param startingPosition the starting position
     */
    public void setStartingPosition(Point2D startingPosition) {
        this.startingPosition = startingPosition;
    }

    /**
     * Gets current position.
     *
     * @return the current position
     */
    public Point2D getCurrentPosition() {
        return currentPosition;
    }

    /**
     * Sets current position.
     *
     * @param currentPosition the current position
     */
    public void setCurrentPosition(Point2D currentPosition) {
        this.currentPosition = currentPosition;
    }

    /**
     * Gets visited sensors.
     *
     * @return the visited sensors
     */
    public ArrayList<Sensor> getVisitedSensors() {
        return visitedSensors;
    }

    /**
     * Sets visited sensors.
     *
     * @param visitedSensors the visited sensors
     */
    public void setVisitedSensors(ArrayList<Sensor> visitedSensors) {
        this.visitedSensors = visitedSensors;
    }

    /**
     * Gets prev movement angle.
     *
     * @return the prev movement angle
     */
    public int getPrevMovementAngle() {
        return prevMovementAngle;
    }

    /**
     * Sets prev movement angle.
     *
     * @param prevMovementAngle the prev movement angle
     */
    public void setPrevMovementAngle(int prevMovementAngle) {
        this.prevMovementAngle = prevMovementAngle;
    }

    /**
     * Gets seed.
     *
     * @return the seed
     */
    public int getSeed() {
        return seed;
    }

    /**
     * Gets rng.
     *
     * @return the rng
     */
    public Random getRNG() {
        return RNG;
    }

    /**
     * Gets flight path log file.
     *
     * @return the flight path log file
     */
    public Path getFlightPathLogFile() {
        return flightPathLogFile;
    }
}
