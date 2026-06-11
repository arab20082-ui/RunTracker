package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class RunDetailsFragment extends Fragment {

    private static final String ARG_DISTANCE = "distance";
    private static final String ARG_TIME     = "time";
    private static final String ARG_CALORIES = "calories";
    private static final String ARG_STREAK   = "streak";
    private static final String ARG_PACE     = "pace";
    private static final String ARG_BPM      = "BPM";
    private static final String ARG_DATE     = "date";

    public RunDetailsFragment() {}

    public static RunDetailsFragment newInstance(RunItem run) {
        RunDetailsFragment f = new RunDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DISTANCE, run.getDistance());
        args.putString(ARG_TIME,     run.getTime());
        args.putString(ARG_CALORIES, run.getCalories());
        args.putString(ARG_STREAK,   run.getStreak());
        args.putString(ARG_PACE,     run.getAvgpace());
        args.putString(ARG_BPM,      run.getBPM());
        args.putString(ARG_DATE,     run.getDate());
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView    tvDistance = view.findViewById(R.id.tvDistanceDetail);
        TextView    tvTime     = view.findViewById(R.id.tvTimeDetail);
        TextView    tvCalories = view.findViewById(R.id.tvCaloriesDetail);
        TextView    tvStreak   = view.findViewById(R.id.tvStreakDetail);
        TextView    tvPace     = view.findViewById(R.id.tvPaceDetail);
        TextView    tvBPM      = view.findViewById(R.id.tvBPMDetail);
        TextView    tvDate     = view.findViewById(R.id.tvRunDateDetail);
        ImageButton btnBack    = view.findViewById(R.id.btnBackDetail);
        Button      btnShare   = view.findViewById(R.id.btnShareRun);

        // ── Populate fields ──────────────────────────────
        if (getArguments() != null) {
            Bundle a = getArguments();

            String dist = a.getString(ARG_DISTANCE, "0");
            String time = a.getString(ARG_TIME,     "00:00");
            String cal  = a.getString(ARG_CALORIES, "0");
            String str  = a.getString(ARG_STREAK,   "0");
            String pace = a.getString(ARG_PACE,     "--:--");
            String bpm  = a.getString(ARG_BPM,      "--");
            String date = a.getString(ARG_DATE,     "");

            tvDistance.setText(dist);
            tvTime.setText(time);
            tvCalories.setText(cal);
            tvStreak.setText(str);
            tvPace.setText((pace != null && !pace.isEmpty()) ? pace : "--:--");
            tvBPM.setText((bpm  != null && !bpm.isEmpty())  ? bpm  : "--");

            if (tvDate != null)
                tvDate.setText(Utils.formatDisplayDate(date));

            // ── Share button ─────────────────────────────
            // ✅ FIXED: was wired to nothing before
            if (btnShare != null) {
                btnShare.setOnClickListener(v -> {
                    String shareText = buildShareText(dist, time, cal, pace, bpm, date);
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_SUBJECT, "My Run");
                    intent.putExtra(Intent.EXTRA_TEXT, shareText);
                    startActivity(Intent.createChooser(intent, "Share your run via"));
                });
            }
        }

        // ── Back button ──────────────────────────────────
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getParentFragmentManager() != null)
                    getParentFragmentManager().popBackStack();
            });
        }
    }

    // ── Build share message ───────────────────────────────

    private String buildShareText(String dist, String time,
                                  String cal, String pace,
                                  String bpm, String date) {
        StringBuilder sb = new StringBuilder();
        sb.append("🏃 I just completed a run!\n\n");
        sb.append("📅 Date:      ").append(Utils.formatDisplayDate(date)).append("\n");
        sb.append("📍 Distance:  ").append(dist).append(" km\n");
        sb.append("⏱ Time:      ").append(time).append("\n");
        sb.append("⚡ Avg Pace:  ").append(pace).append("\n");
        sb.append("🔥 Calories:  ").append(cal).append(" kcal\n");
        if (bpm != null && !bpm.equals("0") && !bpm.equals("--"))
            sb.append("❤️ Heart Rate: ").append(bpm).append(" bpm\n");
        sb.append("\nTracked with NRC App 🏅");
        return sb.toString();
    }
}