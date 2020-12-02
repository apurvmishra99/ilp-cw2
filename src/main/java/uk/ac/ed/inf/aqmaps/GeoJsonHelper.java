package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * The utility class with methods to generate GeoJSON features.
 */
public class GeoJsonHelper {

    /**
     * The map of readings to rgb value.
     */
    private final NavigableMap<Integer, String> RANGE_TO_RGB;
    /**
     * The map of readings to symbol.
     */
    private final NavigableMap<Integer, String> RANGE_TO_SYMBOL;
    /**
     * The GeoJSON representation of the map with all features.
     */
    private FeatureCollection geojsonMap;

    /**
     * Instantiates a new GeoJSON helper.
     */
    public GeoJsonHelper() {
        RANGE_TO_RGB = new TreeMap<>();
        RANGE_TO_RGB.put(0, "#00ff00");
        RANGE_TO_RGB.put(32, "#40ff00");
        RANGE_TO_RGB.put(64, "#80ff00");
        RANGE_TO_RGB.put(96, "#c0ff00");
        RANGE_TO_RGB.put(128, "#ffc000");
        RANGE_TO_RGB.put(160, "#ff8000");
        RANGE_TO_RGB.put(192, "#ff4000");
        RANGE_TO_RGB.put(224, "#ff0000");

        RANGE_TO_SYMBOL = new TreeMap<>();
        RANGE_TO_SYMBOL.put(0, "lighthouse");
        RANGE_TO_SYMBOL.put(128, "danger");
    }

    /**
     * Writes the created GeoJSON map to a given filename.
     *
     * @param fileName the file name
     */
    public void writeToFile(String fileName) {
        if (this.geojsonMap == null) {
            System.err.println("The GeoJSON map has not been created yet.");
            System.exit(1);
        }
        try (FileWriter file = new FileWriter(fileName)) {
            file.write(this.geojsonMap.toJson());
            file.flush();
        } catch (IOException e) {
            System.err.println("There was an error in writing the GeoJSON file to the given filename: " + fileName);
            System.exit(1);
        }
    }

    /**
     * Create a GeoJSON map of type {@link FeatureCollection} and store it in the geojsonMap field.
     *
     * @param sensors       the list of visited and unvisited sensors by the drone
     * @param movePointList the list of points in which drone moved (in order)
     */
    public void createGeoJsonMap(
            ArrayList<Sensor> sensors, ArrayList<Point> movePointList) {
        var mapFeatures = this.generateGeoJsonMarkers(sensors);
        var lineStringFeature = Feature.fromGeometry(LineString.fromLngLats(movePointList));
        mapFeatures.add(lineStringFeature);
        this.geojsonMap = FeatureCollection.fromFeatures(mapFeatures);
    }

    /**
     * Generates a list GeoJSON markers from a list of sensors.
     *
     * @param sensors the list of visited and unvisited sensors by the drone
     * @return the array list of GeoJSON features
     */
    private ArrayList<Feature> generateGeoJsonMarkers(ArrayList<Sensor> sensors) {
        var features = new ArrayList<Feature>();
        for (Sensor sensor : sensors) {
            var location = sensor.getLocation();
            var coord = sensor.getCoord();
            var readingString = sensor.getReading();
            var battery = sensor.getBattery();
            var visited = sensor.isVisited();

            var reading = -1;
            if (!readingString.equals("null") && !readingString.isEmpty()) {
                reading = (int) Math.round(Double.parseDouble(readingString));
            }

            var marker = Point.fromLngLat(coord.getX(), coord.getY());
            var rgbString = getRGBString(reading);
            var markerSymbol = getMarkerSymbol(reading);

            if (battery <= 10.0) {
                rgbString = getRGBString(-1);
                markerSymbol = getMarkerSymbol(-1);
            }

            if (!visited) {
                markerSymbol = "";
                rgbString = "#aaaaaa";
            }

            var feature = Feature.fromGeometry(marker);
            feature.addStringProperty("location", location);
            feature.addStringProperty("rgb-string", rgbString);
            feature.addStringProperty("marker-color", rgbString);
            feature.addStringProperty("marker-symbol", markerSymbol);
            features.add(feature);
        }
        return features;
    }

    /**
     * Gets rgb string for the reading value.
     *
     * @param reading the reading
     * @return the rgb string
     */
    private String getRGBString(int reading) {
        String result;
        if (reading < 0 || reading > 255) {
            result = "#000000";
        } else {
            result = RANGE_TO_RGB.floorEntry(reading).getValue();
        }
        return result;
    }

    /**
     * Gets marker symbol for the reading value.
     *
     * @param reading the reading
     * @return the marker symbol
     */
    private String getMarkerSymbol(int reading) {
        String result;
        if (reading < 0 || reading > 255) {
            result = "cross";
        } else {
            result = RANGE_TO_SYMBOL.floorEntry(reading).getValue();
        }
        return result;
    }
}
