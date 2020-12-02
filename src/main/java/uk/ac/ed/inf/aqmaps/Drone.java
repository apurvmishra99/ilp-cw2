package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.Point;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * This class is used to make and run our autonomous drone.
 * It holds the movement algorithm and related helper functions.
 */
public abstract class Drone {
    /**
     * The seed value for the random number generator.
     */
    protected final int seed;
    /**
     * The Random Number Generator.
     */
    protected final Random RNG;
    /**
     * The Path value for the flightPathLog.
     */
    protected final Path flightPathLogFile;
    /**
     * The number of moves left.
     */
    protected int movesLeft;
    /**
     * The list of sensors to visit.
     */
    protected ArrayList<Sensor> toVisit;
    /**
     * The starting position co-ordinates.
     */
    protected Point2D startingPosition;
    /**
     * The current position co-ordinates.
     */
    protected Point2D currentPosition;

    /**
     * Initializes the values for the fields of drone class.
     *
     * @param seed              the seed
     * @param movesLeft         the moves left, i.e. the max number of moves the drone can make
     * @param toVisit           the list of sensors to visit
     * @param startingPosition  the starting position
     * @param flightPathLogFile the path of flightPathLog file
     */
    public Drone(
            int seed,
            int movesLeft,
            ArrayList<Sensor> toVisit,
            Point2D startingPosition,
            Path flightPathLogFile) {
        super();
        this.seed = seed;
        this.RNG = new Random(seed);
        this.movesLeft = movesLeft;
        this.toVisit = toVisit;
        this.startingPosition = startingPosition;
        this.currentPosition = startingPosition;
        this.flightPathLogFile = flightPathLogFile;
    }

    /**
     * <p>
     * This method is required to be implemented by the subclasses of the Drone class.
     * The implementation of the movement algorithm decides our movement on the map
     * and how we collect the readings from sensors.
     * </p>
     *
     * @return the array list of type Mapbox.Geojson.Point with points the drone visits (in order)
     */
    public abstract ArrayList<Point> collectReadings();

    /**
     * Find nearest sensor from the current location of the drone.
     *
     * @return the closest sensor
     */
    protected Sensor findNearestSensor() {
        return Collections.min(toVisit, new DistanceComparator(this.currentPosition));
    }

    /**
     * Calculate the next position of the drone based on the movement angle selected.
     *
     * @param movementAngle the movement angle
     * @return the next position co-ordinates as a Point2D object
     */
    protected Point2D nextPosition(int movementAngle) {
        var radianAngle = Math.toRadians(movementAngle);
        // It moves by a distance of 0.0003 degrees
        return new Point2D.Double(
                this.currentPosition.getX() + 0.0003 * Math.cos(radianAngle),
                this.currentPosition.getY() + 0.0003 * Math.sin(radianAngle));
    }

    /**
     * This method logs each of the drone steps to a file.
     *
     * @param prevCoord the prev position of the drone
     * @param moveAngle the angle of the drone movement
     * @param loc       the w3w location or "null"
     */
    protected void logToFile(Point2D prevCoord, int moveAngle, String loc) {
        var logMessage =
                String.format(
                        "%d, %f, %f, %d, %f, %f, %s\n",
                        150 - this.movesLeft + 1,
                        prevCoord.getX(),
                        prevCoord.getY(),
                        moveAngle,
                        this.currentPosition.getX(),
                        this.currentPosition.getY(),
                        loc);
        try {
            Files.writeString(this.flightPathLogFile, logMessage, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Could not write " + logMessage + "to log file.");
        }
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
