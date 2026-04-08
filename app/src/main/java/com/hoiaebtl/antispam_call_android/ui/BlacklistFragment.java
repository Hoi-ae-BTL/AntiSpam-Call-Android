package com.hoiaebtl.antispam_call_android.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hoiaebtl.antispam_call_android.data.database.AppDatabase;
import com.hoiaebtl.antispam_call_android.data.entity.SpamNumber;
import com.hoiaebtl.antispam_call_android.databinding.FragmentBlacklistBinding;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BlacklistFragment extends Fragment {

    private static final String TAG = "BlacklistFragment";
    private FragmentBlacklistBinding binding;
    private BlacklistAdapter adapter;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBlacklistBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        loadBlacklist();

        binding.fabAddBlacklist.setOnClickListener(v -> showAddNumberDialog());
    }

    private void setupRecyclerView() {
        adapter = new BlacklistAdapter();
        binding.rvBlacklist.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvBlacklist.setAdapter(adapter);

        adapter.setOnDeleteClickListener(spamNumber -> {
            executorService.execute(() -> {
                AppDatabase.getInstance(requireContext()).spamNumberDao().delete(spamNumber);
                loadBlacklist();
            });
        });
    }

    private void loadBlacklist() {
        executorService.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(requireContext());
                List<SpamNumber> list = db.spamNumberDao().getAllSpamNumbers();
                
                Log.d(TAG, "Đã tải danh sách chặn: " + list.size() + " số.");
                
                requireActivity().runOnUiThread(() -> {
                    if (binding != null) {
                        if (list.isEmpty()) {
                            binding.rvBlacklist.setVisibility(View.GONE);
                            // Hiển thị một TextView thông báo trống nếu cần
                        } else {
                            binding.rvBlacklist.setVisibility(View.VISIBLE);
                            adapter.setItems(list);
                        }
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi tải danh sách: " + e.getMessage());
            }
        });
    }

    private void showAddNumberDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Thêm số điện thoại chặn");

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_PHONE);
        input.setHint("Nhập số điện thoại");
        builder.setView(input);

        builder.setPositiveButton("Thêm", (dialog, which) -> {
            String number = input.getText().toString().trim();
            if (!number.isEmpty()) {
                addNumberToBlacklist(number);
            } else {
                Toast.makeText(getContext(), "Vui lòng nhập số điện thoại", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void addNumberToBlacklist(String number) {
        executorService.execute(() -> {
            try {
                SpamNumber spam = new SpamNumber();
                spam.phone_number = number;
                spam.primary_category_id = 1; // Mặc định Lừa đảo
                spam.total_reports = 1;
                spam.last_reported_at = java.text.DateFormat.getDateTimeInstance().format(new java.util.Date());
                
                AppDatabase.getInstance(requireContext()).spamNumberDao().insert(spam);
                loadBlacklist();
                
                requireActivity().runOnUiThread(() -> 
                    Toast.makeText(getContext(), "Đã thêm " + number + " vào danh sách chặn", Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> 
                    Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
