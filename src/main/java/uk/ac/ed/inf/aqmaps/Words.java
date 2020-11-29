package uk.ac.ed.inf.aqmaps;

/**
 * The type Words.
 */
public class Words {
    /**
     * The Country.
     */
    private String country;
    /**
     * The Square.
     */
    private Square square;
    /**
     * The Nearest place.
     */
    private String nearestPlace;
    /**
     * The Coordinates.
     */
    private Coords coordinates;
    /**
     * The Words.
     */
    private String words;
    /**
     * The Language.
     */
    private String language;
    /**
     * The Map.
     */
    private String map;

    /**
     * Gets country.
     *
     * @return the country
     */
    public String getCountry() {
        return country;
    }

    /**
     * Sets country.
     *
     * @param country the country
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * Gets square.
     *
     * @return the square
     */
    public Square getSquare() {
        return square;
    }

    /**
     * Sets square.
     *
     * @param square the square
     */
    public void setSquare(Square square) {
        this.square = square;
    }

    /**
     * Gets nearest place.
     *
     * @return the nearest place
     */
    public String getNearestPlace() {
        return nearestPlace;
    }

    /**
     * Sets nearest place.
     *
     * @param nearestPlace the nearest place
     */
    public void setNearestPlace(String nearestPlace) {
        this.nearestPlace = nearestPlace;
    }

    /**
     * Gets coordinates.
     *
     * @return the coordinates
     */
    public Coords getCoordinates() {
        return coordinates;
    }

    /**
     * Sets coordinates.
     *
     * @param coordinates the coordinates
     */
    public void setCoordinates(Coords coordinates) {
        this.coordinates = coordinates;
    }

    /**
     * Gets words.
     *
     * @return the words
     */
    public String getWords() {
        return words;
    }

    /**
     * Sets words.
     *
     * @param words the words
     */
    public void setWords(String words) {
        this.words = words;
    }

    /**
     * Gets language.
     *
     * @return the language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Sets language.
     *
     * @param language the language
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * Gets map.
     *
     * @return the map
     */
    public String getMap() {
        return map;
    }

    /**
     * Sets map.
     *
     * @param map the map
     */
    public void setMap(String map) {
        this.map = map;
    }

    /**
     * To string string.
     *
     * @return the string
     */
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

    /**
     * The type Square.
     */
    public static class Square {
        /**
         * The Southwest.
         */
        private Coords southwest;
        /**
         * The Northeast.
         */
        private Coords northeast;

        /**
         * Gets southwest.
         *
         * @return the southwest
         */
        public Coords getSouthwest() {
            return southwest;
        }

        /**
         * Sets southwest.
         *
         * @param southwest the southwest
         */
        public void setSouthwest(Coords southwest) {
            this.southwest = southwest;
        }

        /**
         * Gets northeast.
         *
         * @return the northeast
         */
        public Coords getNortheast() {
            return northeast;
        }

        /**
         * Sets northeast.
         *
         * @param northeast the northeast
         */
        public void setNortheast(Coords northeast) {
            this.northeast = northeast;
        }

        /**
         * To string string.
         *
         * @return the string
         */
        @Override
        public String toString() {
            return "Square [southwest=" + southwest + ", northeast=" + northeast + "]";
        }
    }

    /**
     * The type Coords.
     */
    public static class Coords {
        /**
         * The Lng.
         */
        private double lng;
        /**
         * The Lat.
         */
        private double lat;

        /**
         * Gets lng.
         *
         * @return the lng
         */
        public double getLng() {
            return lng;
        }

        /**
         * Sets lng.
         *
         * @param lng the lng
         */
        public void setLng(double lng) {
            this.lng = lng;
        }

        /**
         * Gets lat.
         *
         * @return the lat
         */
        public double getLat() {
            return lat;
        }

        /**
         * Sets lat.
         *
         * @param lat the lat
         */
        public void setLat(double lat) {
            this.lat = lat;
        }

        /**
         * To string string.
         *
         * @return the string
         */
        @Override
        public String toString() {
            return "Coords [lng=" + lng + ", lat=" + lat + "]";
        }
    }
}
