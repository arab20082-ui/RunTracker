package com.example.myapplication;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class FirebaseServices {

    private static FirebaseServices instance;
    private final FirebaseAuth      auth;
    private final FirebaseFirestore db;

    private static final String USERS = "Users";
    private static final String RUNS  = "Runs";

    private FirebaseServices() {
        auth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();
    }

    public static FirebaseServices getInstance() {
        if (instance == null) instance = new FirebaseServices();
        return instance;
    }

    public FirebaseAuth      getAuth()         { return auth; }
    public FirebaseFirestore getFirestore()    { return db; }
    public FirebaseUser      getFirebaseUser() { return auth.getCurrentUser(); }

    public String getCurrentUid() {
        FirebaseUser u = auth.getCurrentUser();
        return u != null ? u.getUid() : null;
    }

    public void signOut() { auth.signOut(); }

    // ── User ──────────────────────────────────────────────

    public void getUserData(OnCompleteListener<DocumentSnapshot> l) {
        String uid = getCurrentUid();
        if (uid == null) return;
        db.collection(USERS).document(uid).get().addOnCompleteListener(l);
    }

    public void updateUser(User user, OnCompleteListener<Void> l) {
        String uid = getCurrentUid();
        if (uid == null) return;
        db.collection(USERS).document(uid).set(user).addOnCompleteListener(l);
    }

    public void createUser(User user, OnCompleteListener<Void> l) {
        updateUser(user, l);
    }

    // ── Runs ──────────────────────────────────────────────

    /**
     * ✅ FIXED: Removed .orderBy() to avoid requiring a Firestore composite index.
     * Sorting is done in Java after fetch instead.
     * The old query: .whereEqualTo("userId", uid).orderBy("date", DESCENDING)
     * silently returned empty results unless the index was manually created
     * in the Firebase console.
     */
    public void getUserRuns(OnCompleteListener<QuerySnapshot> l) {
        String uid = getCurrentUid();
        if (uid == null) return;
        db.collection(RUNS)
                .whereEqualTo("userId", uid)   // ✅ filter only — no orderBy
                .get()
                .addOnCompleteListener(l);
    }

    public void addRun(RunItem run, OnCompleteListener<Void> l) {
        String uid = getCurrentUid();
        if (uid == null) return;
        run.setUserId(uid);
        db.collection(RUNS).add(run).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String docId = task.getResult().getId();
                run.setId(docId);
                db.collection(RUNS)
                        .document(docId)
                        .update("id", docId)
                        .addOnCompleteListener(l);
            }
        });
    }

    public void deleteRun(String docId, OnCompleteListener<Void> l) {
        db.collection(RUNS).document(docId).delete().addOnCompleteListener(l);
    }

    public void updateRun(RunItem run, OnCompleteListener<Void> l) {
        if (run.getId() == null || run.getId().isEmpty()) return;
        db.collection(RUNS).document(run.getId()).set(run).addOnCompleteListener(l);
    }
}