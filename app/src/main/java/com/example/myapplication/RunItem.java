package com.example.myapplication;

public class RunItem {

    private String id;        // ✅ Firestore document ID
    private String userId;    // ✅ owner UID for filtering
    private String date;      // ✅ "yyyy-MM-dd" for sorting
    private String distance;
    private String time;
    private String calories;
    private String streak;
    private String avgpace;
    private String BPM;       // ✅ consistent casing with all other classes

    // ✅ Required empty constructor for Firestore deserialization
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

    // Getters
    public String getId()       { return id; }
    public String getUserId()   { return userId; }
    public String getDate()     { return date; }
    public String getDistance() { return distance; }
    public String getTime()     { return time; }
    public String getCalories() { return calories; }
    public String getStreak()   { return streak; }
    public String getAvgpace()  { return avgpace; }
    public String getBPM()      { return BPM; }

    // ✅ Setters — required for Firestore toObject()
    public void setId(String id)             { this.id = id; }
    public void setUserId(String userId)     { this.userId = userId; }
    public void setDate(String date)         { this.date = date; }
    public void setDistance(String distance) { this.distance = distance; }
    public void setTime(String time)         { this.time = time; }
    public void setCalories(String calories) { this.calories = calories; }
    public void setStreak(String streak)     { this.streak = streak; }
    public void setAvgpace(String avgpace)   { this.avgpace = avgpace; }
    public void setBPM(String BPM)           { this.BPM = BPM; }
}