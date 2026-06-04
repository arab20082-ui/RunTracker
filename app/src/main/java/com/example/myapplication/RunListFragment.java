package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class RunListFragment extends Fragment {

    private RecyclerView    recyclerView;
    private SearchView      searchView;
    private RunListAdapter  adapter;
    private FirebaseServices fbs;

    private final ArrayList<RunItem> runList      = new ArrayList<>();
    private final ArrayList<RunItem> filteredList = new ArrayList<>();

    public RunListFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_run_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
        loadRunFromFirebase();
    }

    private void init(View view) {
        fbs = FirebaseServices.getInstance();

        recyclerView = view.findViewById(R.id.rvRunlist);
        searchView   = view.findViewById(R.id.srchViewRun);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        adapter = new RunListAdapter(runList);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(position -> {
            RunItem clicked = runList.get(position);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frameLayout, RunDetailsFragment.newInstance(clicked))
                    .addToBackStack(null)
                    .commit();
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
        fbs.getUserRuns(task -> {
            if (!isAdded() || getContext() == null) return;

            if (task.isSuccessful() && task.getResult() != null) {
                runList.clear();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    RunItem run = doc.toObject(RunItem.class);
                    run.setId(doc.getId());
                    runList.add(run);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }
    private void applyFilter(String query) {
        if (query.trim().isEmpty()) {
            adapter.setRuns(runList);
            return;
        }
        filteredList.clear();
        String lower = query.toLowerCase();

        for (RunItem run : runList) {
            String dist     = run.getDistance() != null ? run.getDistance() : "";
            String time     = run.getTime()     != null ? run.getTime()     : "";
            String calories = run.getCalories() != null ? run.getCalories() : "";
            String streak   = run.getStreak()   != null ? run.getStreak()   : "";
            if (dist.toLowerCase().contains(lower)     ||
                    time.toLowerCase().contains(lower)     ||
                    calories.toLowerCase().contains(lower) ||
                    streak.toLowerCase().contains(lower)) {
                filteredList.add(run);
            }
        }
        adapter.setRuns(filteredList);
    }
}