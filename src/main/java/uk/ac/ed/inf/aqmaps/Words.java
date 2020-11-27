package uk.ac.ed.inf.aqmaps;

public class Words {
    private String country;
    private Square square;
    private String nearestPlace;
    private Coords coordinates;
    private String words;
    private String language;
    private String map;

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Square getSquare() {
        return square;
    }

    public void setSquare(Square square) {
        this.square = square;
    }

    public String getNearestPlace() {
        return nearestPlace;
    }

    public void setNearestPlace(String nearestPlace) {
        this.nearestPlace = nearestPlace;
    }

    public Coords getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coords coordinates) {
        this.coordinates = coordinates;
    }

    public String getWords() {
        return words;
    }

    public void setWords(String words) {
        this.words = words;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getMap() {
        return map;
    }

    public void setMap(String map) {
        this.map = map;
    }

    @Override
    public String toString() {
        return "Words [country="
                + country
                + ", square="
                + square
                + ", nearestPlace="
                + nearestPlace
                + ", coordinates="
                + coordinates
                + ", words="
                + words
                + ", language="
                + language
                + ", map="
                + map
                + "]";
    }

    public static class Square {
        private Coords southwest;
        private Coords northeast;

        public Coords getSouthwest() {
            return southwest;
        }

        public void setSouthwest(Coords southwest) {
            this.southwest = southwest;
        }

        public Coords getNortheast() {
            return northeast;
        }

        public void setNortheast(Coords northeast) {
            this.northeast = northeast;
        }

        @Override
        public String toString() {
            return "Square [southwest=" + southwest + ", northeast=" + northeast + "]";
        }
    }

    public static class Coords {
        private double lng;
        private double lat;

        public double getLng() {
            return lng;
        }

        public void setLng(double lng) {
            this.lng = lng;
        }

        public double getLat() {
            return lat;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        @Override
        public String toString() {
            return "Coords [lng=" + lng + ", lat=" + lat + "]";
        }
    }
}
