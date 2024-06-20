package com.dicoding.travt;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private String placeId;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Get placeId and userId from Intent
        placeId = getIntent().getStringExtra("placeId");
        userId = getIntent().getStringExtra("userId");

        // Cek apakah placeId dan userId ada
        if (placeId == null || userId == null) {
            Toast.makeText(this, "Place ID or User ID not found", Toast.LENGTH_SHORT).show();
            return; // Berhenti jika salah satu atau kedua nilai tidak ditemukan
        }

        // Initialize fused location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Check for location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        // Enable the location layer on the map
        mMap.setMyLocationEnabled(true);

        // Get current location
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        // Got last known location. In some rare situations this can be null.
                        double currentLatitude = location.getLatitude();
                        double currentLongitude = location.getLongitude();

                        // Add marker for current location
                        LatLng currentLocation = new LatLng(currentLatitude, currentLongitude);
                        mMap.addMarker(new MarkerOptions().position(currentLocation).title("Lokasi Saya"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f));

                        // Fetch coordinates from API based on placeId and userId
                        fetchCoordinatesFromApi(placeId, userId, currentLocation);
                    } else {
                        Toast.makeText(MapsActivity.this, "Location not available", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchCoordinatesFromApi(String placeId, String userId, LatLng currentLocation) {
        // API endpoint to fetch coordinates based on placeId and userId
        String apiUrl = "http://34.101.192.36:3000/destination/" + placeId + "?uid=" + userId;

        // Volley request to fetch data from API
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, apiUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject data = response.getJSONObject("data");
                            JSONObject location = data.getJSONObject("location");
                            String placeName = data.getString("name");
                            double latitude = location.getDouble("_latitude");
                            double longitude = location.getDouble("_longitude");

                            // Add marker for the place
                            LatLng placeLocation = new LatLng(latitude, longitude);
                            mMap.addMarker(new MarkerOptions().position(placeLocation).title(placeName));

                            // Simulate Dijkstra algorithm to get nearest route
                            List<LatLng> shortestPath = dijkstraAlgorithm(currentLocation, placeLocation);

                            // Draw polyline for the shortest path
                            drawPolyline(shortestPath);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MapsActivity.this, "Error parsing JSON response", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MapsActivity.this, "Error fetching data from API", Toast.LENGTH_SHORT).show();
            }
        });

        // Add the request to the RequestQueue
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }

    // Method to simulate Dijkstra algorithm (dummy implementation)
    private List<LatLng> dijkstraAlgorithm(LatLng start, LatLng end) {
        List<LatLng> path = new ArrayList<>();
        // Dummy path for demonstration
        path.add(start);
        path.add(end);
        return path;
    }

    // Method to draw polyline on the map
    private void drawPolyline(List<LatLng> path) {
        PolylineOptions polylineOptions = new PolylineOptions().addAll(path).color(Color.BLUE).width(5);
        mMap.addPolyline(polylineOptions);
    }
}
