package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Drone {
  private final int seed;
  private final Random RNG;
  private final ArrayList<ArrayList<Point2D>> noFlyZonesArrayList;
  private final PrintWriter flightPathWriter;
  private int movesLeft;
  private ArrayList<Point2D> moves;
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
      PrintWriter flightPathWriter) {
    super();
    this.seed = seed;
    this.RNG = new Random(seed);
    this.movesLeft = movesLeft;
    this.moves = new ArrayList<>();
    this.toVisit = toVisit;
    this.visitedSensors = new ArrayList<>();
    this.startingPosition = startingPosition;
    this.currentPosition = startingPosition;
    this.prevMovementAngle = -1;
    this.noFlyZonesArrayList = noFlyZonesArrayList;
    this.flightPathWriter = flightPathWriter;
  }

  public LineString collectReadings() {
    GeometryHelpers.setPolygonPointsArr(noFlyZonesArrayList);
    var pointList = new ArrayList<Point>();
    // add starting position
    pointList.add(Point.fromLngLat(this.startingPosition.getX(), this.startingPosition.getY()));
    var nearestSensor = this.findNearestSensor();
    var movementAngle = this.selectMovementAngle(nearestSensor);

    while (this.movesLeft > 0) {
      System.out.println(this.movesLeft);
      if (this.toVisit.size() > 0) {
        System.out.println(visitedSensors.size());
        var nextCoords = this.nextPosition(movementAngle);
        var moveLine = new Line2D.Double(this.currentPosition, nextCoords);

        if (GeometryHelpers.inPlayArea(nextCoords)
            && !GeometryHelpers.polygonLineIntersects(moveLine)) {
          var moveCount = 150 - this.movesLeft + 1;
          var currCoord = this.currentPosition;
          // move to next position
          this.setCurrentPosition(nextCoords);
          // check and do stuff if close to sensor
          var loc = this.closeToSensor();
          // set prev movement angle
          this.setPrevMovementAngle(movementAngle);
          // add movement to point list
          pointList.add(Point.fromLngLat(this.currentPosition.getX(), this.currentPosition.getY()));
          // log the movement
          this.flightPathWriter.printf(
              "%d, %f, %f, %d, %f, %f\n",
              moveCount,
              currCoord.getX(),
              currCoord.getY(),
              movementAngle,
              this.currentPosition.getX(),
              this.currentPosition.getY());
          if (toVisit.size() > 0) {
            nearestSensor = this.findNearestSensor();
            movementAngle = this.selectMovementAngle(nearestSensor);
          }
        } else {
          // Chooses a random direction
          var randomNum = this.RNG.nextInt(36);
          movementAngle = randomNum * 10;
          continue;
        }
      } else {
        if (this.visitedSensors.size() == 34) {
          break;
        }
        System.out.println("Going back to starting postion");
        var startDummySensor = new Sensor("null", 0.0, "");
        startDummySensor.setCoord(this.startingPosition);
        this.toVisit.add(startDummySensor);
        movementAngle = this.selectMovementAngle(startDummySensor);
      }
      // decrement the number of moves
      this.movesLeft = this.movesLeft - 1;
    }
    return LineString.fromLngLats(pointList);
  }

  private boolean closeToStart() {
    var dist = this.currentPosition.distance(this.startingPosition);
    return dist < 0.0003;
  }

  private String closeToSensor() {
    var sensor = findNearestSensor();
    var dist = this.currentPosition.distance(sensor.getCoord());
    if ((sensor.getLocation() == "" && dist < 0.0003) || dist < 0.0002) {
      sensor.setVisited(true);
      toVisit.remove(sensor);
      visitedSensors.add(sensor);
      return sensor.getLocation();
    }
    return "null";
  }

  private Sensor findNearestSensor() {
    return Collections.min(toVisit, new DistanceComparator(this.currentPosition));
  }

  private int selectMovementAngle(Sensor nearestSensor) {
    var nearestSensorCoord = nearestSensor.getCoord();
    var dY = nearestSensorCoord.getY() - this.currentPosition.getY();
    var dX = nearestSensorCoord.getX() - this.currentPosition.getX();
    var radians = Math.atan2(dY, dX);
    if (radians < 0) {
      radians += 2 * Math.PI;
    }
    var degrees = (int) Math.round(radians * 180 / Math.PI);
    var movementAngle = (int) (Math.round(degrees / 10.0) * 10);

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
    return new Point2D.Double(
        this.currentPosition.getX() + 0.0003 * Math.cos(radianAngle),
        this.currentPosition.getY() + 0.0003 * Math.sin(radianAngle));
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

  public ArrayList<Point2D> getMoves() {
    return moves;
  }

  public void setMoves(ArrayList<Point2D> moves) {
    this.moves = moves;
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

  public PrintWriter getFlightPathWriter() {
    return flightPathWriter;
  }
}
