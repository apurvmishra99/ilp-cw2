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

public class GeoJsonHelper {

    private final NavigableMap<Integer, String> RANGE_TO_RGB;
    private final NavigableMap<Integer, String> RANGE_TO_SYMBOL;
    private FeatureCollection geojsonMap;

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

    public void writeToFile(String fileName) {
        try (FileWriter file = new FileWriter(fileName)) {
            file.write(this.geojsonMap.toJson());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createGeoJsonMap(
            ArrayList<Sensor> sensors, ArrayList<Point> movePointList) {
        var mapFeatures = this.generateGeoJsonMarkers(sensors);
        var lineStringFeature = Feature.fromGeometry(LineString.fromLngLats(movePointList));
        mapFeatures.add(lineStringFeature);
        this.geojsonMap = FeatureCollection.fromFeatures(mapFeatures);
    }

    private ArrayList<Feature> generateGeoJsonMarkers(ArrayList<Sensor> sensors) {
        var features = new ArrayList<Feature>();
        for (Sensor sensor : sensors) {
            var location = sensor.getLocation();
            var coord = sensor.getCoord();
            var readingString = sensor.getReading();
            var battery = sensor.getBattery();
            var visited = sensor.isVisited();

            var reading = -1;
            if (!readingString.equals("null")) {
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

    private String getRGBString(int num) {
        String result;
        if (num < 0 || num > 255) {
            result = "#000000";
        } else {
            result = RANGE_TO_RGB.floorEntry(num).getValue();
        }
        return result;
    }

    private String getMarkerSymbol(int num) {
        String result;
        if (num < 0 || num > 255) {
            result = "cross";
        } else {
            result = RANGE_TO_SYMBOL.floorEntry(num).getValue();
        }
        return result;
    }
}
