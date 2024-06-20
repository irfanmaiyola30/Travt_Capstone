package com.dicoding.travt.account;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dicoding.travt.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class registerActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private ProgressDialog progres;
    private TextView txt_login;
    private Button btn_regist;
    private EditText username, last_name, email, birth, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();

        progres = new ProgressDialog(registerActivity.this);
        progres.setTitle("Loading");
        progres.setMessage("Tunggu sesaat");
        progres.setCancelable(false);

        initView();
        setListeners();
    }

    private void setListeners() {
        txt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(registerActivity.this, loginActivity.class));
                finish();
            }
        });

        btn_regist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFormValid()) {
                    String passwordText = password.getText().toString();
                    registerUser(username.getText().toString(), last_name.getText().toString(), email.getText().toString(), birth.getText().toString(), passwordText);
                } else {
                    Toast.makeText(registerActivity.this, "Masukan data lengkap", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean isFormValid() {
        return username.getText().length() > 0 &&
                last_name.getText().length() > 0 &&
                email.getText().length() > 0 &&
                birth.getText().length() > 0 &&
                password.getText().length() > 0;
    }

    private void registerUser(String firstName, String lastName, String email, String birth, String password) {
        progres.show();
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progres.dismiss();
                if (task.isSuccessful() && task.getResult() != null) {
                    FirebaseUser firebaseUser = task.getResult().getUser();
                    if (firebaseUser != null) {
                        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                                .setDisplayName(firstName + " " + lastName)
                                .build();
                        firebaseUser.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    firebaseUser.sendEmailVerification();
                                    Toast.makeText(registerActivity.this, "Cek email untuk verifikasi", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(registerActivity.this, loginActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(registerActivity.this, "Gagal memperbarui profil", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(registerActivity.this, "Register gagal cek koneksi", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void reload() {
        Toast.makeText(this, "Cek email untuk verifikasi", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(getApplicationContext(), loginActivity.class));
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            reload();
        }
    }

    private void initView() {
        btn_regist = findViewById(R.id.registerButton);
        username = findViewById(R.id.firstNameEditText);
        last_name = findViewById(R.id.lastNameEditText);
        email = findViewById(R.id.emailEditText);
        birth = findViewById(R.id.birthDateEditText);
        password = findViewById(R.id.passwordEditText);
        txt_login = findViewById(R.id.loginTextView);
    }
}
