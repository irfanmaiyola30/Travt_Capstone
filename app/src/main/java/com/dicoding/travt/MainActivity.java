package com.dicoding.travt;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.MenuItem;

import com.dicoding.travt.fragment.FavoriteFragment;
import com.dicoding.travt.fragment.PlaceFragment;
import com.dicoding.travt.fragment.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.dicoding.travt.fragment.HomeFragment;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadFragment(new HomeFragment());
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setItemIconTintList(null);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.profile) {
            loadFragment(new ProfileFragment());
            return true;
        } else if (id == R.id.home) {
            loadFragment(new HomeFragment());
            return true;
        } else if (id == R.id.travt) {
            loadFragment(new PlaceFragment());
            return true;
        } else if (id == R.id.fav) {
            loadFragment(new FavoriteFragment());
            return true;

        }
        return false;
    }

}
