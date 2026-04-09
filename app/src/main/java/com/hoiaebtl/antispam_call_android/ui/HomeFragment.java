package com.hoiaebtl.antispam_call_android.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hoiaebtl.antispam_call_android.R;
import com.hoiaebtl.antispam_call_android.data.database.AppDatabase;
import com.hoiaebtl.antispam_call_android.data.entity.PersonalList;
import com.hoiaebtl.antispam_call_android.data.entity.SpamNumber;
import com.hoiaebtl.antispam_call_android.databinding.FragmentHomeBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private SharedPreferences prefs;
    private FirebaseFirestore db;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                checkAllPermissions();
            });

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
        db = FirebaseFirestore.getInstance();

        setupUI();
        checkAllPermissions();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkAllPermissions();
    }

    private void setupUI() {
        boolean isAutoBlockEnabled = prefs.getBoolean("auto_block", false);
        binding.switchAutoBlock.setChecked(isAutoBlockEnabled);
        
        binding.switchAutoBlock.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("auto_block", isChecked).apply();
            updateStatusUI();
        });

        binding.btnUpdatePermissions.setOnClickListener(v -> requestMissingPermissions());

        // Xử lý tra cứu
        binding.btnSearchFirebase.setOnClickListener(v -> {
            String phone = binding.etSearchPhone.getText().toString().trim();
            if (phone.isEmpty()) {
                binding.tilSearch.setError("Vui lòng nhập số điện thoại");
                return;
            }
            binding.tilSearch.setError(null);
            searchPhoneNumber(phone);
        });
    }

    private void searchPhoneNumber(String phone) {
        binding.btnSearchFirebase.setEnabled(false);
        binding.tvSearchResult.setVisibility(View.VISIBLE);
        binding.tvSearchResult.setText("Đang tra cứu...");
        binding.tvSearchResult.setBackgroundColor(Color.parseColor("#EEEEEE"));
        binding.tvSearchResult.setTextColor(Color.BLACK);

        // 1. Kiểm tra trong Database local trước (Room)
        executorService.execute(() -> {
            AppDatabase localDb = AppDatabase.getInstance(requireContext());
            PersonalList personal = localDb.personalListDao().findByPhone(phone);
            SpamNumber localSpam = localDb.spamNumberDao().findByPhone(phone);

            String localReason = null;
            if (personal != null) {
                localReason = "Danh sách chặn cá nhân: " + (personal.note != null ? personal.note : "Không có ghi chú");
            } else if (localSpam != null) {
                localReason = "Danh sách đen cộng đồng (đã lưu máy)";
            }

            final String finalLocalReason = localReason;
            
            // 2. Sau đó kiểm tra Firebase
            db.collection("spam_numbers").document(phone).get().addOnCompleteListener(task -> {
                if (binding == null) return;
                binding.btnSearchFirebase.setEnabled(true);

                if (finalLocalReason != null) {
                    // Ưu tiên kết quả local nếu có
                    showSearchResult("🚨 CẢNH BÁO: " + finalLocalReason, true);
                } else if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        String label = document.getString("label");
                        showSearchResult("🚨 CẢNH BÁO: Số này được báo cáo là \"" + (label != null ? label : "Lừa đảo") + "\" trên hệ thống cộng đồng!", true);
                    } else {
                        showSearchResult("✅ Số này hiện không nằm trong bất kỳ danh sách chặn nào.", false);
                    }
                } else {
                    showSearchResult("❌ Lỗi kết nối máy chủ. Vui lòng thử lại.", false);
                }
            });
        });
    }

    private void showSearchResult(String message, boolean isSpam) {
        requireActivity().runOnUiThread(() -> {
            if (binding == null) return;
            binding.tvSearchResult.setText(message);
            if (isSpam) {
                binding.tvSearchResult.setBackgroundColor(Color.parseColor("#FFEBEE"));
                binding.tvSearchResult.setTextColor(Color.parseColor("#B71C1C"));
            } else if (message.startsWith("✅")) {
                binding.tvSearchResult.setBackgroundColor(Color.parseColor("#E8F5E9"));
                binding.tvSearchResult.setTextColor(Color.parseColor("#1B5E20"));
            } else {
                binding.tvSearchResult.setBackgroundColor(Color.parseColor("#FFF3E0"));
                binding.tvSearchResult.setTextColor(Color.BLACK);
            }
        });
    }

    private void checkAllPermissions() {
        if (binding == null) return;

        Context context = requireContext();

        // 1. Kiểm tra quyền cuộc gọi
        boolean phoneState = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
        boolean answerCalls = ContextCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_GRANTED;
        boolean callLog = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED;
        boolean isPhoneOk = phoneState && answerCalls && callLog;
        
        updatePermStatusText(binding.tvStatusPhone, isPhoneOk);

        // 2. Kiểm tra quyền Overlay
        boolean isOverlayOk = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context);
        updatePermStatusText(binding.tvStatusOverlay, isOverlayOk);

        // Nút cập nhật quyền sẽ mờ đi nếu đã đủ hết
        boolean allGranted = isPhoneOk && isOverlayOk;
        binding.btnUpdatePermissions.setEnabled(!allGranted);
        binding.btnUpdatePermissions.setText(allGranted ? "Đã cấp đủ quyền hệ thống" : "Cập nhật quyền hệ thống");

        updateStatusUI();
    }

    private void updatePermStatusText(android.widget.TextView textView, boolean isGranted) {
        textView.setText(isGranted ? "Đã cấp ✅" : "Chưa cấp ❌");
        textView.setTextColor(isGranted ? Color.parseColor("#4CAF50") : Color.parseColor("#F44336"));
    }

    private void requestMissingPermissions() {
        List<String> missing = new ArrayList<>();
        
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            missing.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED) {
            missing.add(Manifest.permission.ANSWER_PHONE_CALLS);
        }
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            missing.add(Manifest.permission.READ_CALL_LOG);
        }

        if (!missing.isEmpty()) {
            requestPermissionLauncher.launch(missing.toArray(new String[0]));
        }

        // Xin quyền Overlay
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(requireContext())) {
            Toast.makeText(getContext(), "Vui lòng cho phép 'Hiển thị trên ứng dụng khác'", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + requireActivity().getPackageName()));
            startActivity(intent);
        }
        
        // Nếu nhấn mà vẫn không có gì hiện ra (đã xin rồi), mở Cài đặt ứng dụng
        if (missing.isEmpty() && (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(requireContext()))) {
            Toast.makeText(getContext(), "Đang mở cài đặt ứng dụng...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + requireActivity().getPackageName()));
            startActivity(intent);
        }
    }

    private void updateStatusUI() {
        if (binding == null) return;
        
        // Kiểm tra xem đã đủ quyền chưa
        boolean phoneState = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
        boolean overlay = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(requireContext());
        boolean isAllReady = phoneState && overlay;
        boolean isAutoBlock = prefs.getBoolean("auto_block", false);

        if (isAllReady) {
            binding.layoutStatusBg.setBackgroundColor(Color.parseColor("#E8F5E9")); // Green
            binding.imgShield.setColorFilter(Color.parseColor("#2E7D32"));
            binding.imgShield.setImageResource(android.R.drawable.ic_dialog_info);
            binding.tvStatusTitle.setText(isAutoBlock ? "Đang tự động chặn" : "Đang bảo vệ");
            binding.tvStatusTitle.setTextColor(Color.parseColor("#1B5E20"));
            binding.tvStatusDesc.setText("Hệ thống đã sẵn sàng làm việc.");
            binding.tvStatusDesc.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            binding.layoutStatusBg.setBackgroundColor(Color.parseColor("#FFF3E0")); // Orange
            binding.imgShield.setColorFilter(Color.parseColor("#EF6C00"));
            binding.imgShield.setImageResource(android.R.drawable.ic_dialog_alert);
            binding.tvStatusTitle.setText("Chưa hoàn tất");
            binding.tvStatusTitle.setTextColor(Color.parseColor("#E65100"));
            binding.tvStatusDesc.setText("Vui lòng cấp đủ quyền hệ thống bên dưới.");
            binding.tvStatusDesc.setTextColor(Color.parseColor("#FB8C00"));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        executorService.shutdown();
    }
}