package com.dicoding.travt.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.dicoding.travt.R;
import com.dicoding.travt.model.Favorite;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

public class DetailFragment extends Fragment {

    private DatabaseReference favoritesRef;
    private ImageView photoImageView, favoriteImageView;
    private TextView namaTempatTextView, cityTextView, ratingTextView, descriptionTextView, TotalReview;
    private RatingBar ratingBar;
    private boolean isFavorite = false;
    private LinearLayout linearLayoutMap;
    private String userId;
    private String placeId; // Menyimpan place ID dari RecyclerView item

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);

        photoImageView = view.findViewById(R.id.photo);
        namaTempatTextView = view.findViewById(R.id.nama_tempat);
        cityTextView = view.findViewById(R.id.city);
        ratingTextView = view.findViewById(R.id.ratting);
        descriptionTextView = view.findViewById(R.id.description);
        favoriteImageView = view.findViewById(R.id.favorite);
        ratingBar = view.findViewById(R.id.ratingBar);
        linearLayoutMap = view.findViewById(R.id.map_button);
        TotalReview = view.findViewById(R.id.totalReview);

        favoriteImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFavorite = !isFavorite;
                updateFavoriteStatus();
            }
        });

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if (fromUser) {
                    sendRatingToServer((int) rating);
                    ratingTextView.setText(String.valueOf(rating));

                }
            }
        });

        // Mendapatkan data dari Bundle
        Bundle bundle = getArguments();
        if (bundle != null) {
            String name = bundle.getString("name");
            String city = bundle.getString("city");
            double rating = bundle.getDouble("rating");
            String description = bundle.getString("description");
            String photo = bundle.getString("photo");
            double totalRating = bundle.getDouble("totalRating");
            placeId = bundle.getString("placeId", "");

            namaTempatTextView.setText(name != null ? name : "N/A");
            cityTextView.setText(city != null ? city : "N/A");
            ratingTextView.setText(String.valueOf(rating));
            descriptionTextView.setText(description != null ? description : "N/A");
            TotalReview.setText(String.valueOf((int) totalRating));
            if (photo != null && !photo.isEmpty()) {
                Picasso.get().load(photo).into(photoImageView);
            } else {
                photoImageView.setImageResource(R.drawable.logo_travt); // Default image if photo URL is empty
            }
            // Set nilai rating awal di RatingBar dari server
            fetchRatingFromServer(userId);

            // Periksa status favorit menggunakan nama tempat
            checkFavoriteStatus(name);
        }

        return view;
    }
    private void fetchRatingFromServer(String userId) {
        // Buat URL untuk mendapatkan data rating dari server berdasarkan userId
        String url = "http://example.com/api/rating?userId=" + userId;

        // Buat request JSON untuk mendapatkan data rating
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Ambil rating dari response JSON
                            double rating = response.getDouble("rating");

                            // Set nilai rating di RatingBar dan TextView
                            ratingBar.setRating((float) rating);
                            ratingTextView.setText(String.valueOf(rating));
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), "Error parsing JSON: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getContext(), "Error fetching rating: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Tambahkan request ke queue
        RequestQueue queue = Volley.newRequestQueue(getContext());
        queue.add(jsonObjectRequest);
    }


    private void checkFavoriteStatus(String name) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            favoritesRef = FirebaseDatabase.getInstance().getReference("favorites").child(userId);
            favoritesRef.orderByChild("name").equalTo(name)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            isFavorite = dataSnapshot.exists(); // Periksa apakah data favorit ada atau tidak
                            updateFavoriteIcon(); // Perbarui ikon favorit berdasarkan status
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle error
                            Toast.makeText(getContext(), "Gagal memeriksa status favorit", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // Handle jika pengguna tidak masuk
            Toast.makeText(getContext(), "Anda harus masuk untuk menambahkan favorit", Toast.LENGTH_SHORT).show();
            // Mungkin tambahkan redirect ke login atau sembunyikan tombol favorite
        }
    }

    private void updateFavoriteStatus() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // Handle jika pengguna tidak masuk
            Toast.makeText(getContext(), "Anda harus masuk untuk menambahkan favorit", Toast.LENGTH_SHORT).show();
            return;
        }

        userId = currentUser.getUid(); // Pastikan userId terinisialisasi

        String name = namaTempatTextView.getText().toString();
        String city = cityTextView.getText().toString();
        double rating = Double.parseDouble(ratingTextView.getText().toString());
        String description = descriptionTextView.getText().toString();
        // Ambil gambar dari ImageView jika perlu
        // String photo = ...;

        if (isFavorite) {
            favoritesRef.orderByChild("name").equalTo(name)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                snapshot.getRef().removeValue()
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(getContext(), "Data berhasil dihapus", Toast.LENGTH_SHORT).show();
                                                isFavorite = false;
                                                updateFavoriteIcon();
                                            } else {
                                                Toast.makeText(getContext(), "Gagal menghapus data", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle error
                            Toast.makeText(getContext(), "Gagal menghapus data", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            String favoriteId = favoritesRef.push().getKey();
            Favorite favorite = new Favorite(name, city, rating, description);
            favoritesRef.child(favoriteId).setValue(favorite)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Data berhasil disimpan", Toast.LENGTH_SHORT).show();
                            isFavorite = true;
                            updateFavoriteIcon();
                        } else {
                            Toast.makeText(getContext(), "Gagal menyimpan data", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void updateFavoriteIcon() {
        if (isFavorite) {
            favoriteImageView.setImageResource(R.drawable.favorite_merah);
        } else {
            favoriteImageView.setImageResource(R.drawable.favorite_putih);
        }
    }

    private void sendRatingToServer(int rating) {
        // Check for empty placeId or userId before proceeding
        if (placeId == null || userId == null || placeId.isEmpty() || userId.isEmpty()) {
            Toast.makeText(getContext(), "placeId atau userId kosong. Harap periksa datanya.", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://34.101.192.36:3000/destination/" + placeId + "/review/create";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("rating", rating); // Sending rating as integer
            jsonBody.put("uid", userId);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error creating JSON object: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        // Create JsonObjectRequest
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(getContext(), "Rating berhasil dikirim", Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String message = "Gagal mengirim rating";
                if (error.networkResponse != null && error.networkResponse.data != null) {
                    String errorResponse = new String(error.networkResponse.data);
                    try {
                        JSONObject errorObj = new JSONObject(errorResponse);
                        if (errorObj.has("message")) {
                            message = "Gagal mengirim rating: " + errorObj.getString("message");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }

        });

        // Add request to RequestQueue
        RequestQueue queue = Volley.newRequestQueue(getContext());
        queue.add(jsonObjectRequest);
    }

}
