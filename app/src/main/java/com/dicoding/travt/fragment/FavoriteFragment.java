package com.dicoding.travt.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.dicoding.travt.R;
import com.dicoding.travt.adapter.FavoriteAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.dicoding.travt.model.Favorite;

import java.util.ArrayList;
import java.util.List;

public class FavoriteFragment extends Fragment {

    private RecyclerView recyclerView;
    private FavoriteAdapter adapter;
    private DatabaseReference favoritesRef;
    private String userId; // Variabel untuk menyimpan ID pengguna yang sesuai

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorit, container, false);

        recyclerView = view.findViewById(R.id.recycler_favorite);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new FavoriteAdapter();
        recyclerView.setAdapter(adapter);

        // Mendapatkan ID pengguna dari preferensi atau tempat penyimpanan lainnya
        userId = getUserId();

        favoritesRef = FirebaseDatabase.getInstance().getReference("favorites");

        loadFavorites();

        return view;
    }

    private void loadFavorites() {
        // Memuat data favorit berdasarkan ID pengguna
        favoritesRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Favorite> favoriteList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Favorite favorite = snapshot.getValue(Favorite.class);
                    if (favorite != null) {
                        favoriteList.add(favorite);
                    }
                }
                adapter.setFavoriteList(favoriteList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    // Metode untuk mendapatkan ID pengguna (misalnya dari preferensi)
    private String getUserId() {
        // Contoh implementasi pengambilan ID pengguna dari preferensi
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return preferences.getString("userId", ""); // Ganti dengan metode pengambilan ID pengguna yang sesuai
    }
}
