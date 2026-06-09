package com.example.myapplication;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {

    private static Utils instance;

    public static Utils getInstance() {
        if (instance == null)
            instance = new Utils();
        return instance;
    }

    public static double timeStringToSeconds(String avgpace) {return 0;
    }

    public static char[] secondsToTimeString(long avgPaceSec) {return null ;
    }

    public interface UploadCallback {
        void onUploaded(String downloadUrl);
    }

    /**
     * Uploads an image to Firebase Storage and returns the download URL.
     */
    public void uploadImage(Context context, Uri imageUri, UploadCallback callback) {
        if (imageUri == null) {
            Toast.makeText(context, "Image URI is null", Toast.LENGTH_SHORT).show();
            return;
        }

        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference("AirCrafts/" + System.currentTimeMillis() + ".jpg");

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String downloadUrl = uri.toString();
                            callback.onUploaded(downloadUrl);
                        }).addOnFailureListener(e ->
                                Toast.makeText(context, "Error getting download URL: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        ))
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    /**
     * Formats a date string from yyyy-MM-dd to a more readable format like MMM dd, yyyy.
     */
    public static String formatDisplayDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return "";
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = inputFormat.parse(dateStr);
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            return date != null ? outputFormat.format(date) : dateStr;
        } catch (ParseException e) {
            return dateStr;
        }
    }
}
