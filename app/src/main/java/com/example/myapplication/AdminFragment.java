package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

public class AdminFragment extends Fragment {

    // ── Views ─────────────────────────────────────────────
    private ImageView  imgNavProfile;
    private CardView   cardNavProfile;
    private Button     btnStartRun;
    private TextView   tvDailyGoal;
    private TextView   tvWeeklyDistance;
    private TextView   tvDistanceChange;
    private TextView   tvAvgPace;
    private TextView   tvAvgHR;
    private TextView   tvTrainingTag;
    private TextView   tvTrainingName;
    private TextView   tvTrainingProgress;
    private TextView   tvActivityName;
    private TextView   tvActivityMeta;
    private TextView   tvViewAll;
    private View       cardTraining;
    private View       cardLatestActivity;
    private View       navRun;
    private View       navPlans;
    private View       navHistory;

    // ── Firebase ──────────────────────────────────────────
    private FirebaseServices fbs;
    private FirebaseFirestore db;

    public AdminFragment() {}

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

        fbs = FirebaseServices.getInstance();
        db  = FirebaseFirestore.getInstance();

        bindViews(view);
        setupNavigation();
        loadUserProfileImage();
        loadWeeklyStats();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserProfileImage(); // refresh avatar when returning from ProfileFragment
    }

    // ── View binding ───────────────────────────────────────

    private void bindViews(View view) {
        imgNavProfile      = view.findViewById(R.id.imgNavProfile);
        cardNavProfile     = view.findViewById(R.id.cardNavProfile);
        btnStartRun        = view.findViewById(R.id.btnStartRun);
        tvDailyGoal        = view.findViewById(R.id.tvDailyGoal);
        tvWeeklyDistance   = view.findViewById(R.id.tvWeeklyDistance);
        tvDistanceChange   = view.findViewById(R.id.tvDistanceChange);
        tvAvgPace          = view.findViewById(R.id.tvAvgPace);
        tvAvgHR            = view.findViewById(R.id.tvAvgHR);
        tvTrainingTag      = view.findViewById(R.id.tvTrainingTag);
        tvTrainingName     = view.findViewById(R.id.tvTrainingName);
        tvTrainingProgress = view.findViewById(R.id.tvTrainingProgress);
        tvActivityName     = view.findViewById(R.id.tvActivityName);
        tvActivityMeta     = view.findViewById(R.id.tvActivityMeta);
        tvViewAll          = view.findViewById(R.id.tvViewAll);
        cardTraining       = view.findViewById(R.id.cardTraining);
        cardLatestActivity = view.findViewById(R.id.cardLatestActivity);
        navRun             = view.findViewById(R.id.navRun);
        navPlans           = view.findViewById(R.id.navPlans);
        navHistory         = view.findViewById(R.id.navHistory);
    }

    // ── Navigation ─────────────────────────────────────────

    private void setupNavigation() {

        // START RUN → RunFragment
        btnStartRun.setOnClickListener(v -> navigateTo(new RunFragment()));

        // Profile avatar → UserProfileFragment
        cardNavProfile.setOnClickListener(v -> navigateTo(new UserProfileFragment()));

        // VIEW ALL → RunListFragment
        tvViewAll.setOnClickListener(v -> navigateTo(new RunListFragment()));

        // Latest activity card → RunListFragment
        cardLatestActivity.setOnClickListener(v -> navigateTo(new RunListFragment()));

        // Training card → RunFragment
        cardTraining.setOnClickListener(v -> navigateTo(new RunFragment()));

        // Bottom nav — RUN
        navRun.setOnClickListener(v -> navigateTo(new RunFragment()));

        // Bottom nav — PLANS (placeholder: reuse RunListFragment until Plans screen exists)
        navPlans.setOnClickListener(v -> navigateTo(new RunListFragment()));

        // Bottom nav — HISTORY
        navHistory.setOnClickListener(v -> navigateTo(new RunListFragment()));
    }

    /** Replace current fragment and add to back stack. */
    private void navigateTo(Fragment destination) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameLayout, destination)
                .addToBackStack(null)
                .commit();
    }

    // ── Firebase: profile image ────────────────────────────

    private void loadUserProfileImage() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();

        db.collection("Users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!isAdded() || getContext() == null) return;
                    if (imgNavProfile == null) return;

                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null
                                && user.getImgpro() != null
                                && !user.getImgpro().isEmpty()) {
                            Picasso.get()
                                    .load(user.getImgpro())
                                    .placeholder(R.drawable.ic_user)
                                    .error(R.drawable.ic_user)
                                    .into(imgNavProfile);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("AdminFragment", "Error loading profile image", e));
    }

    // ── Firebase: weekly stats ─────────────────────────────

    private void loadWeeklyStats() {
        String uid = fbs.getCurrentUid();
        if (uid == null) return;

        fbs.getUserRuns(task -> {
            if (!isAdded() || getContext() == null) return;
            if (!task.isSuccessful() || task.getResult() == null) return;

            double totalKm   = 0;
            double totalPace = 0;
            double totalBpm  = 0;
            int    count     = 0;
            RunItem latestRun = null;

            for (QueryDocumentSnapshot doc : task.getResult()) {
                RunItem run = doc.toObject(RunItem.class);
                run.setId(doc.getId());

                // Accumulate distance
                try {
                    if (run.getDistance() != null)
                        totalKm += Double.parseDouble(run.getDistance());
                } catch (NumberFormatException ignored) {}

                // Accumulate pace seconds
                try {
                    if (run.getAvgpace() != null && !run.getAvgpace().isEmpty())
                        totalPace += Utils.timeStringToSeconds(run.getAvgpace());
                } catch (Exception ignored) {}

                // Accumulate BPM
                try {
                    if (run.getBPM() != null)
                        totalBpm += Double.parseDouble(run.getBPM());
                } catch (NumberFormatException ignored) {}

                // Track most recent run (Firestore orders by date desc)
                if (latestRun == null) latestRun = run;

                count++;
            }

            // ── Update distance card ──
            String distText = String.format("%.1f", totalKm);
            tvWeeklyDistance.setText(distText);
            tvDailyGoal.setText("TOTAL: " + distText + " KM");

            // ── Update avg pace card ──
            if (count > 0) {
                long avgPaceSec = (long) (totalPace / count);
                tvAvgPace.setText(Utils.secondsToTimeString(avgPaceSec) + "\"");
            } else {
                tvAvgPace.setText("--:--");
            }

            // ── Update avg HR card ──
            if (count > 0) {
                int avgBpm = (int) (totalBpm / count);
                tvAvgHR.setText(String.valueOf(avgBpm));
            } else {
                tvAvgHR.setText("--");
            }

            // ── Update latest activity row ──
            if (latestRun != null) {
                tvActivityName.setText(
                        "RUN · " + Utils.formatDisplayDate(latestRun.getDate()));
                tvActivityMeta.setText(
                        latestRun.getDistance() + " KM  ·  " + latestRun.getTime());

                // Tap latest activity → open its details
                final RunItem finalRun = latestRun;
                cardLatestActivity.setOnClickListener(v ->
                        navigateTo(RunDetailsFragment.newInstance(finalRun)));
            }

            // ── Update distance change badge (placeholder logic) ──
            tvDistanceChange.setText(count > 0 ? "+" + count + " runs" : "");
        });
    }
}