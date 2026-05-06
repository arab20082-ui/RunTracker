package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.auth.User;


public class SignupFragment extends Fragment {

    private EditText etUsername, etPassword;
    private Button btnSignup;
    private FirebaseServices fbs;

    public SignupFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate and get the root view
        View view = inflater.inflate(R.layout.fragment_signup2, container, false);

        // Connect XML views
        etUsername = view.findViewById(R.id.etUsernameSignup);
        etPassword = view.findViewById(R.id.etPasswordSignup);
        btnSignup  = view.findViewById(R.id.btnSignupSignup);

        // Firebase instance
        fbs = FirebaseServices.getInstance();

        // Button click
        btnSignup.setOnClickListener(v -> {

            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(getActivity(), "Some fields are empty!", Toast.LENGTH_SHORT).show();
                return;
            }

            fbs.getAuth().createUserWithEmailAndPassword(username, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), "You have successfully signed up!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), "Failed to sign up! Check email or password!", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        return view;
    }
}
