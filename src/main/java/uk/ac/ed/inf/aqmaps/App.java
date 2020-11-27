package uk.ac.ed.inf.aqmaps;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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

            var dateString = day + "-" + month + "-" + year;
            Path logFile = Path.of("flightpath-" + dateString + ".txt");
            Files.deleteIfExists(logFile);
            Files.createFile(logFile);

            var drone = new Drone(randomState, 150, sensors, noFlyCoords, startingPosition, logFile);
            var movementPoints = drone.collectReadings();
            var processedSensors = drone.getVisitedSensors();
            processedSensors.addAll(drone.getToVisit());

            var geojsonHelper = new GeoJsonHelper();
            geojsonHelper.createGeoJsonMap(processedSensors, movementPoints);
            geojsonHelper.writeToFile("readings-" + dateString + ".geojson");

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
