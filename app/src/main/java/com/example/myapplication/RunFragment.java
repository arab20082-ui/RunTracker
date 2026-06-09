package com.example.myapplication;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;


public class RunFragment extends Fragment {

    private EditText editTextDistance, editTextTime, editTextCalories, editTextStreak,editTextPace,editTextBPM ;
    private Button buttonAddRun;
    private FirebaseFirestore db;

    public RunFragment() {
        // Required empty constructor
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_run, container, false);

        editTextDistance = view.findViewById(R.id.etDistanceRun);
        editTextTime = view.findViewById(R.id.etTimeRun);
        editTextCalories = view.findViewById(R.id.etCaloriesRun);
        editTextStreak = view.findViewById(R.id.etStreakRun);
editTextBPM = view.findViewById(R.id.etBPMRun);

        buttonAddRun = view.findViewById(R.id.btnSaveRun);


        db = FirebaseFirestore.getInstance();

        buttonAddRun.setOnClickListener(v -> addRunToFirestore());

        return view;
    }

    private void addRunToFirestore() {
        String distance = editTextDistance.getText().toString().trim();
        String time = editTextTime.getText().toString().trim();
        String calories = editTextCalories.getText().toString().trim();
        String streak = editTextStreak.getText().toString().trim();
        String bpm = editTextBPM.getText().toString().trim();;

        if (TextUtils.isEmpty(distance) || TextUtils.isEmpty(time) ||
                TextUtils.isEmpty(calories) || TextUtils.isEmpty(streak)) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }


        RunItem run = new RunItem(distance,
                time,
                calories,
                streak ,
                bpm);

        /*
        *     public RunItem(String distance, String time, String calories,
                   String streak, String avgpace, String BPM,
                   String date, String userId)*/


        db.collection("runs")
                .add(run)
                .addOnSuccessListener(documentReference ->
                        Toast.makeText(getContext(), "Run added!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    public EditText getEditTextBPM() {
        return editTextBPM;
    }
}