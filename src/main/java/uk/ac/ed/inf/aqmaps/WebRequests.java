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
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WebRequests {

    private static final HttpClient client = HttpClient.newHttpClient();
    private final String host;
    private final int port;

    public WebRequests() {
        this("http://localhost", 80);
//    System.out.println(
//        "Using default host(http://localhost) and port(80) as no host and port supplied.");
    }

    public WebRequests(String host) {
        this(host, 80);
//    System.out.println("Using default port 80 as no port supplied.");
    }

    public WebRequests(int port) {
        this("http://localhost", port);
//    System.out.println("Using default host(http://localhost) as no host supplied.");
    }

    public WebRequests(String host, int port) {
        this.host = host;
        this.port = port;
        checkConnection();
    }

    private void checkConnection() {
        try {
            var request =
                    HttpRequest.newBuilder().uri(URI.create(this.host + ":" + this.port + "/")).build();
            var response = client.send(request, BodyHandlers.ofString());
            var status = response.statusCode();
//      System.out.println("Got response code: " + status + ", connection to server established.");
        } catch (ConnectException e) {
            System.out.println(
                    "Fatal error: Unable to connect to " + this.host + " at port " + this.port + ".");
            System.exit(1); // Exit the application
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            checkConnection();
        }
    }

    public ArrayList<ArrayList<Point2D>> getBuidings()
            throws IOException, InterruptedException {

        var request =
                HttpRequest.newBuilder()
                        .uri(URI.create(this.host + ":" + this.port + "/buildings/no-fly-zones.geojson"))
                        .build();
        var response = client.send(request, BodyHandlers.ofString());

        var geoJsonString = response.body();

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

    public ArrayList<Sensor> getMaps(String path) throws IOException, InterruptedException {

        // Get air-quality-data JSON file from the server.
        var request =
                HttpRequest.newBuilder()
                        .uri(
                                URI.create(this.host + ":" + this.port + "/maps/" + path + "air-quality-data.json"))
                        .build();
        var response = client.send(request, BodyHandlers.ofString());

        var geoJsonString = response.body();

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

    public Point2D.Double getWords(String path, boolean coordsOnly)
            throws IOException, InterruptedException {

        if (!coordsOnly) {
            getWords(path);
        }
        // Get the details.json file for given path.
        var request =
                HttpRequest.newBuilder()
                        .uri(URI.create(this.host + ":" + this.port + "/words/" + path + "/details.json"))
                        .build();
        var response = client.send(request, BodyHandlers.ofString());

        var jsonString = response.body();

        // Deserialising the fetched JSON file.
        var words = new Gson().fromJson(jsonString, Words.class);

        return new Point2D.Double(words.getCoordinates().getLng(), words.getCoordinates().getLat());
    }

    public Words getWords(String path) throws IOException, InterruptedException {

        // Get the details.json file for given path.
        var request =
                HttpRequest.newBuilder()
                        .uri(URI.create(this.host + ":" + this.port + "/words/" + path + "/details.json"))
                        .build();
        var response = client.send(request, BodyHandlers.ofString());

        var jsonString = response.body();

        // Deserialising the fetched JSON file.

        return new Gson().fromJson(jsonString, Words.class);
    }
}
