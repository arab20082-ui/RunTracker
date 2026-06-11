package com.example.myapplication;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Utils {

    private Utils() {}

    // ── Date ──────────────────────────────────────────────

    public static String getTodayDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date());
    }

    public static String formatDisplayDate(String rawDate) {
        if (rawDate == null || rawDate.isEmpty()) return "";
        try {
            SimpleDateFormat in  = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat out = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            Date d = in.parse(rawDate);
            return d != null ? out.format(d) : rawDate;
        } catch (Exception e) { return rawDate; }
    }

    // ── Time ──────────────────────────────────────────────

    public static String secondsToTimeString(long totalSeconds) {
        long m = TimeUnit.SECONDS.toMinutes(totalSeconds);
        long s = totalSeconds - TimeUnit.MINUTES.toSeconds(m);
        return String.format(Locale.getDefault(), "%02d:%02d", m, s);
    }

    public static long timeStringToSeconds(String time) {
        if (time == null || time.isEmpty()) return 0;
        try {
            String clean = time.replaceAll("[^0-9:]", "");
            String[] parts = clean.split(":");
            if (parts.length == 2)
                return Long.parseLong(parts[0]) * 60 + Long.parseLong(parts[1]);
            if (parts.length == 3)
                return Long.parseLong(parts[0]) * 3600
                        + Long.parseLong(parts[1]) * 60
                        + Long.parseLong(parts[2]);
        } catch (Exception ignored) {}
        return 0;
    }

    public static String calculatePace(double distanceKm, long durationSeconds) {
        if (distanceKm <= 0 || durationSeconds <= 0) return "--:--";
        return secondsToTimeString((long)(durationSeconds / distanceKm));
    }

    // ── Calories ──────────────────────────────────────────

    /**
     * Weight-adjusted calorie estimate.
     * Formula: steps × 0.04 × (weightKg / 70)
     * Default weight 70kg if unknown → same as before.
     */
    public static int calculateCalories(int steps, double weightKg) {
        if (weightKg <= 0) weightKg = 70.0;
        return (int)(steps * 0.04 * (weightKg / 70.0));
    }

    /**
     * MET-based calorie estimate (more accurate when distance is known).
     * Uses MET ≈ 8.0 for running, standard formula:
     * kcal = MET × weightKg × durationHours
     */
    public static int calculateCaloriesMET(double distanceKm,
                                           long durationSeconds,
                                           double weightKg) {
        if (weightKg <= 0) weightKg = 70.0;
        if (durationSeconds <= 0 || distanceKm <= 0) return 0;
        double hours = durationSeconds / 3600.0;
        // MET scales with speed: ~6 for jogging, ~10 for fast running
        double speedKmh = distanceKm / hours;
        double met = speedKmh < 8 ? 6.0 : speedKmh < 12 ? 8.0 : 10.0;
        return (int)(met * weightKg * hours);
    }

    // ── Streak ────────────────────────────────────────────

    /**
     * Calculates the current consecutive-day streak from a list of run dates.
     * Dates must be in "yyyy-MM-dd" format. List order doesn't matter.
     *
     * Rules:
     * - A streak is consecutive days with at least one run each day.
     * - Multiple runs on the same day count as one streak day.
     * - If the most recent run was today or yesterday, streak is active.
     * - Otherwise streak is 0.
     */
    public static int calculateStreak(List<String> runDates) {
        if (runDates == null || runDates.isEmpty()) return 0;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // Convert to Calendar days, deduplicate
        java.util.TreeSet<Long> days = new java.util.TreeSet<>(java.util.Collections.reverseOrder());
        for (String dateStr : runDates) {
            try {
                Date d = sdf.parse(dateStr);
                if (d == null) continue;
                Calendar cal = Calendar.getInstance();
                cal.setTime(d);
                // Normalize to midnight
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                days.add(cal.getTimeInMillis());
            } catch (Exception ignored) {}
        }

        if (days.isEmpty()) return 0;

        // Check if streak is still active (most recent run today or yesterday)
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        long todayMs     = today.getTimeInMillis();
        long yesterdayMs = todayMs - TimeUnit.DAYS.toMillis(1);

        long mostRecent = days.first();
        if (mostRecent != todayMs && mostRecent != yesterdayMs) return 0;

        // Count consecutive days backwards from most recent
        int streak = 0;
        long expected = mostRecent;
        for (long dayMs : days) {
            if (dayMs == expected) {
                streak++;
                expected -= TimeUnit.DAYS.toMillis(1);
            } else if (dayMs < expected) {
                break; // gap found
            }
            // dayMs > expected means duplicate date already accounted for
        }

        return streak;
    }

    // ── Validation ────────────────────────────────────────

    public static boolean isValidEmail(String email) {
        return email != null
                && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    // ── Network ───────────────────────────────────────────

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkCapabilities caps =
                cm.getNetworkCapabilities(cm.getActiveNetwork());
        return caps != null && (
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)     ||
                        caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
    }

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    // ── Image upload ──────────────────────────────────────

    public interface UploadCallback {
        void onUploaded(String downloadUrl);
        void onFailed(String errorMessage);
    }

    public static void uploadImage(Context context, Uri imageUri,
                                   UploadCallback callback) {
        if (imageUri == null) {
            if (callback != null) callback.onFailed("No image selected");
            return;
        }
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "unknown";
        String path = "profile_images/" + uid + "_"
                + System.currentTimeMillis() + ".jpg";
        StorageReference ref = FirebaseStorage.getInstance()
                .getReference().child(path);
        ref.putFile(imageUri)
                .addOnSuccessListener(snap ->
                        ref.getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    if (callback != null) callback.onUploaded(uri.toString());
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Error getting URL: "
                                            + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    if (callback != null) callback.onFailed(e.getMessage());
                                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Upload failed: "
                            + e.getMessage(), Toast.LENGTH_SHORT).show();
                    if (callback != null) callback.onFailed(e.getMessage());
                });
    }
}