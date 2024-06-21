package com.dicoding.travt.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dicoding.travt.R;
import com.dicoding.travt.adapter.FavoriteAdapter;
import com.dicoding.travt.model.FavoriteItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FavoriteFragment extends Fragment implements FavoriteAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private FavoriteAdapter adapter;
    private List<FavoriteItem> favoriteItems;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_favorit, container, false);

        recyclerView = rootView.findViewById(R.id.recycler_favorite);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        favoriteItems = new ArrayList<>();
        adapter = new FavoriteAdapter(getActivity(), favoriteItems);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);

        // Inisialisasi Firebase Authentication
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            fetchFavoriteItems(currentUser.getUid());
        } else {
            // Tangani kasus ketika user tidak terautentikasi
            Toast.makeText(getActivity(), "Pengguna tidak terautentikasi", Toast.LENGTH_SHORT).show();
        }

        return rootView;
    }

    private void fetchFavoriteItems(String userId) {
        String apiUrl = "https://travt-api-backend-7ycttqjnva-et.a.run.app/favorite?uid=" + userId;

        new Thread(() -> {
            try {
                URL url = new URL(apiUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    InputStream in = urlConnection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    parseJson(response.toString());
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                // Tangani kesalahan jaringan
                handleFetchError("Error fetching data: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private void parseJson(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONObject dataObject = jsonObject.getJSONObject("data");

            // Hapus item yang ada
            favoriteItems.clear();

            // Iterasi melalui kunci dataObject (ID tempat)
            Iterator<String> keys = dataObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                JSONObject placeObject = dataObject.getJSONObject(key);

                // Parse field yang relevan dari placeObject
                String name = placeObject.getString("name");
                String city = placeObject.getString("city");
                int rating = placeObject.getInt("rating");
                String description = placeObject.getString("description");
                String photoUrl = placeObject.optString("photo1", ""); // Ganti dengan field foto yang sesuai

                // Buat objek FavoriteItem dan tambahkan ke daftar
                FavoriteItem item = new FavoriteItem(key, photoUrl, name, city, rating, description);
                favoriteItems.add(item);
            }

            // Update UI di thread utama
            updateUIOnMainThread();

        } catch (JSONException e) {
            handleFetchError("Kesalahan parsing JSON: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateUIOnMainThread() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
        }
    }

    private void handleFetchError(String errorMessage) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public void onItemClick(FavoriteItem item) {
        // Periksa validitas placeId sebelum mengarahkan ke DetailFragment
        if (item.getPlaceId() != null && !item.getPlaceId().isEmpty()) {
            openDetailFragment(item, currentUser.getUid());
        } else {
            Toast.makeText(getActivity(), "ID tempat tidak valid", Toast.LENGTH_SHORT).show();
        }
    }

    private void openDetailFragment(FavoriteItem item, String uid) {
        // Pass data to DetailFragment
        Bundle bundle = new Bundle();
        bundle.putString("placeId", item.getPlaceId());
        bundle.putString("name", item.getNamaTempat());
        bundle.putString("city", item.getCity());
        bundle.putString("description", item.getDescription());
        bundle.putDouble("rating", item.getRating());
        bundle.putString("photo", item.getPhotoUrl());
        // Assume totalRating is not available in FavoriteItem class, adjust accordingly

        DetailFragment detailFragment = new DetailFragment();
        detailFragment.setArguments(bundle);

        // Open DetailFragment
        FragmentTransaction transaction = requireFragmentManager().beginTransaction();
        transaction.replace(R.id.container, detailFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    // Inner class for deserializing subtypes field
    class SubtypesDeserializer implements JsonDeserializer<List<String>> {
        @Override
        public List<String> deserialize(JsonElement json, Type typeOfT, com.google.gson.JsonDeserializationContext context) throws JsonParseException {
            List<String> subtypes = new ArrayList<>();
            if (json.isJsonArray()) {
                for (JsonElement element : json.getAsJsonArray()) {
                    subtypes.add(element.getAsString());
                }
            } else if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
                subtypes.add(json.getAsString());
            }
            return subtypes;
        }
    }
}
