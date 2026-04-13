package com.hoiaebtl.antispam_call_android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hoiaebtl.antispam_call_android.R;
import com.hoiaebtl.antispam_call_android.data.database.AppDatabase;
import com.hoiaebtl.antispam_call_android.data.entity.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AuthActivity extends AppCompatActivity {

    private static final String TAG = "AuthActivity";

    private LinearLayout layoutPhone, layoutOtp;
    private EditText etPhone, etOtp;
    private Button btnSendOtp, btnVerifyOtp;
    private TextView tvResend, tvError;
    private ProgressBar progressAuth;

    private FirebaseAuth mAuth;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    private String currentPhoneNumber = "";
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        mAuth = FirebaseAuth.getInstance();

        // Kiểm tra Auto Login
        if (mAuth.getCurrentUser() != null) {
            goToMainActivity();
            return;
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        layoutPhone = findViewById(R.id.layout_step_phone);
        layoutOtp = findViewById(R.id.layout_step_otp);
        etPhone = findViewById(R.id.et_phone_number);
        etOtp = findViewById(R.id.et_otp_code);
        btnSendOtp = findViewById(R.id.btn_send_otp);
        btnVerifyOtp = findViewById(R.id.btn_verify_otp);
        tvResend = findViewById(R.id.tv_resend_otp);
        tvError = findViewById(R.id.tv_auth_error);
        progressAuth = findViewById(R.id.progress_auth);
    }

    private void setupListeners() {
        btnSendOtp.setOnClickListener(v -> {
            String phone = etPhone.getText().toString().trim();
            if (phone.isEmpty()) {
                showError("Vui lòng nhập số điện thoại");
                return;
            }
            if (phone.startsWith("0")) {
                phone = phone.substring(1);
            }
            currentPhoneNumber = "+84" + phone;
            startPhoneNumberVerification(currentPhoneNumber);
        });

        btnVerifyOtp.setOnClickListener(v -> {
            String code = etOtp.getText().toString().trim();
            if (code.isEmpty() || code.length() < 6) {
                showError("Mã OTP không hợp lệ");
                return;
            }
            verifyPhoneNumberWithCode(mVerificationId, code);
        });

        tvResend.setOnClickListener(v -> {
            layoutOtp.setVisibility(View.GONE);
            layoutPhone.setVisibility(View.VISIBLE);
            tvError.setVisibility(View.GONE);
            etOtp.setText("");
        });
    }

    private void startPhoneNumberVerification(String phoneNumber) {
        showLoading(true);
        tvError.setVisibility(View.GONE);

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)       // Number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Auto-resolution timeout
                        .setActivity(this)                 // Activity for callback binding
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                    Log.d(TAG, "onVerificationCompleted:" + credential);
                    signInWithPhoneAuthCredential(credential);
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    Log.w(TAG, "onVerificationFailed", e);
                    showLoading(false);
                    showError("Lỗi gửi SMS: " + e.getMessage());
                }

                @Override
                public void onCodeSent(@NonNull String verificationId,
                                       @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    Log.d(TAG, "onCodeSent:" + verificationId);
                    mVerificationId = verificationId;
                    mResendToken = token;

                    showLoading(false);
                    layoutPhone.setVisibility(View.GONE);
                    layoutOtp.setVisibility(View.VISIBLE);
                }
            };

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        showLoading(true);
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = task.getResult().getUser();
                        Log.d(TAG, "Đăng nhập thành công UID: " + user.getUid());
                        saveUserToLocalDatabase(user);
                    } else {
                        showLoading(false);
                        Log.w(TAG, "Đăng nhập thất bại", task.getException());
                        if (task.getException() instanceof com.google.firebase.auth.FirebaseAuthInvalidCredentialsException) {
                            showError("Mã OTP không chính xác");
                        } else {
                            showError("Lỗi hệ thống khi đăng nhập");
                        }
                    }
                });
    }

    private void saveUserToLocalDatabase(FirebaseUser firebaseUser) {
        executorService.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                User user = db.userDao().getUserById(1);
                
                if (user == null) {
                    user = new User();
                    user.user_id = 1;
                    user.phone_number = firebaseUser.getPhoneNumber();
                    user.created_at = java.text.DateFormat.getDateTimeInstance().format(new java.util.Date());
                    db.userDao().insert(user);
                } else {
                    user.phone_number = firebaseUser.getPhoneNumber();
                    db.userDao().update(user);
                }
                
                Log.d(TAG, "Đã lưu User vào Room DB");
                
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(AuthActivity.this, "Xác thực thành công!", Toast.LENGTH_SHORT).show();
                    goToMainActivity();
                });
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi lưu Room DB: " + e.getMessage());
                runOnUiThread(() -> {
                    showLoading(false);
                    goToMainActivity();
                });
            }
        });
    }

    private void goToMainActivity() {
        startActivity(new Intent(AuthActivity.this, MainActivity.class));
        finish();
    }

    private void showLoading(boolean isLoading) {
        progressAuth.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSendOtp.setEnabled(!isLoading);
        btnVerifyOtp.setEnabled(!isLoading);
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
