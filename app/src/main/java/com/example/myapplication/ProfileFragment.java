package com.example.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.squareup.picasso.Picasso;

public class ProfileFragment extends Fragment {

    private EditText  etFirstName, etLastName, etEmail, etPhone,
            etAddress, etPassword, etConfirmPassword;
    private EditText  etWeight, etHeight, etAge;
    private ImageView ivProfileImage;
    private Button    btnUpdate;

    private FirebaseServices fbs;
    private Uri    imageUri        = null;
    private String currentImageUrl = "";

    private static final int PERMISSION_CODE = 201;

    // ✅ Use GetContent launcher — works on all Android versions
    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            imageUri = uri;
                            ivProfileImage.setImageURI(uri);
                        }
                    });

    public ProfileFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fbs = FirebaseServices.getInstance();

        etFirstName       = view.findViewById(R.id.etFirstNameProfile);
        etLastName        = view.findViewById(R.id.etLastNameProfile);
        etEmail           = view.findViewById(R.id.etEmailProfile);
        etPhone           = view.findViewById(R.id.etPhoneProfile);
        etAddress         = view.findViewById(R.id.etAddressProfile);
        etPassword        = view.findViewById(R.id.etPasswordProfile);
        etConfirmPassword = view.findViewById(R.id.etConfirmPasswordProfile);
        etWeight          = view.findViewById(R.id.etWeightProfile);
        etHeight          = view.findViewById(R.id.etHeightProfile);
        etAge             = view.findViewById(R.id.etAgeProfile);
        ivProfileImage    = view.findViewById(R.id.ivProfileImageEdit);
        btnUpdate         = view.findViewById(R.id.btnUpdateProfile);

        // ✅ Tapping the photo requests permission then opens picker
        ivProfileImage.setOnClickListener(v -> checkPermissionAndPickImage());

        btnUpdate.setOnClickListener(v -> updateUser());
        loadUserData();
    }

    // ── Permission + picker ───────────────────────────────

    private void checkPermissionAndPickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ — READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        PERMISSION_CODE);
            }
        } else {
            // Android 12 and below — READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_CODE);
            }
        }
    }

    private void openImagePicker() {
        pickImageLauncher.launch("image/*");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                Toast.makeText(getContext(),
                        "Permission required to select a photo.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    // ── Load data ─────────────────────────────────────────

    private void loadUserData() {
        FirebaseUser fu = fbs.getFirebaseUser();
        if (fu == null) return;
        etEmail.setText(fu.getEmail());

        fbs.getUserData(task -> {
            if (!isAdded() || getContext() == null) return;
            if (task.isSuccessful() && task.getResult() != null
                    && task.getResult().exists()) {
                DocumentSnapshot doc = task.getResult();
                User user = doc.toObject(User.class);
                if (user != null) {
                    etFirstName.setText(user.getFirstName());
                    etLastName.setText(user.getLastName());
                    etPhone.setText(user.getPhone());
                    etAddress.setText(user.getAddress());

                    if (user.getWeightKg() > 0)
                        etWeight.setText(String.valueOf((int) user.getWeightKg()));
                    if (user.getHeightCm() > 0)
                        etHeight.setText(String.valueOf(user.getHeightCm()));
                    if (user.getAge() > 0)
                        etAge.setText(String.valueOf(user.getAge()));

                    currentImageUrl = user.getImgpro() != null
                            ? user.getImgpro() : "";
                    if (!currentImageUrl.isEmpty())
                        Picasso.get()
                                .load(currentImageUrl)
                                .placeholder(R.drawable.ic_user)
                                .error(R.drawable.ic_user)
                                .into(ivProfileImage);
                }
            } else {
                Toast.makeText(getContext(),
                        "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ── Save ──────────────────────────────────────────────

    private void updateUser() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName  = etLastName.getText().toString().trim();
        String email     = etEmail.getText().toString().trim();
        String phone     = etPhone.getText().toString().trim();
        String address   = etAddress.getText().toString().trim();
        String password  = etPassword.getText().toString().trim();
        String confirm   = etConfirmPassword.getText().toString().trim();
        String weightStr = etWeight.getText().toString().trim();
        String heightStr = etHeight.getText().toString().trim();
        String ageStr    = etAge.getText().toString().trim();

        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName)
                || TextUtils.isEmpty(email) || TextUtils.isEmpty(phone)
                || TextUtils.isEmpty(address)) {
            Toast.makeText(getContext(),
                    "Some fields are empty.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email");
            etEmail.requestFocus();
            return;
        }
        if (!password.isEmpty()) {
            if (!password.equals(confirm)) {
                etConfirmPassword.setError("Passwords do not match");
                etConfirmPassword.requestFocus();
                return;
            }
            if (password.length() < 6) {
                etPassword.setError("Minimum 6 characters");
                etPassword.requestFocus();
                return;
            }
            FirebaseUser fu = fbs.getFirebaseUser();
            if (fu != null) fu.updatePassword(password);
        }

        double weightKg = 70.0;
        int    heightCm = 170;
        int    age      = 25;
        try { if (!weightStr.isEmpty()) weightKg = Double.parseDouble(weightStr); }
        catch (Exception ignored) {}
        try { if (!heightStr.isEmpty()) heightCm = Integer.parseInt(heightStr); }
        catch (Exception ignored) {}
        try { if (!ageStr.isEmpty()) age = Integer.parseInt(ageStr); }
        catch (Exception ignored) {}

        final double fw = weightKg;
        final int    fh = heightCm;
        final int    fa = age;

        if (imageUri != null) {
            // ✅ Show uploading feedback
            Toast.makeText(getContext(),
                    "Uploading photo…", Toast.LENGTH_SHORT).show();

            Utils.uploadImage(requireContext(), imageUri,
                    new Utils.UploadCallback() {
                        @Override public void onUploaded(String url) {
                            saveToFirestore(firstName, lastName, email,
                                    address, phone, url, fw, fh, fa);
                        }
                        @Override public void onFailed(String err) {
                            if (!isAdded() || getContext() == null) return;
                            Toast.makeText(getContext(),
                                    "Photo upload failed, saving other data.",
                                    Toast.LENGTH_SHORT).show();
                            saveToFirestore(firstName, lastName, email,
                                    address, phone, currentImageUrl, fw, fh, fa);
                        }
                    });
        } else {
            saveToFirestore(firstName, lastName, email,
                    address, phone, currentImageUrl, fw, fh, fa);
        }
    }

    private void saveToFirestore(String fn, String ln, String email,
                                 String addr, String ph, String img,
                                 double weightKg, int heightCm, int age) {
        User user = new User(fn, ln, email, addr, ph, img,
                weightKg, heightCm, age);
        fbs.updateUser(user, task -> {
            if (!isAdded() || getContext() == null) return;
            if (task.isSuccessful()) {
                Toast.makeText(getContext(),
                        "Profile updated!", Toast.LENGTH_SHORT).show();
                // Go back so UserProfileFragment refreshes
                requireActivity().getSupportFragmentManager()
                        .popBackStack();
            } else {
                Toast.makeText(getContext(),
                        "Update failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}