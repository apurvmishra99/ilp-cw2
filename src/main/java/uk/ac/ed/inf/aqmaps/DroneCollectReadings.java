package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.Point;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.nio.file.Path;
import java.util.ArrayList;

/**
 * The type Drone collect readings.
 */
public class DroneCollectReadings extends Drone {
    /**
     * The list of list of points of all the no-fly zones on the map.
     */
    private final ArrayList<ArrayList<Point2D>> noFlyZonesArrayList;
    /**
     * The list of visited sensors.
     */
    private ArrayList<Sensor> visitedSensors;
    /**
     * The previous movement angle.
     */
    private int prevMovementAngle;

    /**
     * Instantiates a new Drone.
     *
     * @param seed                the seed
     * @param movesLeft           the moves left, i.e. the max number of moves the drone can make
     * @param toVisit             the list of sensors to visit
     * @param noFlyZonesArrayList the list of list of points defining the no-fly zones on the map
     * @param startingPosition    the starting position
     * @param flightPathLogFile   the path of flightPathLog file
     */
    public DroneCollectReadings(int seed, int movesLeft, ArrayList<Sensor> toVisit, ArrayList<ArrayList<Point2D>> noFlyZonesArrayList, Point2D startingPosition, Path flightPathLogFile) {
        super(seed, movesLeft, toVisit, startingPosition, flightPathLogFile);
        this.noFlyZonesArrayList = noFlyZonesArrayList;
        this.visitedSensors = new ArrayList<>();
        this.prevMovementAngle = -1;
    }

    /**
     * This method implements a greedy search algorithm to collect readings from all sensors,
     * and return to the starting position.
     *
     * @return the array list of type Mapbox.Geojson.Point with points the drone visits (in order)
     */
    public ArrayList<Point> collectReadings() {
        GeometryHelpers.setPolygonPointsArr(this.noFlyZonesArrayList);
        var pointList = new ArrayList<Point>();
        // add starting position
        pointList.add(Point.fromLngLat(this.startingPosition.getX(), this.startingPosition.getY()));

        /* track if the drone is continuously intersecting the no-fly zones */
        var intersectionLoopCount = 0;

        var nearestSensor = this.findNearestSensor();
        var movementAngle = this.selectMovementAngle(nearestSensor);

        while (this.movesLeft > 0) {
            if (this.toVisit.size() > 0) {
                var nextCoords = this.nextPosition(movementAngle);
                var moveLine = new Line2D.Double(this.currentPosition, nextCoords);

                /*
                 if intersectAngle == -1 then the move does not go over a no fly zone
                 else we get the slope of the side we intersect to change our angle to
                */
                var intersectAngle = GeometryHelpers.polygonLineIntersects(moveLine);
                var inPlayArea = GeometryHelpers.inPlayArea(nextCoords);

                if (intersectAngle == -1 && inPlayArea) {
                    var prevCoord = this.currentPosition;
                    // move to next position
                    this.setCurrentPosition(nextCoords);
                    // check and do stuff if close to sensor
                    var loc = this.closeToSensor();
                    // set prev movement angle
                    this.prevMovementAngle = movementAngle;
                    // add movement to point list
                    pointList.add(Point.fromLngLat(this.currentPosition.getX(), this.currentPosition.getY()));
                    // log the movement
                    logToFile(prevCoord, this.prevMovementAngle, loc);
                    // update for next move
                    if (toVisit.size() > 0) {
                        nearestSensor = this.findNearestSensor();
                        movementAngle = this.selectMovementAngle(nearestSensor);
                    }
                } else if (intersectAngle != -1 && inPlayArea) {
                    /*
                    We discard the intersectAngle, if the angle we get is;
                     same as the movement angle,
                     opposite to our previous movement angle,
                     or, we have been in an intersection loop for more than 4 counts.
                    */
                    if (intersectAngle == movementAngle || this.prevMovementAngle == (intersectAngle + 180) % 360 || (++intersectionLoopCount) > 4) {
                        movementAngle = this.randomMovementAngle();
                        intersectionLoopCount = 0;
                    } else {
                        movementAngle = intersectAngle;
                        intersectionLoopCount++;
                    }
                    continue;
                } else {
                    // in case the drone was moving out of the play area we choose a random angle
                    movementAngle = this.randomMovementAngle();
                    continue;
                }
            } else {
                if (this.visitedSensors.size() == 34) {
                    // All sensors visited and the drone is back at the starting position.
                    this.visitedSensors.remove(33);
                    System.out.println("Done!");
                    break;
                }
                /*
                 Add a dummy sensor with starting location as the coordinates to the toVisit sensor list,
                 and update the movement angle of the drone.
                */
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
     * Select the movement angle rounded to nearest 10 between [0, 350].
     *
     * @param nearestSensor the nearest sensor
     * @return the movement angle
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
     * Check is the current position of the drone is close to any sensor.
     * If yes return the w3w location associated to it else "null". This method also checks
     * if we are close enough to our starting position when we go back to it after reading all
     * sensors.
     *
     * @return the w3w string or "null"
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
     * Generates a random movement angle.
     *
     * @return the random angle
     */
    private int randomMovementAngle() {
        return this.RNG.nextInt(36) * 10;
    }

    /**
     * Gets no fly zones array list.
     *
     * @return the no fly zones array list
     */
    public ArrayList<ArrayList<Point2D>> getNoFlyZonesArrayList() {
        return noFlyZonesArrayList;
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
}
