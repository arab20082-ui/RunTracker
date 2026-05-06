package com.example.myapplication;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;


public class RunListFragment extends Fragment {

    private RecyclerView recyclerView;
    private SearchView searchView;
    private RunListAdapter adapter;
    private FirebaseServices fbs;

    private ArrayList<RunItem> RunList = new ArrayList<>();
    private ArrayList<RunItem> filteredList = new ArrayList<>();

    public RunListFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_run_list, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        init();
        loadRunFromFirebase();
    }

    private void init() {
        recyclerView = getView().findViewById(R.id.rvRunlist);
        searchView = getView().findViewById(R.id.srchViewRun);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);

        fbs = FirebaseServices.getInstance();

        adapter = new RunListAdapter(getActivity(), RunList);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(position -> {
            Toast.makeText(getActivity(),
                    "Clicked: " + RunList.get(position).getDistance(),
                    Toast.LENGTH_SHORT).show();
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                applyFilter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                applyFilter(newText);
                return true;
            }
        });
    }

    private void loadRunFromFirebase() {
        fbs.getFirestore().collection("runs" )

                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        RunList.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            RunItem sm = doc.toObject(RunItem.class);
                            RunList.add(sm);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void applyFilter(String query) {
        if (query.trim().isEmpty()) {
            adapter = new RunListAdapter(getActivity(), RunList);
            recyclerView.setAdapter(adapter);
            return;
        }

        filteredList.clear();

        for (RunItem sm : RunList) {
            if (sm.getDistance().toLowerCase().contains(query.toLowerCase()) ||
                    sm.getTime().toLowerCase().contains(query.toLowerCase()) ||
                    sm.getCalories().toLowerCase().contains(query.toLowerCase()) ||
                    sm.getStreak().toLowerCase().contains(query.toLowerCase())
            ) {
                filteredList.add(sm);
            }
        }

        adapter = new RunListAdapter(getActivity(), filteredList);
        recyclerView.setAdapter(adapter);
    }
}