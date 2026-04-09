package com.hoiaebtl.antispam_call_android.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hoiaebtl.antispam_call_android.data.database.AppDatabase;
import com.hoiaebtl.antispam_call_android.data.entity.PersonalList;
import com.hoiaebtl.antispam_call_android.data.entity.SpamNumber;
import com.hoiaebtl.antispam_call_android.databinding.FragmentBlacklistBinding;

import java.util.ArrayList;
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

        adapter.setOnDeleteClickListener(item -> {
            executorService.execute(() -> {
                AppDatabase db = AppDatabase.getInstance(requireContext());
                if (item instanceof PersonalList) {
                    db.personalListDao().delete(((PersonalList) item).phone_number);
                } else if (item instanceof SpamNumber) {
                    db.spamNumberDao().delete((SpamNumber) item);
                }
                loadBlacklist();
            });
        });
    }

    private void loadBlacklist() {
        executorService.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(requireContext());
                // Lấy cả danh sách cá nhân và danh sách cộng đồng đã lưu
                List<PersonalList> personal = db.personalListDao().getUserList(1);
                List<SpamNumber> global = db.spamNumberDao().getAllSpamNumbers();
                
                List<Object> combinedList = new ArrayList<>();
                combinedList.addAll(personal);
                combinedList.addAll(global);
                
                requireActivity().runOnUiThread(() -> {
                    if (binding != null) {
                        if (combinedList.isEmpty()) {
                            binding.rvBlacklist.setVisibility(View.GONE);
                            binding.tvEmptyBlacklist.setVisibility(View.VISIBLE);
                        } else {
                            binding.rvBlacklist.setVisibility(View.VISIBLE);
                            binding.tvEmptyBlacklist.setVisibility(View.GONE);
                            adapter.setItems(combinedList);
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
        builder.setTitle("Thêm số vào danh sách chặn");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 0);

        final EditText inputPhone = new EditText(getContext());
        inputPhone.setHint("Số điện thoại");
        inputPhone.setInputType(InputType.TYPE_CLASS_PHONE);
        layout.addView(inputPhone);

        final EditText inputNote = new EditText(getContext());
        inputNote.setHint("Ghi chú (ví dụ: Tiếp thị nhà đất)");
        layout.addView(inputNote);

        builder.setView(layout);

        builder.setPositiveButton("Chặn", (dialog, which) -> {
            String phone = inputPhone.getText().toString().trim();
            String note = inputNote.getText().toString().trim();
            if (!phone.isEmpty()) {
                addNumberToPersonalList(phone, note);
            } else {
                Toast.makeText(getContext(), "Vui lòng nhập số điện thoại", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void addNumberToPersonalList(String phone, String note) {
        executorService.execute(() -> {
            try {
                PersonalList personal = new PersonalList();
                personal.phone_number = phone;
                personal.user_id = 1;
                personal.note = note.isEmpty() ? "Chặn thủ công" : note;
                personal.list_type = "BLACKLIST";
                personal.created_at = java.text.DateFormat.getDateTimeInstance().format(new java.util.Date());
                
                AppDatabase.getInstance(requireContext()).personalListDao().insert(personal);
                loadBlacklist();
                
                requireActivity().runOnUiThread(() -> 
                    Toast.makeText(getContext(), "Đã thêm " + phone + " vào danh sách cá nhân", Toast.LENGTH_SHORT).show());
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
        executorService.shutdown();
    }
}