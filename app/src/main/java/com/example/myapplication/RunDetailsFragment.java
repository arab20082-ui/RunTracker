package com.example.myapplication;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class RunDetailsFragment extends Fragment {

    // تعريف عناصر الواجهة (TextViews) لعرض التفاصيل
    private TextView tvDistanceDetail, tvTimeDetail, tvCaloriesDetail, tvStreakDetail, tvPaceDetail, tvBPMDetail;
    private Button btnBack;

    // مفاتيح استقبال البيانات من الـ Bundle
    private static final String ARG_DISTANCE = "distance";
    private static final String ARG_TIME = "time";
    private static final String ARG_CALORIES = "calories";
    private static final String ARG_STREAK = "streak";
    private static final String ARG_PACE = "pace";

    private static final String ARG_BPM = "BPM";
    private Bundle container;

    public RunDetailsFragment() {
        // Required empty public constructor
    }

    /**
     * دالة مصنعية (Factory Method) لإنشاء كائن من هذا الـ Fragment وتمرير البيانات له بأمان
     */
    public static RunDetailsFragment newInstance(RunItem runItem) {
        RunDetailsFragment fragment = new RunDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DISTANCE, runItem.getDistance());
        args.putString(ARG_TIME, runItem.getTime());
        args.putString(ARG_CALORIES, runItem.getCalories());
        args.putString(ARG_STREAK, runItem.getStreak());
        args.putString(ARG_BPM, runItem.getBPM());

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // نفخ واجهة التفاصيل (تأكد من إنشاء ملف XML بهذا الاسم)
        return inflater.inflate(R.layout.fragment_details, container, false);
    }

    @SuppressLint("WrongViewCast")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, container);

        // 1. ربط عناصر الواجهة من الـ XML
        tvDistanceDetail = view.findViewById(R.id.tvDistanceDetail);
        tvTimeDetail = view.findViewById(R.id.tvTimeDetail);
        tvCaloriesDetail = view.findViewById(R.id.tvCaloriesDetail);
        tvStreakDetail = view.findViewById(R.id.tvStreakDetail);
        tvPaceDetail = view.findViewById(R.id.tvPaceDetail);
        tvBPMDetail = view.findViewById(R.id.tvBPMDetail);
        btnBack = view.findViewById(R.id.btnBackDetail);

        // 2. استقبال وعرض البيانات القادمة في الـ Bundle
        if (getArguments() != null) {
            tvDistanceDetail.setText(getArguments().getString(ARG_DISTANCE, "0") + " KM");
            tvTimeDetail.setText(getArguments().getString(ARG_TIME, "00:00"));
            tvCaloriesDetail.setText(getArguments().getString(ARG_CALORIES, "0") + " kcal");
            tvStreakDetail.setText(getArguments().getString(ARG_STREAK, "0") + " Days");
            tvBPMDetail.setText(getArguments().getString(ARG_BPM, "0") + " BPM");


            String pace = getArguments().getString(ARG_PACE);
            tvPaceDetail.setText(pace != null && !pace.isEmpty() ? pace : "--:--");
        }

        // 3. زر العودة للخلف ونبذ الشاشة الحالية من الـ Stack
        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });
    }
}