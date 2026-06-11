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
    private ImageView imgNavProfile;
    private ImageView ivHeroImage;       // ✅ hero running photo
    private ImageView ivTrainingBg;      // ✅ training card photo
    private CardView  cardNavProfile;
    private Button    btnStartRun;
    private TextView  tvDailyGoal, tvWeeklyDistance, tvDistanceChange;
    private TextView  tvAvgPace, tvAvgHR;
    private TextView  tvActivityName, tvActivityMeta, tvViewAll;
    private View      cardTraining, cardLatestActivity;
    private View      navRun, navPlans, navHistory;

    // ── Firebase ──────────────────────────────────────────
    private FirebaseServices  fbs;
    private FirebaseFirestore db;

    // ✅ Beautiful free running photo (Unsplash — no API key needed)
    private static final String HERO_IMAGE_URL =
            "https://images.unsplash.com/photo-1571008887538-b36bb32f4571" +
                    "?w=800&q=80&fit=crop";

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
        loadHeroPhoto();       // ✅ load the running photo
        setupNavigation();
        loadUserProfileImage();
        loadWeeklyStats();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserProfileImage();
        loadWeeklyStats();
    }

    // ── Binding ───────────────────────────────────────────

    private void bindViews(View view) {
        imgNavProfile      = view.findViewById(R.id.imgNavProfile);
        ivHeroImage        = view.findViewById(R.id.ivHeroImage);
        ivTrainingBg       = view.findViewById(R.id.ivTrainingBg);
        cardNavProfile     = view.findViewById(R.id.cardNavProfile);
        btnStartRun        = view.findViewById(R.id.btnStartRun);
        tvDailyGoal        = view.findViewById(R.id.tvDailyGoal);
        tvWeeklyDistance   = view.findViewById(R.id.tvWeeklyDistance);
        tvDistanceChange   = view.findViewById(R.id.tvDistanceChange);
        tvAvgPace          = view.findViewById(R.id.tvAvgPace);
        tvAvgHR            = view.findViewById(R.id.tvAvgHR);
        tvActivityName     = view.findViewById(R.id.tvActivityName);
        tvActivityMeta     = view.findViewById(R.id.tvActivityMeta);
        tvViewAll          = view.findViewById(R.id.tvViewAll);
        cardTraining       = view.findViewById(R.id.cardTraining);
        cardLatestActivity = view.findViewById(R.id.cardLatestActivity);
        navRun             = view.findViewById(R.id.navRun);
        navPlans           = view.findViewById(R.id.navPlans);
        navHistory         = view.findViewById(R.id.navHistory);
    }

    // ── Hero photo ────────────────────────────────────────

    private void loadHeroPhoto() {
        if (ivHeroImage == null) return;

        // ✅ Load beautiful running photo from Unsplash
        Picasso.get()
                .load(HERO_IMAGE_URL)
                .placeholder(android.R.color.black)  // black while loading
                .error(android.R.color.black)         // black if no internet
                .into(ivHeroImage);

        // Same photo at lower opacity for training card
        if (ivTrainingBg != null) {
            Picasso.get()
                    .load(HERO_IMAGE_URL)
                    .placeholder(android.R.color.black)
                    .error(android.R.color.black)
                    .into(ivTrainingBg);
        }
    }

    // ── Navigation ────────────────────────────────────────

    private void setupNavigation() {
        btnStartRun.setOnClickListener(v -> go(new RunFragment()));
        cardNavProfile.setOnClickListener(v -> go(new UserProfileFragment()));
        tvViewAll.setOnClickListener(v -> go(new RunListFragment()));
        cardLatestActivity.setOnClickListener(v -> go(new RunListFragment()));
        cardTraining.setOnClickListener(v -> go(new PlansFragment()));
        navRun.setOnClickListener(v -> go(new RunFragment()));
        navPlans.setOnClickListener(v -> go(new PlansFragment()));
        navHistory.setOnClickListener(v -> go(new RunListFragment()));
    }

    private void go(Fragment dest) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameLayout, dest)
                .addToBackStack(null)
                .commit();
    }

    // ── Profile image ─────────────────────────────────────

    private void loadUserProfileImage() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();
        db.collection("Users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (!isAdded() || getContext() == null) return;
                    if (imgNavProfile == null) return;
                    if (doc.exists()) {
                        User user = doc.toObject(User.class);
                        if (user != null && user.getImgpro() != null
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
                        Log.e("AdminFragment", "Profile image error", e));
    }

    // ── Weekly stats ──────────────────────────────────────

    private void loadWeeklyStats() {
        fbs.getUserRuns(task -> {
            if (!isAdded() || getContext() == null) return;
            if (!task.isSuccessful() || task.getResult() == null) return;

            double totalKm = 0, totalPace = 0, totalBpm = 0;
            int    count   = 0;
            RunItem latest = null;

            for (QueryDocumentSnapshot doc : task.getResult()) {
                RunItem run = doc.toObject(RunItem.class);
                run.setId(doc.getId());

                try { if (run.getDistance() != null)
                    totalKm += Double.parseDouble(run.getDistance()); }
                catch (Exception ignored) {}

                try { if (run.getAvgpace() != null && !run.getAvgpace().isEmpty())
                    totalPace += Utils.timeStringToSeconds(run.getAvgpace()); }
                catch (Exception ignored) {}

                try { if (run.getBPM() != null)
                    totalBpm += Double.parseDouble(run.getBPM()); }
                catch (Exception ignored) {}

                if (latest == null) latest = run;
                count++;
            }

            // Distance
            tvWeeklyDistance.setText(String.format("%.1f", totalKm));
            tvDailyGoal.setText("TOTAL: " + String.format("%.1f", totalKm) + " KM");
            tvDistanceChange.setText(count > 0 ? count + " runs" : "");

            // Pace
            if (count > 0) {
                long pSec = (long)(totalPace / count);
                tvAvgPace.setText(Utils.secondsToTimeString(pSec) + "\"");
                tvAvgHR.setText(String.valueOf((int)(totalBpm / count)));
            } else {
                tvAvgPace.setText("--:--");
                tvAvgHR.setText("--");
            }

            // Latest run
            if (latest != null) {
                tvActivityName.setText(
                        "RUN · " + Utils.formatDisplayDate(latest.getDate()));
                tvActivityMeta.setText(
                        latest.getDistance() + " KM  ·  " + latest.getTime());
                final RunItem fin = latest;
                cardLatestActivity.setOnClickListener(v ->
                        go(RunDetailsFragment.newInstance(fin)));
            }
        });
    }
}