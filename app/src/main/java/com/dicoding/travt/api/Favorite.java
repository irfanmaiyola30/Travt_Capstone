package com.dicoding.travt.api;

public class Favorite {
    private String name;
    private String city;
    private double rating;
    private String description;

    public Favorite() {
        // Default constructor required for calls to DataSnapshot.getValue(Favorite.class)
    }

    public Favorite(String name, String city, double rating, String description) {
        this.name = name;
        this.city = city;
        this.rating = rating;
        this.description = description;
    }

    // Getters and setters...
}
