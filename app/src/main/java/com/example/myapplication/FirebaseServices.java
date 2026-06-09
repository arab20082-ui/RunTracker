package com.example.myapplication;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

public class FirebaseServices {

    private static FirebaseServices instance;

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private FirebaseFirestore firestore;

    private FirebaseServices() {
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    public static synchronized FirebaseServices getInstance() {
        if (instance == null) {
            instance = new FirebaseServices();
        }
        return instance;
    }

    public FirebaseAuth getAuth() { return auth; }
    public FirebaseDatabase getDatabase() { return database; }
    public FirebaseStorage getStorage() { return storage; }
    public FirebaseFirestore getFirestore() { return firestore; }


    public FirebaseUser getFirebaseUser() {
        return auth.getCurrentUser();
    }

    public void updateUser(User user, OnCompleteListener<Void> listener) {
        FirebaseUser firebaseUser = getFirebaseUser();
        if (firebaseUser != null) {
            firestore.collection("Users")
                    .document(firebaseUser.getUid())
                    .set(user)
                    .addOnCompleteListener(listener);
        }
    }

    public void createUser(User user, OnCompleteListener<Void> listener) {
        FirebaseUser firebaseUser = getFirebaseUser();
        if (firebaseUser != null) {
            firestore.collection("Users")
                    .document(firebaseUser.getUid())
                    .set(user)
                    .addOnCompleteListener(listener);
        }
    }


    public void getUserData(OnCompleteListener<DocumentSnapshot> listener) {
        FirebaseUser firebaseUser = getFirebaseUser();
        if (firebaseUser != null) {
            firestore.collection("Users") //
                    .document(firebaseUser.getUid())
                    .get()
                    .addOnCompleteListener(listener);
        }
    }

    public void getUserRuns(OnCompleteListener<QuerySnapshot> listener) {
        FirebaseUser firebaseUser = getFirebaseUser();
        if (firebaseUser != null) {
            firestore.collection("runs")
                    .whereEqualTo("userId", firebaseUser.getUid())
                    .get()
                    .addOnCompleteListener(listener);
        }
    }

    public void signOut() {
    }

    public String getCurrentUid() {return null;
    }
}

