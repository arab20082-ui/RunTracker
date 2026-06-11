package com.example.myapplication;

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
    private TextView  tvEmailRow, tvPhoneRow, tvAddressRow; // ✅ the 3 broken rows
    private TextView  tvStatRuns, tvStatKm, tvStatStreak;
    private Button    btnEditProfile;
    private View      btnLogout;
    private FirebaseServices fbs;

    public UserProfileFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fbs = FirebaseServices.getInstance();

        imgProfile   = view.findViewById(R.id.imgUserProfile);
        tvUsername   = view.findViewById(R.id.tvUsername);
        tvEmail      = view.findViewById(R.id.tvEmail);         // header subtitle
        btnLogout    = view.findViewById(R.id.btnLogout);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);

        // ✅ The three info rows that were always showing "—"
        tvEmailRow   = view.findViewById(R.id.tvEmailRow);
        tvPhoneRow   = view.findViewById(R.id.tvPhoneRow);
        tvAddressRow = view.findViewById(R.id.tvAddressRow);

        // Stats
        tvStatRuns   = view.findViewById(R.id.tvStatRuns);
        tvStatKm     = view.findViewById(R.id.tvStatKm);
        tvStatStreak = view.findViewById(R.id.tvStatStreak);

        loadUserData();
        loadStats();

        btnEditProfile.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameLayout, new ProfileFragment())
                        .addToBackStack(null).commit());

        btnLogout.setOnClickListener(v -> logout());
    }

    // Reload every time user returns from ProfileFragment
    @Override
    public void onResume() {
        super.onResume();
        loadUserData();
        loadStats();
    }

    private void loadUserData() {
        fbs.getUserData(task -> {
            if (!isAdded() || getContext() == null) return;

            if (task.isSuccessful() && task.getResult() != null
                    && task.getResult().exists()) {

                User user = task.getResult().toObject(User.class);
                if (user == null) return;

                // ── Header ──────────────────────────────
                String fullName = ((user.getFirstName() != null ? user.getFirstName() : "")
                        + " " + (user.getLastName() != null ? user.getLastName() : "")).trim();
                tvUsername.setText(fullName.isEmpty() ? "Runner" : fullName);

                String email = user.getEmail() != null ? user.getEmail() : "";
                tvEmail.setText(email);           // header subtitle

                // ── Info rows ✅ now actually populated ──
                tvEmailRow.setText(email.isEmpty() ? "—" : email);

                String phone = user.getPhone() != null ? user.getPhone() : "";
                tvPhoneRow.setText(phone.isEmpty() ? "—" : phone);

                String address = user.getAddress() != null ? user.getAddress() : "";
                tvAddressRow.setText(address.isEmpty() ? "—" : address);

                // ── Profile photo ────────────────────────
                String img = user.getImgpro();
                if (img != null && !img.isEmpty()) {
                    Picasso.get()
                            .load(img)
                            .placeholder(R.drawable.ic_user)
                            .error(R.drawable.ic_user)
                            .into(imgProfile);
                }

            } else {
                Log.e("UserProfileFragment", "Failed to load user data");
                Toast.makeText(getContext(),
                        "Failed to load profile details",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadStats() {
        fbs.getUserRuns(task -> {
            if (!isAdded() || getContext() == null) return;
            if (!task.isSuccessful() || task.getResult() == null) return;

            int    runCount  = 0;
            double totalKm   = 0;
            java.util.List<String> dates = new java.util.ArrayList<>();

            for (com.google.firebase.firestore.QueryDocumentSnapshot doc
                    : task.getResult()) {
                RunItem run = doc.toObject(RunItem.class);
                runCount++;
                try {
                    if (run.getDistance() != null)
                        totalKm += Double.parseDouble(run.getDistance());
                } catch (Exception ignored) {}
                if (run.getDate() != null) dates.add(run.getDate());
            }

            int streak = Utils.calculateStreak(dates);

            if (tvStatRuns   != null) tvStatRuns.setText(String.valueOf(runCount));
            if (tvStatKm     != null) tvStatKm.setText(String.format("%.0f", totalKm));
            if (tvStatStreak != null) tvStatStreak.setText(String.valueOf(streak));
        });
    }

    private void logout() {
        if (!isAdded() || getContext() == null) return;
        fbs.signOut();
        Toast.makeText(getContext(),
                "Logged out successfully", Toast.LENGTH_SHORT).show();
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameLayout, new LoginFragment())
                .commit();
    }
}