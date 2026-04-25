package com.hoiaebtl.antispam_call_android.ui;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.hoiaebtl.antispam_call_android.R;
import com.hoiaebtl.antispam_call_android.data.entity.CallLog;
import com.hoiaebtl.antispam_call_android.databinding.ItemCallLogBinding;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LogsAdapter extends RecyclerView.Adapter<LogsAdapter.LogViewHolder> {

    private List<CallLog> items = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault());

    public void setItems(List<CallLog> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCallLogBinding binding = ItemCallLogBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new LogViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class LogViewHolder extends RecyclerView.ViewHolder {
        private final ItemCallLogBinding binding;

        public LogViewHolder(ItemCallLogBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(CallLog log) {
            binding.tvLogNumber.setText(log.getPhoneNumber());
            binding.tvLogTime.setText(dateFormat.format(new Date(log.getCallTime())));

            if (log.isSpam()) {
                binding.tvLogTag.setVisibility(View.VISIBLE);
                binding.tvLogTag.setText("Lừa đảo");
                binding.tvLogTag.setTextColor(Color.parseColor("#F44336"));
                binding.imgCallTypeIcon.setColorFilter(Color.parseColor("#F44336"));
            } else {
                binding.tvLogTag.setVisibility(View.GONE);
                binding.imgCallTypeIcon.setColorFilter(Color.parseColor("#4CAF50"));
            }
        }
    }
}
