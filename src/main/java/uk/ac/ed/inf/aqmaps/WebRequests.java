package uk.ac.ed.inf.aqmaps;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The Web requests class contains methods to do the required HTTP requests
 * and parse the JSON responses appropriately to Java objects.
 */
public class WebRequests {

    /**
     * The HTTP client.
     */
    private static final HttpClient client = HttpClient.newHttpClient();
    /**
     * The host.
     */
    private final String host;
    /**
     * The port.
     */
    private final int port;

    /**
     * Instantiates a new Web requests.
     */
    public WebRequests() {
        this("http://localhost", 80);
    }

    /**
     * Instantiates a new Web requests.
     *
     * @param host the host
     * @param port the port
     */
    public WebRequests(String host, int port) {
        this.host = host;
        this.port = port;
        checkConnection();
    }

    /**
     * Instantiates a new Web requests.
     *
     * @param host the host
     */
    public WebRequests(String host) {
        this(host, 80);
    }

    /**
     * Instantiates a new Web requests.
     *
     * @param port the port
     */
    public WebRequests(int port) {
        this("http://localhost", port);
    }

    /**
     * Checks if a connection can be established to given host and port.
     */
    private void checkConnection() {
        try {
            var request =
                    HttpRequest.newBuilder().uri(URI.create(this.host + ":" + this.port + "/")).build();
            var response = client.send(request, BodyHandlers.ofString());
        } catch (ConnectException | InterruptedException e) {
            System.err.println(
                    "Unable to connect to " + this.host + " at port " + this.port + ".");
            System.exit(1);
        } catch (IOException e) {
            System.err.println(
                    "IO Exception while connecting to " + this.host + " at port " + this.port + ".");
            System.exit(1);
        }

    }

    /**
     * Get the no-fly zones on the map.
     *
     * @return the list of list of points defining each no-fly zone
     */
    public ArrayList<ArrayList<Point2D>> getBuidings() {
        var request =
                HttpRequest.newBuilder()
                        .uri(URI.create(this.host + ":" + this.port + "/buildings/no-fly-zones.geojson"))
                        .build();
        var geoJsonString = safeGET(request);
        // Creating a list of features
        FeatureCollection fc = FeatureCollection.fromJson(geoJsonString);
        List<Feature> fcList = fc.features();

        // Cast features to Polygon objects to add in noFlyZones
        var noFlyZones = new ArrayList<ArrayList<Point2D>>();

        if (fcList != null) {
            for (int i = 0; i <= (fcList.size() - 1); i++) {

                var g = fcList.get(i).geometry();
                var poly = (Polygon) g;
                // From polygon object get all the coordinates and store them in Point2D list.
                var pts = Objects.requireNonNull(poly).coordinates().get(0);
                var ptsArr = new ArrayList<Point2D>();
                for (Point pt : pts) {
                    var point = new Point2D.Double(pt.longitude(), pt.latitude());
                    ptsArr.add(point);
                }
                noFlyZones.add(ptsArr);
            }
        }

        return noFlyZones;
    }

    /**
     * This method does a get request safely while handling any exceptions..
     *
     * @param request the request object
     * @return the string of the response
     */
    private String safeGET(HttpRequest request) {
        HttpResponse<String> response = null;
        try {
            response = client.send(request, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.err.println("Exception while connecting to " + this.host + " at port " + this.port + ".");
            System.exit(1);
        }

        return response.body();
    }

    /**
     * Gets map for the given date.
     *
     * @param path the w3w location
     * @return the list of {@link Sensor} to visit
     */
    public ArrayList<Sensor> getMaps(String path) {

        // Get air-quality-data JSON file from the server.
        var request =
                HttpRequest.newBuilder()
                        .uri(
                                URI.create(this.host + ":" + this.port + "/maps/" + path + "air-quality-data.json"))
                        .build();
        var geoJsonString = safeGET(request);

        // Deserialise JSON Array to an ArrayList of AirQualityData
        Type targetClassType = new TypeToken<ArrayList<Sensor>>() {
        }.getType();

        ArrayList<Sensor> sensorList = new Gson().fromJson(geoJsonString, targetClassType);

        for (Sensor sensor : sensorList) {
            var wordPath = sensor.getLocation().replace(".", "/");
            var coord = getWords(wordPath, true);
            sensor.setCoord(coord);
        }

        return sensorList;
    }

    /**
     * Gets details of the W3W location word.
     *
     * @param path       the w3w lcoation
     * @param coordsOnly the boolean flag
     * @return the coordinates of the {@link Sensor}
     */
    public Point2D.Double getWords(String path, boolean coordsOnly) {
        if (!coordsOnly) {
            getWords(path);
        }
        // Get the details.json file for given path.
        var request =
                HttpRequest.newBuilder()
                        .uri(URI.create(this.host + ":" + this.port + "/words/" + path + "/details.json"))
                        .build();
        var jsonString = safeGET(request);

        // Deserialising the fetched JSON file.
        var words = new Gson().fromJson(jsonString, Words.class);

        return new Point2D.Double(words.getCoordinates().getLng(), words.getCoordinates().getLat());
    }

    /**
     * Gets details of the W3W location word.
     *
     * @param path the w3w lcoation
     * @return the object of class words with deserialised json details
     */
    public Words getWords(String path) {

        // Get the details.json file for given path.
        var request =
                HttpRequest.newBuilder()
                        .uri(URI.create(this.host + ":" + this.port + "/words/" + path + "/details.json"))
                        .build();
        var jsonString = safeGET(request);

        // Deserialising the fetched JSON file.

        return new Gson().fromJson(jsonString, Words.class);
    }
}
