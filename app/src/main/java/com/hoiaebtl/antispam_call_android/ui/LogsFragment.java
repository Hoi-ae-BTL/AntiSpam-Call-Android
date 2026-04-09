package com.hoiaebtl.antispam_call_android.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hoiaebtl.antispam_call_android.data.database.AppDatabase;
import com.hoiaebtl.antispam_call_android.data.entity.CallLog;
import com.hoiaebtl.antispam_call_android.databinding.FragmentLogsBinding;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LogsFragment extends Fragment {

    private static final String TAG = "LogsFragment";
    private FragmentLogsBinding binding;
    private LogsAdapter adapter;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLogsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        loadLogs();
    }

    private void setupRecyclerView() {
        adapter = new LogsAdapter();
        binding.rvCallLogs.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvCallLogs.setAdapter(adapter);
    }

    private void loadLogs() {
        executorService.execute(() -> {
            try {
                // Giả định user_id = 1 cho phiên bản đơn giản
                List<CallLog> logs = AppDatabase.getInstance(requireContext()).callLogDao().getUserLogs(1);
                
                requireActivity().runOnUiThread(() -> {
                    if (binding != null) {
                        adapter.setItems(logs);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi tải nhật ký: " + e.getMessage());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
