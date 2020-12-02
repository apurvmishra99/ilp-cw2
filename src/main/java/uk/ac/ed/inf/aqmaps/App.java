package uk.ac.ed.inf.aqmaps;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The class from where the application is started.
 */
public class App {
    /**
     * <p>This the entry point of application. The program expects 7 arguments to run successfully. These are: </p>
     * <pre>
     *     *   day
     *     *   month
     *     *   year
     *     *   starting latitude
     *     *   starting longitude
     *     *   random state seed
     *     *   port
     * </pre>
     * <p> The arguments should always be provided in the same order<p/>
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        if (args.length < 7) {
            throw new IllegalArgumentException(" We need 7 arguments to proceed.");
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

        var noFlyCoords = requestHandler.getBuidings();
        var sensors = requestHandler.getMaps(year + "/" + month + "/" + day + "/");

        var dateString = day + "-" + month + "-" + year;
        Path logFile = Path.of("flightpath-" + dateString + ".txt");
        try {
            Files.deleteIfExists(logFile);
            Files.createFile(logFile);
        } catch (IOException e) {
            System.err.println("IO exception while creating or deleting the flight path file.");
            System.exit(1);
        }

        var drone = new DroneCollectReadings(randomState, 150, sensors, noFlyCoords, startingPosition, logFile);
        var movementPoints = drone.collectReadings();
        // get visited sensors
        var processedSensors = drone.getVisitedSensors();
        // add univisited sensors to the list
        processedSensors.addAll(drone.getToVisit());

        var geojsonHelper = new GeoJsonHelper();
        geojsonHelper.createGeoJsonMap(processedSensors, movementPoints);
        geojsonHelper.writeToFile("readings-" + dateString + ".geojson");

    }
}

