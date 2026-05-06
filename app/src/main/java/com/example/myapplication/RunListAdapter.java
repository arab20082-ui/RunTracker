package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RunListAdapter extends RecyclerView.Adapter<RunListAdapter.MyViewHolder> {

    Context context;
    ArrayList<RunItem> RunList;
    private OnItemClickListener itemClickListener;
    private FirebaseServices fbs;

    public RunListAdapter(Context context, ArrayList<RunItem> RunList) {
        this.context = context;
        this.RunList = RunList;
        this.fbs = FirebaseServices.getInstance();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        RunItem run = RunList.get(position);


        // ======== النصوص ===========
        holder.Distance.setText(run.getDistance());
        holder.Time.setText(run.getTime());
        holder.Calories.setText(run.getCalories());
        holder.Streak.setText(run.getStreak());







        // ======== النقر على العنصر ============
        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null)
                itemClickListener.onItemClick(position);
        });
    }

    @Override
    public int getItemCount() {
        return RunList.size();
    }

    // ========= المفضلة ========






    // ========= ViewHolder ===========
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView Distance, Time, Calories, Streak;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            Distance = itemView.findViewById(R.id.tvDistance);
            Time = itemView.findViewById(R.id.tvTime);
            Calories = itemView.findViewById(R.id.tvCalories);
            Streak = itemView.findViewById(R.id.tvStreak);

        }
    }

    // ========= Interface for onClick ========
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }
}
