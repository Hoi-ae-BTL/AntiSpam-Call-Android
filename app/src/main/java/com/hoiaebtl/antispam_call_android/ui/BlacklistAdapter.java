package com.hoiaebtl.antispam_call_android.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.hoiaebtl.antispam_call_android.data.entity.PersonalList;
import com.hoiaebtl.antispam_call_android.data.entity.SpamNumber;
import com.hoiaebtl.antispam_call_android.databinding.ItemBlacklistBinding;
import java.util.ArrayList;
import java.util.List;

public class BlacklistAdapter extends RecyclerView.Adapter<BlacklistAdapter.ViewHolder> {

    private List<Object> items = new ArrayList<>();
    private OnDeleteClickListener deleteClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(Object item);
    }

    public void setItems(List<Object> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBlacklistBinding binding = ItemBlacklistBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Object item = items.get(position);
        
        if (item instanceof PersonalList) {
            PersonalList p = (PersonalList) item;
            holder.binding.tvBlacklistNumber.setText(p.phone_number);
            holder.binding.tvBlacklistNote.setText("Cá nhân: " + (p.note != null ? p.note : "Không có ghi chú"));
        } else if (item instanceof SpamNumber) {
            SpamNumber s = (SpamNumber) item;
            holder.binding.tvBlacklistNumber.setText(s.phone_number);
            holder.binding.tvBlacklistNote.setText("Cộng đồng (Báo cáo: " + s.total_reports + " lần)");
        }
        
        holder.binding.btnDeleteBlacklist.setOnClickListener(v -> {
            if (deleteClickListener != null) {
                deleteClickListener.onDeleteClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemBlacklistBinding binding;
        ViewHolder(ItemBlacklistBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}