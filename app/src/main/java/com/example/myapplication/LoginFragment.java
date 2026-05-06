package com.example.myapplication;

import android.os.Bundle;

import androidx.annotation.NonNull;
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
    private EditText etUsername , etPassword;
    private TextView tvSignupLink, tvForgotpassword;
    private Button btnLogin;
    private FirebaseServices fbs ;



    public LoginFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login2, container, false);


    }
    @Override
    public void onStart() {
        super.onStart();
        fbs = FirebaseServices.getInstance();
        etUsername = getView().findViewById(R.id.etUsernameLogin);
        etPassword = getView().findViewById(R.id.etPasswordLogin);
        btnLogin = getView().findViewById(R.id.btnLoginLogin);
        tvSignupLink = getView().findViewById(R.id.tvSignupLinkLogin);
        tvForgotpassword=getView().findViewById(R.id.tvForgotPasswordLogin);
        tvForgotpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoForgotPasswordFragment();
            }
        });
        tvSignupLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoSignupFragment();
            }
        });
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
                // data validation
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                if (username.trim().isEmpty() || password.trim().isEmpty()){
                    Toast.makeText(getActivity(), "some fields are empty!", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Login procedure
                fbs.getAuth().signInWithEmailAndPassword(username, password).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        FragmentActivity activity = getActivity();
                        if (activity == null) {
                            return;
                        }

                        if (task.isSuccessful()){
                            Toast.makeText(activity, "you have successfully login!", Toast.LENGTH_SHORT).show();

                            FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
                            ft.replace(R.id.frameLayout, new AdminFragment());
                            ft.commit();

                        }
                        else {

                            Toast.makeText(activity, "failed to login!check user or password!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });



            }
        });


    }

    private void gotoSignupFragment() {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.frameLayout, new SignupFragment());
            ft.commit();
        }
    }
    private void gotoForgotPasswordFragment(){
        FragmentActivity activity = getActivity();
        if (activity != null) {
            FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.frameLayout, new ForgotPasswordFragment());
            ft.commit();
        }
    }

}
