package com.hoiaebtl.antispam_call_android.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hoiaebtl.antispam_call_android.data.database.AppDatabase;
import com.hoiaebtl.antispam_call_android.databinding.FragmentStatsBinding;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StatsFragment extends Fragment {

    private FragmentStatsBinding binding;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStatsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadStatistics();
    }

    private void loadStatistics() {
        executorService.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            int totalBlocked = db.callLogDao().getTotalSpamCalls();
            
            // Giả lập dữ liệu phân loại (Trong thực tế sẽ query count group by category_id)
            // Vì hiện tại database chưa lưu category_id vào CallLog nên ta dùng dummy data
            
            requireActivity().runOnUiThread(() -> {
                if (binding != null) {
                    binding.tvTotalBlocked.setText(String.valueOf(totalBlocked));
                    
                    // Nếu totalBlocked > 0, ta có thể tính toán phần trăm cho các thanh Progress
                    // Tạm thời để các thanh progress như layout mẫu
                }
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
