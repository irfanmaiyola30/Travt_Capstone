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

import com.dicoding.travt.MainActivity;
import com.dicoding.travt.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class loginActivity extends AppCompatActivity {

    private EditText email,password;
    private Button login;
    private TextView regis, forgotPassword;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initView();
        keRegis();
        loginn();
        sendCredentialsToServer();
        setupForgotPassword();
    }

    private void setupForgotPassword() {
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(loginActivity.this, ForgotPassword.class));
            }
        });
    }

    private void loginn() {
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(email.getText().length()>0 && password.getText().length()>0){
                    login(email.getText().toString(), password.getText().toString());
                }else{
                    Toast.makeText(loginActivity.this, "masukan email dan password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void login(String email, String password){
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful() && task.getResult()!=null){
                    if(task.getResult().getUser()!=null && task.getResult().getUser().isEmailVerified()){
                        reloada();
                    }else{
                        Toast.makeText(loginActivity.this, "password atau email salah atau mungkin belum verifikasi email cek email", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(loginActivity.this, "cek kembali email atau password anda", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void reloada(){
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = auth.getCurrentUser();
        if(currentUser != null && currentUser.isEmailVerified()){
            reloada();
        }
    }

    private void keRegis() {
        regis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(loginActivity.this, registerActivity.class));
            }
        });
    }

    private void initView() {
        regis = findViewById(R.id.signUpTextView);
        login = findViewById(R.id.loginButton);
        email = findViewById(R.id.emailEditText);
        password = findViewById(R.id.passwordEditText);
        forgotPassword = findViewById(R.id.forgotPasswordTextView);
    }
    private void sendCredentialsToServer() {
        OkHttpClient client = new OkHttpClient();

        // Membuat JSON Object untuk dikirim ke server
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email", email); // Mengambil email dari variabel class
            jsonBody.put("password", password); // Mengambil password dari variabel class
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(loginActivity.this, "Error: Gagal membuat JSON Object.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Membuat request body dari JSON Object
        RequestBody requestBody = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json; charset=utf-8"));

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
                        Toast.makeText(loginActivity.this, "Gagal terhubung ke server. Periksa koneksi internet anda.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseData);
                        final String uid = jsonObject.getString("uid");

                        // Menampilkan pesan Toast di UI thread
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(loginActivity.this, "Login berhasil dengan UID: " + uid, Toast.LENGTH_SHORT).show();
                                reloada(); // Pindah ke MainActivity atau handle kegiatan setelah login berhasil
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(loginActivity.this, "Error: Gagal parsing response JSON.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(loginActivity.this, "Login gagal. Periksa kembali email atau password Anda.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

}
