package com.hoiaebtl.antispam_call_android.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import android.app.role.RoleManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.work.PeriodicWorkRequest;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.ExistingPeriodicWorkPolicy;
import java.util.concurrent.TimeUnit;
import com.hoiaebtl.antispam_call_android.data.worker.SpamSyncWorker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.hoiaebtl.antispam_call_android.R;
import com.hoiaebtl.antispam_call_android.core.CallListenService;
import com.hoiaebtl.antispam_call_android.data.database.DatabaseSeeder;
import com.hoiaebtl.antispam_call_android.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Kiểm tra phiên đăng nhập (Bảo mật 2 lớp)
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }
        
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Seed database with initial data
        DatabaseSeeder.seedIfNeeded(this);

        // Hiển thị HomeFragment mặc định khi mới mở app
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        // Xử lý sự kiện click trên Bottom Navigation
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Fragment selectedFragment = null;

            if (itemId == R.id.navigation_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.navigation_blacklist) {
                selectedFragment = new BlacklistFragment();
            } else if (itemId == R.id.navigation_logs) {
                selectedFragment = new LogsFragment();
            } else if (itemId == R.id.navigation_dialer) {
                selectedFragment = new DialerFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }
            return false;
        });

        checkAndRequestPermissions();
        scheduleBackgroundSync();
    }

    private void scheduleBackgroundSync() {
        // Đồng bộ Firebase về Local DB mỗi 12 tiếng
        PeriodicWorkRequest syncRequest = new PeriodicWorkRequest.Builder(SpamSyncWorker.class, 12, TimeUnit.HOURS)
                .build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork("SpamSync", ExistingPeriodicWorkPolicy.KEEP, syncRequest);

        // [MỚI FIX] Buộc đồng bộ NGAY LẬP TỨC lần đầu mở app thay vì đợi 12 tiếng!
        OneTimeWorkRequest immediateSync = new OneTimeWorkRequest.Builder(SpamSyncWorker.class).build();
        WorkManager.getInstance(this).enqueue(immediateSync);
    }

    private void loadFragment(Fragment fragment) {
        try {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment, fragment)
                    .setReorderingAllowed(true)
                    .commit();
            Log.d("MainActivity", "Đã chuyển sang Fragment: " + fragment.getClass().getSimpleName());
        } catch (Exception e) {
            Log.e("MainActivity", "Lỗi chuyển Fragment: " + e.getMessage());
        }
    }

    private void checkAndRequestPermissions() {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            permissions = new String[]{
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_CALL_LOG,
                    Manifest.permission.ANSWER_PHONE_CALLS,
                    Manifest.permission.READ_CONTACTS
            };
        } else {
            permissions = new String[]{
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_CALL_LOG,
                    Manifest.permission.READ_CONTACTS
            };
        }

        boolean needRequest = false;
        for (String p : permissions) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                needRequest = true;
                break;
            }
        }

        if (needRequest) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        } else {
            startCoreService();
            requestCallScreeningRole();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }
    }

    private void requestCallScreeningRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            RoleManager roleManager = (RoleManager) getSystemService(ROLE_SERVICE);
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
                if (!roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
                    Log.d("MainActivity", "Yêu cầu quyền Call Screening (Native Chặn Gọi)");
                    Intent intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING);
                    startActivityForResult(intent, 200);
                }
            }
        }
    }

    private void startCoreService() {
        Intent serviceIntent = new Intent(this, CallListenService.class);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Không thể khởi chạy Service: " + e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                startCoreService();
                requestCallScreeningRole();
            }
        }
    }
}
