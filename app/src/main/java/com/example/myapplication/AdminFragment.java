package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

public class AdminFragment extends Fragment {

    private ImageView imgUserProfile;
    private FirebaseFirestore db;

    public AdminFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        Button btnAdd = view.findViewById(R.id.btnAdd);
        Button btnAll = view.findViewById(R.id.btnAll);
        CardView cardUserProfile = view.findViewById(R.id.cardUserProfile);
        imgUserProfile = view.findViewById(R.id.imgUserProfile); // ربط الصورة الشخصية من الـ XML

        btnAdd.setOnClickListener(v -> getParentFragmentManager().beginTransaction()
                .replace(R.id.main, new RunFragment())
                .addToBackStack(null)
                .commit());

        btnAll.setOnClickListener(v -> getParentFragmentManager().beginTransaction()
                .replace(R.id.main, new RunListFragment())
                .addToBackStack(null)
                .commit());

        cardUserProfile.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.main, new UserProfileFragment())
                    .addToBackStack(null)
                    .commit();
        });




    }
    @Override
    public void onResume() {
        super.onResume();
        loadUserProfileImage(); // ✅ only here, not in onViewCreated
    }

    private void loadUserProfileImage() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) return; // ✅ safe auth check
        String uid = auth.getCurrentUser().getUid();
        db.collection("Users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null && user.getImgpro() != null && !user.getImgpro().isEmpty()) {
                            if (isAdded() && getContext() != null && imgUserProfile != null) { // ✅ null-safe
                                Picasso.get()
                                        .load(user.getImgpro())
                                        .placeholder(R.drawable.ic_user)
                                        .error(R.drawable.ic_user)
                                        .into(imgUserProfile);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("AdminFragment", "Error loading profile image", e));
    }}