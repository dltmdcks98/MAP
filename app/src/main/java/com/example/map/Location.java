package com.example.map;

public class Location {
    public int num;
    public String location;
    public Double latitude;
    public Double longitude;

    public Location() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Location(int num, String location, Double latitude, Double longitude) {
        this.num = num;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
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
        return "User{" +
                "num='" + location + '\'' +
                "location='" + location + '\'' +
                "latitude='" + latitude  + '\'' +
                ", longitude='" + longitude + '\'' +
                '}';
    }
}
