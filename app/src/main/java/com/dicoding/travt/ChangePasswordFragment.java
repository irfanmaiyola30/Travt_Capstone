package com.dicoding.travt;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ChangePasswordFragment extends Fragment {

    private EditText passwordEditText, newPasswordEditText, confirmPasswordEditText;
    private Button updateButton;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_password, container, false);

        // Inisialisasi views
        passwordEditText = view.findViewById(R.id.pass_lama);
        newPasswordEditText = view.findViewById(R.id.pass_baru);
        confirmPasswordEditText = view.findViewById(R.id.pass_konfirmasi);
        updateButton = view.findViewById(R.id.password_update_btn);

        // Mendapatkan instance dari Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Mengatur onClickListener untuk tombol update
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
            }
        });

        return view;
    }

    private void changePassword() {
        String password = passwordEditText.getText().toString().trim();
        String newPassword = newPasswordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        // Validasi input
        if (password.isEmpty()) {
            passwordEditText.setError("Password lama diperlukan");
            passwordEditText.requestFocus();
            return;
        }

        if (newPassword.isEmpty()) {
            newPasswordEditText.setError("Password baru diperlukan");
            newPasswordEditText.requestFocus();
            return;
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordEditText.setError("Konfirmasi password diperlukan");
            confirmPasswordEditText.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Konfirmasi password tidak cocok");
            confirmPasswordEditText.requestFocus();
            return;
        }

        // Ubah password
        mAuth.getCurrentUser().updatePassword(newPassword)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Password berhasil diperbarui", Toast.LENGTH_SHORT).show();
                            // Handle success, maybe navigate to another fragment or perform other actions
                        } else {
                            Toast.makeText(getContext(), "Gagal memperbarui password. Silakan coba lagi.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
