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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class registerActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private ProgressDialog progress;
    private TextView txt_login;
    private Button btn_regist;
    private EditText username, last_name, email, birth, password;
    private OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        progress = new ProgressDialog(registerActivity.this);
        progress.setTitle("Loading");
        progress.setMessage("Tunggu sebentar");
        progress.setCancelable(false);

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
                    String firstName = username.getText().toString();
                    String lastName = last_name.getText().toString();
                    String userEmail = email.getText().toString();
                    String userBirth = birth.getText().toString();
                    String userPassword = password.getText().toString();

                    registerUser(firstName, lastName, userEmail, userBirth, userPassword);
                } else {
                    Toast.makeText(registerActivity.this, "Isi data dengan lengkap", Toast.LENGTH_SHORT).show();
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
        progress.show();
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progress.dismiss();
                if (task.isSuccessful() && task.getResult() != null) {
                    FirebaseUser firebaseUser = task.getResult().getUser();
                    if (firebaseUser != null) {
                        // Update display name
                        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                                .setDisplayName(firstName + " " + lastName)
                                .build();
                        firebaseUser.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // Send verification email
                                    firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(registerActivity.this, "Cek email untuk verifikasi", Toast.LENGTH_SHORT).show();
                                                sendUserDataToServer(firstName, lastName, email, birth); // Kirim data ke server setelah berhasil verifikasi
                                                startActivity(new Intent(registerActivity.this, loginActivity.class));
                                                finish();
                                            } else {
                                                Toast.makeText(registerActivity.this, "Gagal mengirim email verifikasi", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                } else {
                                    Toast.makeText(registerActivity.this, "Gagal memperbarui profil", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(registerActivity.this, "Registrasi gagal. Cek koneksi Anda", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(registerActivity.this, "Registrasi gagal: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendUserDataToServer(String firstName, String lastName, String email, String birth) {
        // Membuat request body dengan data pengguna
        RequestBody requestBody = new FormBody.Builder()
                .add("firstName", firstName)
                .add("lastName", lastName)
                .add("email", email)
                .add("birth", birth)
                .build();

        // Membuat request untuk mengirim data ke server
        Request request = new Request.Builder()
                .url("http://34.101.192.36:3000/login") // Ganti dengan URL endpoint API Anda
                .post(requestBody)
                .build();

        // Mengirim request ke server secara asynchronous
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(registerActivity.this, "Gagal terhubung ke server. Periksa koneksi internet Anda.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseData);
                        String message = jsonObject.getString("message");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(registerActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(registerActivity.this, "Gagal parsing response JSON.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(registerActivity.this, "Registrasi gagal di server.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            currentUser.reload();
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
