package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RunListAdapter extends RecyclerView.Adapter<RunListAdapter.MyViewHolder> {

    private final ArrayList<RunItem> runList;
    private OnItemClickListener itemClickListener;

    public RunListAdapter(ArrayList<RunItem> runList) {
        this.runList = runList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.data_row, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        RunItem run = runList.get(position);

        holder.tvDistance.setText(run.getDistance() + " km");
        holder.tvTime.setText(run.getTime());
        holder.tvCalories.setText(run.getCalories() + " kcal");
        holder.tvStreak.setText(run.getStreak() + " days");
        holder.tvDate.setText(Utils.formatDisplayDate(run.getDate()));

        String pace = run.getAvgpace();
        holder.tvPace.setText((pace != null && !pace.isEmpty()) ? pace : "--:--");

        String bpm = run.getBPM();
        holder.tvBPM.setText((bpm != null && !bpm.isEmpty()) ? bpm + " bpm" : "--");

        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (itemClickListener != null && pos != RecyclerView.NO_ID)
                itemClickListener.onItemClick(pos);
        });
    }

    @Override
    public int getItemCount() { return runList.size(); }

    public void setRuns(ArrayList<RunItem> newRuns) {
        runList.clear();
        runList.addAll(newRuns);
        notifyDataSetChanged();
    }

    public RunItem getRunAt(int position) { return runList.get(position); }

    public void removeAt(int position) {
        runList.remove(position);
        notifyItemRemoved(position);
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        this.itemClickListener = l;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvDistance, tvTime, tvCalories, tvStreak, tvPace, tvBPM, tvDate;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDistance = itemView.findViewById(R.id.tvDistanceRow);
            tvTime     = itemView.findViewById(R.id.tvTimeRow);
            tvCalories = itemView.findViewById(R.id.tvCaloriesRow);
            tvStreak   = itemView.findViewById(R.id.tvStreakRow);
            tvPace     = itemView.findViewById(R.id.tvPaceRow);
            tvBPM      = itemView.findViewById(R.id.tvBPMRow);
            tvDate     = itemView.findViewById(R.id.tvDateRow);
        }
    }
}