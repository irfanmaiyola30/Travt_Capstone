package com.dicoding.travt.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.dicoding.travt.MapsActivity;
import com.dicoding.travt.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DetailFragment extends Fragment {

    private ImageView photoImageView, favoriteImageView;
    private TextView namaTempatTextView, cityTextView, descriptionTextView, totalReviewTextView, rattingTextView, seeReviewTextView;
    private RatingBar ratingBar;
    private boolean isFavorite = false;
    private String userId;
    private static final String ARG_PLACE_ID = "place_id";
    private String placeId;
    private String reviewsLink;
    private static final String TAG = "FavoriteStatus";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);

        photoImageView = view.findViewById(R.id.photo);
        namaTempatTextView = view.findViewById(R.id.nama_tempat);
        cityTextView = view.findViewById(R.id.city);
        descriptionTextView = view.findViewById(R.id.description);
        favoriteImageView = view.findViewById(R.id.favorite);
        rattingTextView = view.findViewById(R.id.ratting);
        ratingBar = view.findViewById(R.id.ratingBar);
        seeReviewTextView = view.findViewById(R.id.lihat_review);
        totalReviewTextView = view.findViewById(R.id.totalReview);

        favoriteImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFavorite = !isFavorite;
                updateFavoriteStatus();
            }
        });

        seeReviewTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (reviewsLink != null && !reviewsLink.isEmpty()) {
                    // Buka halaman review menggunakan reviewsLink dari API
                    openReviewsLink(reviewsLink);
                } else {
                    Toast.makeText(getContext(), "No reviews available", Toast.LENGTH_SHORT).show();
                }
            }
        });


        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if (fromUser) {
                    sendRatingToServer((int) rating);
                }
            }
        });

        Button mapButton = view.findViewById(R.id.map_button);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    String userId = user.getUid();
                    Intent intent = new Intent(getContext(), MapsActivity.class);
                    intent.putExtra("placeId", placeId);
                    intent.putExtra("userId", userId);
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Bundle bundle = getArguments();
        if (bundle != null) {
            String name = bundle.getString("name");
            String city = bundle.getString("city");
            double ratting = bundle.getDouble("rating");
            double rating = bundle.getDouble("rating");
            String description = bundle.getString("description");
            String photo = bundle.getString("photo");
            double totalRating = bundle.getDouble("totalRating");
            placeId = bundle.getString("placeId", "");

            namaTempatTextView.setText(name != null ? name : "N/A");
            rattingTextView.setText(String.valueOf(ratting));
            cityTextView.setText(city != null ? city : "N/A");
            descriptionTextView.setText(description != null ? description : "N/A");
            ratingBar.setRating((float) rating);
            totalReviewTextView.setText(String.valueOf((int) totalRating));
            if (photo != null && !photo.isEmpty()) {
                Picasso.get().load(photo).into(photoImageView);
            } else {
                photoImageView.setImageResource(R.drawable.logo_travt);
            }
            checkFavoriteStatus();
            fetchRatingFromApi();
        }

        return view;
    }

    private void fetchRatingFromApi() {
        if (placeId == null || placeId.isEmpty()) {
            Toast.makeText(getContext(), "Invalid placeId", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        String url = "http://34.101.192.36:3000/destination/" + placeId + "?uid=" + userId;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject data = response.getJSONObject("data");
                            reviewsLink = data.getString("reviewsLink");
                            double rating = data.getDouble("rating_user");
                            ratingBar.setRating((float) rating);
                            Log.d("RatingDebug", "Rating dari API: " + rating);
                            Log.d("RatingDebug", "Response JSON: " + response.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), "Error parsing rating data", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                handleError(error);
            }
        });

        RequestQueue queue = Volley.newRequestQueue(getContext());
        queue.add(jsonObjectRequest);
    }
    private void openReviewsLink(String reviewsLink) {
        // Pastikan reviewsLink berupa URL yang valid
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(reviewsLink));
        startActivity(intent);
    }


    private void handleError(VolleyError error) {
        String errorMessage = "Error fetching rating data";
        if (error.networkResponse != null && error.networkResponse.data != null) {
            try {
                String errorResponse = new String(error.networkResponse.data);
                JSONObject errorObject = new JSONObject(errorResponse);
                if (errorObject.has("message")) {
                    errorMessage = errorObject.getString("message");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
        Log.e("RatingDebug", "Error fetching rating data: " + error.getMessage());
    }

    private void sendRatingToServer(int rating) {
        if (placeId == null || placeId.isEmpty()) {
            Toast.makeText(getContext(), "Invalid placeId", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        String url = "http://34.101.192.36:3000/destination/" + placeId + "/review/create?uid=" + userId;

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("rating", rating);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error creating JSON object: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(getContext(), "Rating successfully submitted", Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                handleError(error);
            }
        });

        RequestQueue queue = Volley.newRequestQueue(getContext());
        queue.add(jsonObjectRequest);
    }


    private void checkFavoriteStatus() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            new CheckFavoriteTask(placeId, userId).execute();
        } else {
            Toast.makeText(getContext(), "Anda harus login untuk menambahkan ke favorit", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Pengguna tidak login");
        }
    }

    private class CheckFavoriteTask extends AsyncTask<Void, Void, Boolean> {
        private String placeId;
        private String userId;

        public CheckFavoriteTask(String placeId, String userId) {
            this.placeId = placeId;
            this.userId = userId;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                URL url = new URL("http://34.101.192.36:3000/favorite?uid=" + userId);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    reader.close();
                    inputStream.close();

                    // Parse JSON response
                    JSONObject jsonObject = new JSONObject(response.toString());
                    JSONObject dataObject = jsonObject.getJSONObject("data");

                    return dataObject.has(placeId);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean isFavorite) {
            DetailFragment.this.isFavorite = isFavorite;
            favoriteImageView.setImageResource(isFavorite ? R.drawable.favorite_merah : R.drawable.favorite_putih);
        }
    }
    public static DetailFragment newInstance(String placeId) {
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PLACE_ID, placeId);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            placeId = getArguments().getString(ARG_PLACE_ID);
        }
    }


    private void updateFavoriteStatus() {
        String favoriteString = isFavorite ? "true" : "false";
        new UpdateFavoriteTask(placeId, userId, favoriteString).execute();
    }

    private class UpdateFavoriteTask extends AsyncTask<Void, Void, Boolean> {
        private String placeId;
        private String userId;
        private String isFavorite;

        public UpdateFavoriteTask(String placeId, String userId, String isFavorite) {
            this.placeId = placeId;
            this.userId = userId;
            this.isFavorite = isFavorite;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            HttpURLConnection connection = null;
            try {
                String urlString = "http://34.101.192.36:3000/destination/" + placeId + "/favorite?uid=" + userId;
                URL url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();

                // Set request method based on isFavorite value
                if (isFavorite.equals("true")) {
                    connection.setRequestMethod("POST");
                } else if (isFavorite.equals("false")) {
                    connection.setRequestMethod("DELETE");
                }

                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setRequestProperty("Accept", "application/json");

                // Include body with "favorite" as string "true" or "false"
                connection.setDoOutput(true);
                try (OutputStream os = connection.getOutputStream()) {
                    String jsonBody = "{\"favorite\": " + isFavorite + "}";
                    byte[] input = jsonBody.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                    return true;
                } else {
                    // Handle error response
                    InputStream errorStream = connection.getErrorStream();
                    if (errorStream != null) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();
                        Log.e(TAG, "Respon error dari server: " + response.toString());
                        // Handle error message
                        JSONObject errorObject = new JSONObject(response.toString());
                        String errorMessage = errorObject.getString("message");
                        Log.e(TAG, "Pesan error dari server: " + errorMessage);
                        // Show error message
                        showToastMessage(errorMessage);
                    }
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "Exception saat mengupdate status favorit: " + e.getMessage());
                // Show error message
                showToastMessage("Gagal mengupdate status favorit: " + e.getMessage());
                return false;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                if (isFavorite.equals("true")) {
                    showToastMessage("Ditambahkan ke favorit");
                    favoriteImageView.setImageResource(R.drawable.favorite_merah);
                } else {
                    showToastMessage("Dihapus dari favorit");
                    favoriteImageView.setImageResource(R.drawable.favorite_putih);
                }
            } else {
                showToastMessage("Gagal mengupdate status favorit");
                Log.e(TAG, "Gagal mengupdate status favorit");
            }
        }

        private void showToastMessage(String message) {
            // Ensure the toast message is shown on the UI thread
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show());
        }
    }


}


