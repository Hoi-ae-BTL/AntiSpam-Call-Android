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
            if (getContext() == null) return;
            AppDatabase db = AppDatabase.getInstance(requireContext());
            
            // 1. Lấy tổng số cuộc gọi bị chặn
            int totalBlocked = db.callLogDao().getTotalSpamCalls();
            
            // 2. Lấy số lượng theo từng loại (ID dựa trên DatabaseSeeder: 1-Lừa đảo, 2-Quảng cáo, 3-Mạo danh)
            int countFinScam = db.callLogDao().getSpamCountByCategory(1); 
            int countAds = db.callLogDao().getSpamCountByCategory(2);     
            int countGov = db.callLogDao().getSpamCountByCategory(3); 

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (binding != null) {
                        // Cập nhật tổng số
                        binding.tvTotalBlocked.setText(String.valueOf(totalBlocked));
                        
                        if (totalBlocked > 0) {
                            // Tính toán phần trăm
                            int pctFin = (countFinScam * 100) / totalBlocked;
                            int pctAds = (countAds * 100) / totalBlocked;
                            int pctGov = (countGov * 100) / totalBlocked;

                            // Cập nhật ProgressBars
                            binding.pbCat1.setProgress(pctFin);
                            binding.pbCat2.setProgress(pctAds);
                            binding.pbCat3.setProgress(pctGov);
                            
                            // Cập nhật text hiển thị chi tiết
                            binding.tvCat1Name.setText("Lừa đảo tài chính (" + pctFin + "%)");
                            binding.tvCat2Name.setText("Quảng cáo làm phiền (" + pctAds + "%)");
                            binding.tvCat3Name.setText("Mạo danh cơ quan chức năng (" + pctGov + "%)");
                        } else {
                            // Reset về 0 nếu chưa có dữ liệu
                            binding.pbCat1.setProgress(0);
                            binding.pbCat2.setProgress(0);
                            binding.pbCat3.setProgress(0);
                            binding.tvTotalBlocked.setText("0");
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
