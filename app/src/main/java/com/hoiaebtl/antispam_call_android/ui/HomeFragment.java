package com.hoiaebtl.antispam_call_android.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.hoiaebtl.antispam_call_android.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefs = requireActivity().getSharedPreferences("SafeCallPrefs", Context.MODE_PRIVATE);

        // Đọc trạng thái hiện tại từ SharedPreferences
        boolean isAutoBlockEnabled = prefs.getBoolean("auto_block", false);
        binding.switchAutoBlock.setChecked(isAutoBlockEnabled);

        // Xử lý khi người dùng thay đổi trạng thái Switch
        binding.switchAutoBlock.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("auto_block", isChecked).apply();
            updateStatusUI(isChecked);
        });

        updateStatusUI(isAutoBlockEnabled);
    }

    private void updateStatusUI(boolean isEnabled) {
        if (isEnabled) {
            binding.tvStatusTitle.setText("Hệ thống bảo vệ đang BẬT");
            binding.tvStatusDesc.setText("Các cuộc gọi từ danh sách đen sẽ bị chặn tự động.");
            binding.imgShield.setColorFilter(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            binding.tvStatusTitle.setText("Tự động chặn đang TẮT");
            binding.tvStatusDesc.setText("Hệ thống sẽ chỉ cảnh báo, không tự động chặn cuộc gọi.");
            binding.imgShield.setColorFilter(getResources().getColor(android.R.color.darker_gray));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroy();
        binding = null;
    }
}