package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;

public class SignupFragment extends Fragment {

    private EditText etFirstName, etLastName, etEmail, etPhone,
            etAddress, etPassword, etConfirmPassword;
    private Button btnSignup;
    private FirebaseServices fbs;

    public SignupFragment() {}
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  // ✅ inflate only
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_signup2, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) { // ✅ bind here
        super.onViewCreated(view, savedInstanceState);

        fbs = FirebaseServices.getInstance();

        etFirstName       = view.findViewById(R.id.etFirstNameSignup);
        etLastName        = view.findViewById(R.id.etLastNameSignup);
        etEmail           = view.findViewById(R.id.etEmailSignup);
        etPhone           = view.findViewById(R.id.etPhoneSignup);
        etAddress         = view.findViewById(R.id.etAddressSignup);
        etPassword        = view.findViewById(R.id.etPasswordSignup);
        etConfirmPassword = view.findViewById(R.id.etConfirmPasswordSignup);
        btnSignup         = view.findViewById(R.id.btnSignup);

        view.findViewById(R.id.tvLoginLinkSignup).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameLayout, new LoginFragment())
                        .addToBackStack(null)
                        .commit()
        );

        btnSignup.setOnClickListener(v -> attemptSignup());
    }

    private void attemptSignup() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName  = etLastName.getText().toString().trim();
        String email     = etEmail.getText().toString().trim();
        String phone     = etPhone.getText().toString().trim();
        String address   = etAddress.getText().toString().trim();
        String password  = etPassword.getText().toString().trim();
        String confirm   = etConfirmPassword.getText().toString().trim();

        // ✅ All fields present
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() ||
                phone.isEmpty() || address.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Some fields are empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Email format
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email address");
            etEmail.requestFocus();
            return;
        }

        // ✅ Password length
        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        // ✅ Passwords match
        if (!password.equals(confirm)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        fbs.getAuth().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (!isAdded() || getContext() == null) return; // ✅ async guard

                    if (task.isSuccessful()) {
                        // ✅ Save user document to Firestore
                        // User constructor: username, firstName, lastName, email, address, phone, imgpro
                        User newUser = new User(firstName, lastName, email, address, phone, "");

                        fbs.getFirestore().collection("users").add(newUser).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
                        /*
                        fbs.createUser(newUser, saveTask -> {
                            if (!isAdded() || getContext() == null) return;
                            // ✅ Navigate to dashboard, no back stack
                            requireActivity().getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.frameLayout, new AdminFragment())
                                    .commit();
                        });*/
                    } else {
                        Toast.makeText(getContext(),
                                "Signup failed. Check your email or password.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}