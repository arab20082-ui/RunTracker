package com.example.myapplication;

import android.app.Application;

import com.google.firebase.FirebaseApp;

public class RunApp extends Application {

    private static RunApp instance;

    public RunApp(String distance, String time, String calories, String streak, String avgpace, String bpm) {
    }

    public RunApp(String distance, String time, String calories, String streak, String bpm) {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Store global application instance
        instance = this;

        // Initialize Firebase once for the entire app lifetime
        FirebaseApp.initializeApp(this);
    }

    /**
     * Returns the global Application instance.
     * Useful for getting context outside of Activities/Fragments.
     */
    public static RunApp getInstance() {
        return instance;
    }
}