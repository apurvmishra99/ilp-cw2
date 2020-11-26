package uk.ac.ed.inf.aqmaps;

import java.awt.geom.Point2D;

public class Sensor {
  private final String location;
  private final double battery;
  private final String reading;
  // Centre point of sensor which our drone is going to aim for.
  private Point2D coord;
  // Flag to check if the sensor has been visited
  private boolean visited = false;

  public Sensor(String location, double battery, String reading) {
    super();
    this.location = location;
    this.battery = battery;
    this.reading = reading;
  }

  public Point2D getCoord() {
    return coord;
  }

  public void setCoord(Point2D coord) {
    this.coord = coord;
  }

  public boolean isVisited() {
    return visited;
  }

  public void setVisited(boolean visited) {
    this.visited = visited;
  }

  public String getLocation() {
    return location;
  }

  public double getBattery() {
    return battery;
  }

  public String getReading() {
    return reading;
  }

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
