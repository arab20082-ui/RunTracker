package com.example.myapplication;

public class RunItem {

    private String id;
    private String userId;
    private String date;
    private String distance;
    private String time;
    private String calories;
    private String streak;
    private String avgpace;
    private String BPM;

    public RunItem() {}

    public RunItem(String distance, String time, String calories,
                   String streak, String avgpace, String BPM,
                   String date, String userId) {
        this.distance = distance;
        this.time     = time;
        this.calories = calories;
        this.streak   = streak;
        this.avgpace  = avgpace;
        this.BPM      = BPM;
        this.date     = date;
        this.userId   = userId;
    }

    public String getId()       { return id; }
    public void setId(String id){ this.id = id; }

    public String getUserId()            { return userId; }
    public void setUserId(String uid)    { this.userId = uid; }

    public String getDate()              { return date; }
    public void setDate(String date)     { this.date = date; }

    public String getDistance()          { return distance; }
    public void setDistance(String d)    { this.distance = d; }

    public String getTime()              { return time; }
    public void setTime(String t)        { this.time = t; }

    public String getCalories()          { return calories; }
    public void setCalories(String c)    { this.calories = c; }

    public String getStreak()            { return streak; }
    public void setStreak(String s)      { this.streak = s; }

    public String getAvgpace()           { return avgpace; }
    public void setAvgpace(String p)     { this.avgpace = p; }

    public String getBPM()               { return BPM; }
    public void setBPM(String bpm)       { this.BPM = bpm; }
}