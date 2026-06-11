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
import java.util.Collections;

public class RunListFragment extends Fragment {

    private RecyclerView     recyclerView;
    private SearchView       searchView;
    private RunListAdapter   adapter;
    private FirebaseServices fbs;
    private View             layoutEmpty;

    private final ArrayList<RunItem> runList      = new ArrayList<>();
    private final ArrayList<RunItem> filteredList = new ArrayList<>();

    public RunListFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_run_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fbs = FirebaseServices.getInstance();

        recyclerView = view.findViewById(R.id.rvRunlist);
        searchView   = view.findViewById(R.id.srchViewRun);
        layoutEmpty  = view.findViewById(R.id.layoutEmpty);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        adapter = new RunListAdapter(runList);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(position -> {
            RunItem clicked = runList.get(position);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frameLayout,
                            RunDetailsFragment.newInstance(clicked))
                    .addToBackStack(null)
                    .commit();
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String q) { applyFilter(q); return true; }
            @Override public boolean onQueryTextChange(String q) { applyFilter(q); return true; }
        });

        loadRuns();
    }

    // ✅ Reload every time the screen becomes visible
    // so runs saved from RunFragment appear immediately
    @Override
    public void onResume() {
        super.onResume();
        loadRuns();
    }

    private void loadRuns() {
        fbs.getUserRuns(task -> {
            if (!isAdded() || getContext() == null) return;

            if (task.isSuccessful() && task.getResult() != null) {
                runList.clear();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    RunItem run = doc.toObject(RunItem.class);
                    run.setId(doc.getId());
                    runList.add(run);
                }

                // ✅ Sort by date descending in Java (newest first)
                // avoids needing a Firestore composite index
                Collections.sort(runList, (a, b) -> {
                    String dateA = a.getDate() != null ? a.getDate() : "";
                    String dateB = b.getDate() != null ? b.getDate() : "";
                    return dateB.compareTo(dateA); // descending
                });

                adapter.notifyDataSetChanged();

                // ✅ Show/hide empty state correctly
                if (layoutEmpty != null) {
                    layoutEmpty.setVisibility(
                            runList.isEmpty() ? View.VISIBLE : View.GONE);
                }
                recyclerView.setVisibility(
                        runList.isEmpty() ? View.GONE : View.VISIBLE);
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
        for (RunItem r : runList) {
            String d = r.getDistance() != null ? r.getDistance() : "";
            String t = r.getTime()     != null ? r.getTime()     : "";
            String c = r.getCalories() != null ? r.getCalories() : "";
            if (d.toLowerCase().contains(lower)
                    || t.toLowerCase().contains(lower)
                    || c.toLowerCase().contains(lower))
                filteredList.add(r);
        }
        adapter.setRuns(filteredList);
    }
}