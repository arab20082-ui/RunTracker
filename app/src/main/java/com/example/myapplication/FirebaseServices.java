package com.example.myapplication;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

public class FirebaseServices {
    // 🔹 متغير ثابت لتخزين النسخة الوحيدة من الكلاس (Singleton)
    private static FirebaseServices instance;

    // 🔹 تعريف خدمات Firebase الأساسية
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private FirebaseFirestore firestore;

    // 🔹 مُنشئ خاص (Private) حتى لا يمكن إنشاء نسخة جديدة من الخارج
    private FirebaseServices() {
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    // 🔹 دالة getInstance ترجع نفس النسخة كل مرة
    public static FirebaseServices getInstance() {
        if (instance == null) {
            instance = new FirebaseServices();
        }
        return instance;
    }

    // 🔹 دالة ترجع كائن FirebaseAuth
    public FirebaseAuth getAuth() {
        return auth;
    }

    // 🔹 دالة ترجع كائن FirebaseDatabase (لو احتجته لاحقًا)
    public FirebaseDatabase getDatabase() {
        return database;
    }

    // 🔹 دالة ترجع كائن FirebaseStorage (لو احتجته لاحقًا)
    public FirebaseStorage getStorage() {
        return storage;
    }

    public FirebaseFirestore getFirestore() {
        return firestore;
    }
}
