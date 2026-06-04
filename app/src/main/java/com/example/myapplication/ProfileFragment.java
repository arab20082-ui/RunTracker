package com.example.myapplication;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.FirebaseServices;
import com.example.myapplication.R;
import com.example.myapplication.User;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.squareup.picasso.Picasso;

public class ProfileFragment extends Fragment {

    private EditText etFirstName, etLastName, etEmail, etPhone, etAddress;
    private EditText etPassword, etConfirmPassword;
    private ImageView ivProfileImage;
    private Button btnUpdate;

    private FirebaseServices fbs;
    private Uri imageUri = null;
    private String currentImageUrl = "";

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    imageUri = uri;
                    ivProfileImage.setImageURI(uri);
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) { // ✅ correct lifecycle
        super.onViewCreated(view, savedInstanceState);
        init(view);
    }

    private void init(View view) { // ✅ use passed view, not getView()
        fbs = FirebaseServices.getInstance();

        etFirstName = view.findViewById(R.id.etFirstNameProfile);
        etLastName = view.findViewById(R.id.etLastNameProfile);
        etEmail = view.findViewById(R.id.etEmailProfile);
        etPhone = view.findViewById(R.id.etPhoneProfile);
        etAddress = view.findViewById(R.id.etAddressProfile);
        etPassword = view.findViewById(R.id.etPasswordProfile);
        etConfirmPassword = view.findViewById(R.id.etConfirmPasswordProfile);
        ivProfileImage = view.findViewById(R.id.ivProfileImageEdit);
        btnUpdate = view.findViewById(R.id.btnUpdateProfile);

        ivProfileImage.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        btnUpdate.setOnClickListener(v -> updateUser());

        loadUserData();
    }

    private void loadUserData() {
        FirebaseUser firebaseUser = fbs.getFirebaseUser();
        if (firebaseUser == null) return;

        etEmail.setText(firebaseUser.getEmail());

        fbs.getUserData(task -> {
            if (!isAdded() || getContext() == null) return; // ✅ async guard

            if (task.isSuccessful()) {
                DocumentSnapshot doc = task.getResult();
                if (doc != null && doc.exists()) {
                    User user = doc.toObject(User.class);
                    if (user != null) {
                        etFirstName.setText(user.getFirstName());
                        etLastName.setText(user.getLastName());
                        etPhone.setText(user.getPhone());
                        etAddress.setText(user.getAddress());
                        currentImageUrl = user.getImgpro() != null ? user.getImgpro() : "";

                        if (!currentImageUrl.isEmpty()) {
                            Picasso.get()
                                    .load(currentImageUrl)
                                    .placeholder(android.R.drawable.ic_menu_camera)
                                    .into(ivProfileImage);
                        }
                    }
                }
            } else {
                Toast.makeText(getContext(), "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUser() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) ||
                TextUtils.isEmpty(email) || TextUtils.isEmpty(phone) ||
                TextUtils.isEmpty(address)) {
            Toast.makeText(getContext(), "Some fields are empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) { // ✅ format check
            etEmail.setError("Enter a valid email address");
            etEmail.requestFocus();
            return;
        }

        if (!password.isEmpty()) {

        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
        }
    }
}