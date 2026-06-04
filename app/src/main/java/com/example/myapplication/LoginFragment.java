package com.example.myapplication;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;


public class LoginFragment extends Fragment {

    private EditText etUsername, etPassword;
    private TextView tvSignupLink, tvForgotpassword;
    private Button btnLogin;
    private FirebaseServices fbs;

    public LoginFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) { // ✅ correct lifecycle
        super.onViewCreated(view, savedInstanceState);

        fbs = FirebaseServices.getInstance();

        etUsername     = view.findViewById(R.id.etUsernameLogin);   // ✅ use view param
        etPassword     = view.findViewById(R.id.etPasswordLogin);
        btnLogin       = view.findViewById(R.id.btnLoginLogin);
        tvSignupLink   = view.findViewById(R.id.tvSignupLinkLogin);
        tvForgotpassword = view.findViewById(R.id.tvForgotPasswordLogin);

        tvForgotpassword.setOnClickListener(v -> gotoForgotPasswordFragment());
        tvSignupLink.setOnClickListener(v -> gotoSignupFragment());

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Some fields are empty!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(username).matches()) { // ✅ format check
                etUsername.setError("Enter a valid email address");
                etUsername.requestFocus();
                return;
            }

            fbs.getAuth().signInWithEmailAndPassword(username, password)
                    .addOnCompleteListener(requireActivity(), task -> {
                        if (!isAdded() || getContext() == null) return;    // ✅ safe context guard

                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(),
                                    "Logged in successfully!", Toast.LENGTH_SHORT).show();
                            requireActivity().getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.frameLayout, new AdminFragment())

                                    .commit();
                        } else {
                            Toast.makeText(getContext(),
                                    "Login failed. Check your email or password.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void gotoSignupFragment() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameLayout, new SignupFragment())
                .addToBackStack(null)
                .commit();
    }

    private void gotoForgotPasswordFragment() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameLayout, new ForgotPasswordFragment())
                .addToBackStack(null)
                .commit();
    }
}