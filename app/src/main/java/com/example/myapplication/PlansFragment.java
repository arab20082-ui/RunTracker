package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class PlansFragment extends Fragment {

    public PlansFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_plans, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Plan cards — each starts a guided run
        view.findViewById(R.id.cardPlan1).setOnClickListener(v -> startPlan("5K BEGINNER", "Week 1 · Run 1", "Easy 2 km run"));
        view.findViewById(R.id.cardPlan2).setOnClickListener(v -> startPlan("10K BUILDER", "Week 1 · Run 1", "Easy 3 km run"));
        view.findViewById(R.id.cardPlan3).setOnClickListener(v -> startPlan("INTERVAL SPEED", "Session 1", "6 × 400m sprints"));
        view.findViewById(R.id.cardPlan4).setOnClickListener(v -> startPlan("HALF MARATHON", "Week 1 · Long run", "Easy 5 km run"));
        view.findViewById(R.id.cardPlan5).setOnClickListener(v -> startPlan("RECOVERY RUN",  "Any day", "Easy 20 min jog"));
    }

    private void startPlan(String planName, String session, String description) {
        Toast.makeText(getContext(),
                planName + " · " + session, Toast.LENGTH_SHORT).show();
        // Navigate to RunFragment pre-labeled with the plan
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameLayout, new RunFragment())
                .addToBackStack(null)
                .commit();
    }
}