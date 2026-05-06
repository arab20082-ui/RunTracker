package com.example.myapplication;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class AdminFragment extends Fragment {

    private Button btnPage1, btnPage2;

    public AdminFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_admin2, container, false);

        // ربط الأزرار
        btnPage1 = view.findViewById(R.id.btnPage1);
        btnPage2 = view.findViewById(R.id.btnPage2);

        // زر الصفحة الأولى
        btnPage1.setOnClickListener(v -> {
            FragmentTransaction ft = getParentFragmentManager().beginTransaction();
            ft.replace(R.id.frameLayout, new RunFragment());
            ft.addToBackStack(null); // يتيح الرجوع
            ft.commit();
        });

        // زر الصفحة الثانية
        btnPage2.setOnClickListener(v -> {
            FragmentTransaction ft = getParentFragmentManager().beginTransaction();
            ft.replace(R.id.frameLayout, new RunListFragment());
            ft.addToBackStack(null);
            ft.commit();
        });

        return view;
    }
}
