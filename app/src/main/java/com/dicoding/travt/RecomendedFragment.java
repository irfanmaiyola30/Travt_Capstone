package com.dicoding.travt;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dicoding.travt.R;
import com.dicoding.travt.api.ApiCaller;
import com.dicoding.travt.api.DataAdapter;
import com.dicoding.travt.api.HorizontalDataAdapter;
import com.dicoding.travt.fragment.DetailFragment;
import com.dicoding.travt.fragment.LihatSemuaFragment;
import com.google.common.reflect.TypeToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RecomendedFragment extends Fragment {

    private TextView locationTextView;
    private Geocoder geocoder;
    private RecyclerView verticalRecyclerView;
    private RecyclerView horizontalRecyclerView; // RecyclerView untuk menampilkan data horizontal
    private DataAdapter verticalDataAdapter; // Adapter untuk RecyclerView vertical
    private HorizontalDataAdapter horizontalDataAdapter; // Adapter untuk RecyclerView horizontal
    private OkHttpClient client;
    private Gson gson;
    private TextView seeAllTextView;
    private TextView allTextView;
    private TextView PopulerTextView;
    private TextView RecomemendedTextView;

    public RecomendedFragment() {
        // Diperlukan konstruktor kosong
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate layout untuk fragmen ini
        View rootView = inflater.inflate(R.layout.fragment_recomended, container, false);

        // Inisialisasi tampilan
        locationTextView = rootView.findViewById(R.id.location);
        PopulerTextView =rootView.findViewById(R.id.tab_populer);
        RecomemendedTextView = rootView.findViewById(R.id.tab_recomended);
        seeAllTextView = rootView.findViewById(R.id.see_all);
        allTextView = rootView.findViewById(R.id.tab_all);
        verticalRecyclerView = rootView.findViewById(R.id.vertical_recycler_view);
        horizontalRecyclerView = rootView.findViewById(R.id.horizontal_recycler_view); // Inisialisasi RecyclerView horizontal
        verticalRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        horizontalRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)); // Mengatur layout horizontal

        // Atur click listener untuk see_all TextView
        seeAllTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Tambahkan aksi yang diinginkan ketika see_all TextView diklik
                // Misalnya, buka aktivitas atau tampilkan fragmen lain
                // Contoh: Buka fragment baru
                FragmentTransaction transaction = requireFragmentManager().beginTransaction();
                transaction.replace(R.id.container, new LihatSemuaFragment());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        PopulerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Tambahkan aksi yang diinginkan ketika see_all TextView diklik
                // Misalnya, buka aktivitas atau tampilkan fragmen lain
                // Contoh: Buka fragment baru
                FragmentTransaction transaction = requireFragmentManager().beginTransaction();
                transaction.replace(R.id.container, new PopulerFragment());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        RecomemendedTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Tambahkan aksi yang diinginkan ketika see_all TextView diklik
                // Misalnya, buka aktivitas atau tampilkan fragmen lain
                // Contoh: Buka fragment baru
                FragmentTransaction transaction = requireFragmentManager().beginTransaction();
                transaction.replace(R.id.container, new RecomendedFragment());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        allTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Tambahkan aksi yang diinginkan ketika see_all TextView diklik
                // Misalnya, buka aktivitas atau tampilkan fragmen lain
                // Contoh: Buka fragment baru
                FragmentTransaction transaction = requireFragmentManager().beginTransaction();
                transaction.replace(R.id.container, new AllFragment());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        // Inisialisasi Geocoder
        geocoder = new Geocoder(requireContext(), Locale.getDefault());

        // Memeriksa izin lokasi
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Mendapatkan manajer lokasi
            LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
            // Mendapatkan lokasi terakhir yang diketahui
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            // Menampilkan lokasi terakhir jika tersedia
            if (lastKnownLocation != null) {
                displayLocation(lastKnownLocation);
            }
            // Memperbarui lokasi secara periodik
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
        } else {
            // Meminta izin jika lokasi belum di setujui
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        // Inisialisasi OkHttpClient dan Gson dengan SubtypesDeserializer
        client = new OkHttpClient();
        gson = new GsonBuilder()
                .registerTypeAdapter(new TypeToken<List<String>>(){}.getType(), new SubtypesDeserializer())
                .create();

        // Panggil fungsi untuk mengambil data
        fetchData();

        return rootView;
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            // Memperbarui tampilan saat lokasi berubah
            displayLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {}
    };

    private void displayLocation(Location location) {
        try {
            // Mendapatkan nama tempat dari lokasi
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                String placeName = addresses.get(0).getLocality(); // Mendapatkan nama tempat dari alamat
                locationTextView.setText(placeName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Izin lokasi disetujui, menginisialisasi ulang tampilan
                requireActivity().recreate();
            } else {
                // Izin lokasi ditolak, memberi tahu pengguna
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Membebaskan sumber daya jika tampilan dihancurkan
        LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeUpdates(locationListener);
    }

    private void fetchData() {
        Request request =
                new Request.Builder()
                        .url("http://34.101.192.36:3000/destination") // Ubah URL sesuai dengan URL API Anda
                        .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                // Tangani kesalahan saat permintaan gagal
                e.printStackTrace();
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonResponse = response.body().string();
                    ApiCaller.ApiResponse apiResponse = gson.fromJson(jsonResponse, ApiCaller.ApiResponse.class);

                    new Handler(Looper.getMainLooper()).post(() -> {
                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                        if (currentUser != null) {
                            String uid = currentUser.getUid();

                            // Set vertical and horizontal adapters with data
                            verticalDataAdapter = new DataAdapter(getContext(), apiResponse.dataList);
                            verticalRecyclerView.setAdapter(verticalDataAdapter);
                            verticalDataAdapter.setOnItemClickListener(item -> openDetailFragment(item, uid));

                            horizontalDataAdapter = new HorizontalDataAdapter(getContext(), apiResponse.dataList);
                            horizontalRecyclerView.setAdapter(horizontalDataAdapter);
                            horizontalDataAdapter.setOnItemClickListener(item -> openDetailFragment(item, uid));
                        } else {
                            Log.d("onResponse", "User not logged in");
                        }
                    });
                }
            }

            private void openDetailFragment(ApiCaller.Data item, String uid) {
                // Pass data to DetailFragment
                Bundle bundle = new Bundle();
                bundle.putString("placeId", item.placeId);
                bundle.putString("name", item.name);
                bundle.putString("city", item.city);
                bundle.putString("description", item.description);
                bundle.putDouble("rating", item.rating);
                bundle.putString("photo", item.photo);
                bundle.putDouble("totalRating", item.totalRating);

                DetailFragment detailFragment = new DetailFragment();
                detailFragment.setArguments(bundle);

                // Open DetailFragment
                FragmentTransaction transaction = requireFragmentManager().beginTransaction();
                transaction.replace(R.id.container, detailFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
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