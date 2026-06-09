package com.example.myapplication;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.squareup.picasso.Picasso;

public class UserProfileFragment extends Fragment {

    private ImageView imgProfile;
    private TextView  tvUsername, tvEmail;
    private Button    btnLogout, btnEditProfile;
    private FirebaseServices fbs;                          // ✅ use service layer
    public UserProfileFragment() {}
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_profile, container, false);
    }
    @SuppressLint("WrongViewCast")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fbs = FirebaseServices.getInstance();               // ✅ single source of truth
        imgProfile    = view.findViewById(R.id.imgUserProfile);
        tvUsername    = view.findViewById(R.id.tvUsername);
        tvEmail       = view.findViewById(R.id.tvEmail);
        btnLogout     = view.findViewById(R.id.btnLogout);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        loadUserData();
        btnEditProfile.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameLayout, new ProfileFragment())
                        .addToBackStack(null)
                        .commit()
        );

        btnLogout.setOnClickListener(v -> handleLogout());
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserData();                                     // ✅ refresh after returning from ProfileFragment
    }
    private void loadUserData() {
        fbs.getUserData(task -> {
            if (!isAdded() || getContext() == null) return; // ✅ async guard

            if (task.isSuccessful() && task.getResult() != null
                    && task.getResult().exists()) {

                User user = task.getResult().toObject(User.class);
                if (user != null) {
                    // ✅ firstName + lastName instead of missing username field
                    String fullName = user.getFirstName() + " " + user.getLastName();
                    tvUsername.setText(fullName.trim());

                    if (user.getEmail() != null) {
                        tvEmail.setText(user.getEmail());
                    }

                    if (user.getImgpro() != null && !user.getImgpro().isEmpty()) {
                        Picasso.get()
                                .load(user.getImgpro())
                                .placeholder(R.drawable.ic_user)
                                .error(R.drawable.ic_user)
                                .into(imgProfile);
                    }
                }
            } else {
                Log.e("UserProfileFragment", "Failed to load user data");
                Toast.makeText(getContext(),
                        "Failed to load profile details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleLogout() {
        if (!isAdded() || getContext() == null) return;    // ✅ safe guard

        fbs.signOut();

        Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();

        // ✅ Navigate to login, clear back stack — app stays alive
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameLayout, new LoginFragment())
                .commit();
    }
}