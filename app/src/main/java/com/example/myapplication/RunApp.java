package com.example.myapplication;

import android.app.Application;
import com.google.firebase.FirebaseApp;

public class RunApp extends Application {

    private static RunApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        FirebaseApp.initializeApp(this);
    }

    public static RunApp getInstance() { return instance; }
}