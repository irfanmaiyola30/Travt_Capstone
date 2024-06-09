package com.dicoding.travt.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.dicoding.travt.R;
import com.dicoding.travt.model.Favorite;
import com.squareup.picasso.Picasso;

public class DetailFragment extends Fragment {

    private DatabaseReference favoritesRef;
    private ImageView photoImageView, favoriteImageView;
    private TextView namaTempatTextView, cityTextView, ratingTextView, descriptionTextView;
    private boolean isFavorite = false;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);

        favoritesRef = FirebaseDatabase.getInstance().getReference("favorites");
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            // Handle jika pengguna tidak masuk
        }

        photoImageView = view.findViewById(R.id.photo);
        namaTempatTextView = view.findViewById(R.id.nama_tempat);
        cityTextView = view.findViewById(R.id.city);
        ratingTextView = view.findViewById(R.id.ratting);
        descriptionTextView = view.findViewById(R.id.description);
        favoriteImageView = view.findViewById(R.id.favorite);

        Bundle bundle = getArguments();
        if (bundle != null) {
            String name = bundle.getString("name");
            String city = bundle.getString("city");
            double rating = bundle.getDouble("rating");
            String description = bundle.getString("description");
            String photo = bundle.getString("photo");

            namaTempatTextView.setText(name);
            cityTextView.setText(city);
            ratingTextView.setText(String.valueOf(rating));
            descriptionTextView.setText(description);
            Picasso.get().load(photo).into(photoImageView);

            favoriteImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isFavorite = !isFavorite;
                    updateFavoriteStatus(name, city, rating, description, photo);
                }
            });

            checkFavoriteStatus(name);
        }

        return view;
    }

    private void checkFavoriteStatus(String name) {
        favoritesRef.child(userId).orderByChild("name").equalTo(name)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        isFavorite = dataSnapshot.exists(); // Periksa apakah data favorit ada atau tidak
                        updateFavoriteIcon(); // Perbarui ikon favorit berdasarkan status
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle error
                    }
                });
    }

    private void updateFavoriteStatus(String name, String city, double rating, String description, String photo) {
        if (isFavorite) {
            favoritesRef.child(userId).orderByChild("name").equalTo(name)
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
                        }
                    });
        } else {
            String favoriteId = favoritesRef.child(userId).push().getKey();
            Favorite favorite = new Favorite(name, city, rating, description, photo);
            favoritesRef.child(userId).child(favoriteId).setValue(favorite)
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
}
