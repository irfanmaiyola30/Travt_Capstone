package com.dicoding.travt.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.List;

public class ApiCaller {
    private static final String API_URL = "http://34.101.192.36:3000";

    public interface ApiCallback {
        void onSuccess(ApiResponse apiResponse);
        void onFailure(IOException e);
    }

    public static void fetchApiData(ApiCallback callback) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(API_URL)
                .build();

        new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                Gson gson = new GsonBuilder()
                        .registerTypeAdapter(new TypeToken<List<String>>(){}.getType(), new SubtypesDeserializer())
                        .create();

                ApiResponse apiResponse = gson.fromJson(response.body().string(), ApiResponse.class);
                callback.onSuccess(apiResponse);
            } catch (IOException e) {
                callback.onFailure(e);
            }
        }).start();
    }

    public static class ApiResponse {
        @SerializedName("status")
        String status;
        @SerializedName("data")
        public List<Data> dataList;
    }

    public static class Data {
        @SerializedName("reviews_per_score_1")
        int reviewsPerScore1;
        @SerializedName("reviews_per_score_2")
        int reviewsPerScore2;
        @SerializedName("city")
        public String city;
        @SerializedName("other_hours")
        String otherHours;
        @SerializedName("rating")
        public double rating;
        @SerializedName("about")
        About about;
        @SerializedName("description")
        public String description;
        @SerializedName("full_address")
        String fullAddress;
        @SerializedName("subtypes")
        List<String> subtypes;
        @SerializedName("reviews")
        int reviews;
        @SerializedName("reviews_link")
        String reviewsLink;
        @SerializedName("place_id")
        String placeId;
        @SerializedName("photos_count")
        int photosCount;
        @SerializedName("location_link")
        String locationLink;
        @SerializedName("business_status")
        String businessStatus;
        @SerializedName("verified")
        boolean verified;
        @SerializedName("photo1")
        public String photo;
        @SerializedName("site")
        String site;
        @SerializedName("phone")
        public String phone;
        @SerializedName("working_hours")
        public WorkingHours workingHours;
        @SerializedName("name")
        public String name;
        @SerializedName("location")
        Location location;
        @SerializedName("category")
        public String category;
        @SerializedName("reviews_per_score_5")
        int reviewsPerScore5;
        @SerializedName("reviews_per_score_3")
        int reviewsPerScore3;
        @SerializedName("reviews_per_score_4")
        int reviewsPerScore4;
    }

    public static class About {
        @SerializedName("Accessibility")
        Accessibility accessibility;
        @SerializedName("Atmosphere")
        Atmosphere atmosphere;
        @SerializedName("Crowd")
        Crowd crowd;
        @SerializedName("Payments")
        Payments payments;
        @SerializedName("Dining options")
        DiningOptions diningOptions;
        @SerializedName("Children")
        Children children;
        @SerializedName("Service options")
        ServiceOptions serviceOptions;
        @SerializedName("Offerings")
        Offerings offerings;
        @SerializedName("Amenities")
        Amenities amenities;
    }

    public static class Accessibility {
        @SerializedName("Wheelchair-accessible seating")
        boolean wheelchairAccessibleSeating;
        @SerializedName("Wheelchair-accessible car park")
        boolean wheelchairAccessibleCarPark;
    }

    public static class Atmosphere {
        @SerializedName("Cosy")
        boolean cosy;
        @SerializedName("Casual")
        boolean casual;
    }

    public static class Crowd {
        @SerializedName("Groups")
        boolean groups;
    }

    public static class Payments {
        @SerializedName("Cash only")
        boolean cashOnly;
    }

    public static class DiningOptions {
        @SerializedName("Breakfast")
        boolean breakfast;
        @SerializedName("Brunch")
        boolean brunch;
        @SerializedName("Dinner")
        boolean dinner;
        @SerializedName("Dessert")
        boolean dessert;
        @SerializedName("Lunch")
        boolean lunch;
    }

    public static class Children {
        @SerializedName("Good for kids")
        boolean goodForKids;
        @SerializedName("Kids' menu")
        boolean kidsMenu;
    }

    public static class ServiceOptions {
        @SerializedName("Dine-in")
        boolean dineIn;
        @SerializedName("Takeaway")
        boolean takeaway;
    }

    public static class Offerings {
        @SerializedName("Coffee")
        boolean coffee;
        @SerializedName("Halal food")
        boolean halalFood;
    }

    public static class Amenities {
        @SerializedName("Toilets")
        boolean toilets;
    }

    public static class WorkingHours {
        @SerializedName("Monday")
        public String monday;
        @SerializedName("Tuesday")
        public String tuesday;
        @SerializedName("Wednesday")
        public String wednesday;
        @SerializedName("Thursday")
        public String thursday;
        @SerializedName("Friday")
        public String friday;
        @SerializedName("Saturday")
        public String saturday;
        @SerializedName("Sunday")
        public String sunday;
    }

    public static class Location {
        @SerializedName("_latitude")
        double latitude;
        @SerializedName("_longitude")
        double longitude;
    }
}
