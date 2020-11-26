package uk.ac.ed.inf.aqmaps;

import java.awt.geom.Point2D;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class App {
  public static void main(String[] args) {
    if (args.length < 7) {
      throw new IllegalArgumentException(" We need 6 arguments to proceed. ");
    }

    var day = args[0];
    var month = args[1];
    var year = args[2];

    var startLatitude = Double.parseDouble(args[3]);
    var startLongitude = Double.parseDouble(args[4]);
    var startingPosition = new Point2D.Double(startLongitude, startLatitude);

    var randomState = Integer.parseInt(args[5]);
    var port = Integer.parseInt(args[6]);

    var requestHandler = new WebRequests(port);

    try {
      var noFlyCoords = requestHandler.getBuidings();
      var sensors = requestHandler.getMaps(year + "/" + month + "/" + day + "/");

      var logFileName = "flightpath-" + day + "-" + month + "-" + year + ".txt";
      FileWriter logFileWriter = new FileWriter(logFileName);
      PrintWriter flightPathLogger = new PrintWriter(logFileWriter);

      var drone = new Drone(randomState, 150, sensors, noFlyCoords, startingPosition, flightPathLogger);
      drone.getFlightPathWriter().close();
      var movementPoints = drone.collectReadings();

      var geojsonHelper = new GeoJsonHelper();
      var featureCollection = geojsonHelper.createGeoJsonMap(drone.getVisitedSensors(), movementPoints);
      System.out.println(featureCollection.toJson());

    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }
}
