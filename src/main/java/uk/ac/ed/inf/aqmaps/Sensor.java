package uk.ac.ed.inf.aqmaps;

import java.awt.geom.Point2D;

/**
 * The Sensor class to parse the JSON response from http requests,
 * and to store its coordinates, and visit status.
 */
public class Sensor {
    /**
     * The W3W location name.
     */
    private final String location;
    /**
     * The battery level.
     */
    private final double battery;
    /**
     * The air quality reading.
     */
    private final String reading;
    /**
     * Centre point of sensor which our drone is going to aim for.
     */
    private Point2D coord;
    /**
     * Flag to check if the sensor has been visited
     */
    private boolean visited = false;

    /**
     * Instantiates a new Sensor.
     *
     * @param location the location
     * @param battery  the battery
     * @param reading  the reading
     */
    public Sensor(String location, double battery, String reading) {
        super();
        this.location = location;
        this.battery = battery;
        this.reading = reading;
    }

    /**
     * Gets coord.
     *
     * @return the coord
     */
    public Point2D getCoord() {
        return coord;
    }

    /**
     * Sets coord.
     *
     * @param coord the coord
     */
    public void setCoord(Point2D coord) {
        this.coord = coord;
    }

    /**
     * Is visited boolean.
     *
     * @return the boolean
     */
    public boolean isVisited() {
        return visited;
    }

    /**
     * Sets visited.
     *
     * @param visited the visited
     */
    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    /**
     * Gets location.
     *
     * @return the location
     */
    public String getLocation() {
        return location;
    }

    /**
     * Gets battery.
     *
     * @return the battery
     */
    public double getBattery() {
        return battery;
    }

    /**
     * Gets reading.
     *
     * @return the reading
     */
    public String getReading() {
        return reading;
    }

    /**
     * To string string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return "Sensor [location="
                + location
                + ", battery="
                + battery
                + ", reading="
                + reading
                + ", coord="
                + coord
                + ", visited="
                + visited
                + "]";
    }
}
