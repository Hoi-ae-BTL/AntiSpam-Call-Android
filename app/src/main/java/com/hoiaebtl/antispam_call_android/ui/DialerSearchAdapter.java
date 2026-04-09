package com.hoiaebtl.antispam_call_android.ui;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hoiaebtl.antispam_call_android.R;

import java.util.ArrayList;
import java.util.List;

public class DialerSearchAdapter extends RecyclerView.Adapter<DialerSearchAdapter.ViewHolder> {

    public static class SearchResult {
        public String phone;
        public String name;
        public String label;
        public boolean isSpam;
        
        public SearchResult(String phone, String name, String label, boolean isSpam) {
            this.phone = phone;
            this.name = name;
            this.label = label;
            this.isSpam = isSpam;
        }
    }

    private List<SearchResult> results = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String phone);
    }

    public DialerSearchAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setResults(List<SearchResult> newResults) {
        this.results = newResults;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dialer_search, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SearchResult result = results.get(position);
        holder.tvName.setText(result.name);
        holder.tvNumber.setText(result.phone);
        holder.tvBadge.setText(result.label);

        if (result.isSpam) {
            holder.ivIcon.setImageResource(android.R.drawable.ic_dialog_alert);
            holder.ivIcon.setColorFilter(Color.parseColor("#E53935"));
            holder.tvBadge.setBackgroundColor(Color.parseColor("#FFCDD2"));
            holder.tvBadge.setTextColor(Color.parseColor("#B71C1C"));
            holder.tvName.setTextColor(Color.parseColor("#E53935"));
        } else {
            holder.ivIcon.setImageResource(android.R.drawable.ic_menu_info_details);
            holder.ivIcon.setColorFilter(Color.parseColor("#4CAF50"));
            holder.tvBadge.setBackgroundColor(Color.parseColor("#E8F5E9"));
            holder.tvBadge.setTextColor(Color.parseColor("#388E3C"));
            holder.tvName.setTextColor(Color.parseColor("#212121"));
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(result.phone));
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvNumber, tvBadge;
        ImageView ivIcon;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_dial_name);
            tvNumber = itemView.findViewById(R.id.tv_dial_number);
            tvBadge = itemView.findViewById(R.id.tv_dial_badge);
            ivIcon = itemView.findViewById(R.id.iv_dial_icon);
        }
    }
}
