package com.dicoding.travt;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dicoding.travt.api.HistoryAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private static final String TAG = "HistoryFragment"; // Tambahkan tag untuk logging

    public HistoryFragment() {
        // Required empty public constructor
    }

    public static HistoryFragment newInstance() {
        return new HistoryFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.history);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            String displayName = currentUser.getEmail();
            Log.d(TAG, "User ID: " + userId + ", Display Name: " + displayName); // Tambahkan log untuk pemeriksaan
            fetchDataFromApi(userId, displayName);
        } else {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchDataFromApi(String userId, String displayName) {
        OkHttpClient client = new OkHttpClient();
        String url = "http://34.101.192.36:3000/review/history?uid=" + userId;

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Failed to fetch data", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonData = response.body().string();
                    List<ReviewItem> reviews = parseJsonData(jsonData, displayName);

                    requireActivity().runOnUiThread(() -> {
                        adapter = new HistoryAdapter(reviews);
                        recyclerView.setAdapter(adapter);
                    });
                } else {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Failed to fetch data", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    private List<ReviewItem> parseJsonData(String jsonString, String displayName) {
        List<ReviewItem> reviews = new ArrayList<>();

        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONObject dataObject = jsonObject.getJSONObject("data");

            Iterator<String> keys = dataObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                JSONObject reviewObject = dataObject.getJSONObject(key);

                String createdAt = reviewObject.getString("createdAt");
                int placeId = reviewObject.getInt("placeId");
                int ratingUser = reviewObject.getInt("rating_user");
                String namaTempat = reviewObject.getString("name");
                String photo = reviewObject.optString("photo", null); // Handle jika photo null
                String reviewerName = reviewObject.optString("reviewerName", "");

                Log.d(TAG, "Original Reviewer Name: " + reviewerName); // Log untuk pemeriksaan

                if (reviewerName.isEmpty()) {
                    reviewerName = displayName != null ? displayName : "Anonymous";
                }

                Log.d(TAG, "Review Item: " + reviewerName); // Tambahkan log untuk pemeriksaan

                reviews.add(new ReviewItem(createdAt, placeId, ratingUser, namaTempat, photo, reviewerName));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            requireActivity().runOnUiThread(() ->
                    Toast.makeText(requireContext(), "Error parsing JSON data", Toast.LENGTH_SHORT).show()
            );
        }

        return reviews;
    }
}
