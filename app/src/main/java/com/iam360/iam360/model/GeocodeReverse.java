package com.iam360.iam360.model;

/*
{
        "place_id": "ChIJi8MeVwPKlzMRH8FpEHXV0Wk",
        "name": "Manila",
        "vicinity": "Manila",
        "distance": 6300.860875815268
    }
 */

public class GeocodeReverse {

    private String place_id = "";
    private String name = "";
    private String vicinity = "";
    private String distance = "";

    public String getPlace_id() {
        return place_id;
    }

    public void setPlace_id(String place_id) {
        this.place_id = place_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVicinity() {
        return vicinity;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }
}
