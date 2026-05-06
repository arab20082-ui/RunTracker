package com.example.myapplication;

public class RunItem {
    private String distance;
    private String time;
    private String calories;
    private String streak;

    public RunItem() { }

    public RunItem(String distance, String time, String calories, String streak) {
        this.distance = distance;
        this.time = time;
        this.calories = calories;
        this.streak = streak;

    }

    public String getDistance() { return distance; }
    public String getTime() { return time; }
    public String getCalories() { return calories; }
    public String getStreak() { return streak; }




}
