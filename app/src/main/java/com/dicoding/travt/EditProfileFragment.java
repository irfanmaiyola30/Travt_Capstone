package com.dicoding.travt;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class EditProfileFragment extends Fragment {
    private FirebaseAuth auth;
    private FirebaseUser user;
    private StorageReference storageReference;

    private EditText usernameEditText, birthEditText, phoneEditText;
    private Button updateButton;
    private ImageView profileImageView;
    private DatabaseReference databaseReference;
    private FirebaseUser currentUser;
    private Uri imageUri;

    private static final int PICK_IMAGE_REQUEST = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference("profile_images");

        // Initialize views
        usernameEditText = view.findViewById(R.id.profile_username);
        birthEditText = view.findViewById(R.id.profile_bird);
        phoneEditText = view.findViewById(R.id.profile_phone);
        updateButton = view.findViewById(R.id.profle_update_btn);
        profileImageView = view.findViewById(R.id.profile_image_view);

        loadUserProfile();

        // Initialize Firebase components
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference("profile_images");
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid());

        // Load user data from Firebase Realtime Database
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String username = snapshot.child("username").getValue().toString();
                    String birth = snapshot.child("birth").getValue().toString();
                    String phone = snapshot.child("phone").getValue().toString();

                    // Fill EditText with user data
                    usernameEditText.setText(username);
                    birthEditText.setText(birth);
                    phoneEditText.setText(phone);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        // Set onClickListener for profile image
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        // Set onClickListener for update button
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile();
            }
        });

        // Set phoneEditText to be non-editable
        phoneEditText.setFocusable(false);
        phoneEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle click action if needed
            }
        });

        return view;
    }
    private void loadUserProfile() {
        if (user != null) {
            usernameEditText.setText(user.getDisplayName());
            phoneEditText.setText(user.getPhoneNumber());
            if (user.getPhotoUrl() != null) {
                Glide.with(this).load(user.getPhotoUrl()).into(profileImageView);
            }
        }
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImageView.setImageURI(imageUri);
        }
    }

    private void updateProfile() {
        String username = usernameEditText.getText().toString().trim();
        String birth = birthEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();

        // Update username, birth, and phone in Firebase Realtime Database
        databaseReference.child("username").setValue(username);
        databaseReference.child("birth").setValue(birth);
        databaseReference.child("phone").setValue(phone);

        // Upload new profile image to Firebase Storage
        if (imageUri != null) {
            StorageReference fileReference = storageReference.child(UUID.randomUUID().toString() + ".jpg");
            fileReference.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    // Update photo URL in Firebase User profile
                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                            .setPhotoUri(uri)
                                            .build();

                                    currentUser.updateProfile(profileUpdates)
                                            .addOnCompleteListener(task -> {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(getActivity(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                                                    // Navigate back to ProfileFragment
                                                    getParentFragmentManager().popBackStack();
                                                    loadUserProfile();
                                                } else {
                                                    Toast.makeText(getActivity(), "Profile update failed", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getActivity(), "Failed to upload profile image", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // If no new image was uploaded, just update the profile information
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(username)
                    .build();

            currentUser.updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                            // Navigate back to ProfileFragment
                            getParentFragmentManager().popBackStack();
                        } else {
                            Toast.makeText(getActivity(), "Profile update failed", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
