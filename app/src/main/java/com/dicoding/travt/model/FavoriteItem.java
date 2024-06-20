package com.dicoding.travt.model;

public class FavoriteItem {
    private String placeId;
    private String photoUrl;
    private String namaTempat;
    private String city;
    private int rating;
    private String description;

    public FavoriteItem(String placeId, String photoUrl, String namaTempat, String city, int rating, String description) {
        this.placeId = placeId;
        this.photoUrl = photoUrl;
        this.namaTempat = namaTempat;
        this.city = city;
        this.rating = rating;
        this.description = description;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getNamaTempat() {
        return namaTempat;
    }

    public void setNamaTempat(String namaTempat) {
        this.namaTempat = namaTempat;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
