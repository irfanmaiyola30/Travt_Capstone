package com.dicoding.travt.model;

public class Favorite {
    private String name;
    private String city;
    private double rating;
    private String description;
    private String photo;

    // Default constructor required for calls to DataSnapshot.getValue(Favorite.class)
    public Favorite(String name, String city, double rating, String description) {
    }

    // Constructor with parameters
    public Favorite(String name, String city, double rating, String description, String photo) {
        this.name = name;
        this.city = city;
        this.rating = rating;
        this.description = description;
        this.photo = photo;
    }

    // Getter and Setter methods
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }
}
