package com.dicoding.travt.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dicoding.travt.R;
import com.dicoding.travt.account.loginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    private FirebaseAuth auth;
    private TextView usernameTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        auth = FirebaseAuth.getInstance();

        Button logoutButton = rootView.findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Sign out the user
                auth.signOut();

                // Redirect to the login activity
                Intent intent = new Intent(getActivity(), loginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                getActivity().finish();
            }
        });

        // Inisialisasi TextView untuk menampilkan nama pengguna
        usernameTextView = rootView.findViewById(R.id.usernameTextView);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Memperbarui tampilan nama pengguna setiap kali fragmen dimulai
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String firstname = currentUser.getEmail();
            usernameTextView.setText(firstname);
        }
    }

}
