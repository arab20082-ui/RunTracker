package com.example.myapplication;

public class RunApp {
    private String distance;
    private String time;
    private String calories;
    private String streak;

    public RunApp() {
        // Firestore requires empty constructor
    }

    public RunApp(String distance, String time, String calories, String streak) {
        this.distance = distance;
        this.time = time;
        this.calories = calories;
        this.streak = streak;
    }

    public String getDistance() {
        return distance;
    }

    public String getTime() {
        return time;
    }

    public String getCalories() {
        return calories;
    }

    public String getStreak() {
        return streak;
    }
}

