package com.example.map;

public class Location {
    public String location;
    public Double latitude;
    public Double longitude;

    public Location() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Location(String location, Double latitude, Double longitude) {
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() { return longitude; }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "map{" +
                "location='" + location + '\'' +
                "latitude='" + latitude  + '\'' +
                ", longitude='" + longitude + '\'' +
                '}';
    }
}
