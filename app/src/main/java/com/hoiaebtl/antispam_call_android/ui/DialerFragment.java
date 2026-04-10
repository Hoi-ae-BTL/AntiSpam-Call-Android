package com.hoiaebtl.antispam_call_android.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.hoiaebtl.antispam_call_android.R;

public class DialerFragment extends Fragment {

    private TextView tvDialedNumber;
    private StringBuilder currentNumber = new StringBuilder();
    private DialerSearchAdapter searchAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dialer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvDialedNumber = view.findViewById(R.id.tv_dialed_number);
        setupNumpad(view);
        
        RecyclerView rvResults = view.findViewById(R.id.rv_dialer_results);
        rvResults.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));
        searchAdapter = new DialerSearchAdapter(phone -> {
            currentNumber.setLength(0);
            currentNumber.append(phone);
            updateDisplay();
        });
        rvResults.setAdapter(searchAdapter);

        view.findViewById(R.id.btn_call).setOnClickListener(v -> {
            if (currentNumber.length() > 0) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + currentNumber.toString()));
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Vui lòng nhập số", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Theo dõi thay đổi số điện thoại để thực hiện tìm kiếm Smart Dialing
        tvDialedNumber.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                String num = s.toString();
                // TODO: Gọi hàm tìm kiếm nhanh danh bạ và Firebase ở đây (Smart Dial)
                searchSmartDial(num);
            }
        });
    }

    private void searchSmartDial(String query) {
        if (query.length() < 3) {
            searchAdapter.setResults(new java.util.ArrayList<>());
            return;
        }
        
        // Live Caller ID: Khi số bắt đầu có hình hài (>= 9 số), quét kho dữ liệu đám mây!
        if (query.length() >= 9) {
            String normalizedNumber = com.hoiaebtl.antispam_call_android.core.NumberNormalizer.normalize(query, "VN");
            new com.hoiaebtl.antispam_call_android.core.HybridSpamChecker(getContext())
                .checkCallerInfo(normalizedNumber, info -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            java.util.List<DialerSearchAdapter.SearchResult> list = new java.util.ArrayList<>();
                            if (info.isSpam) {
                                list.add(new DialerSearchAdapter.SearchResult(query, "CẢNH BÁO LỪA ĐẢO", info.label, true));
                            } else if (info.isVerifiedSafe && info.name != null) {
                                list.add(new DialerSearchAdapter.SearchResult(query, info.name, info.label, false));
                            } else {
                                list.add(new DialerSearchAdapter.SearchResult(query, "Không có thông tin", "Dữ liệu trống", false));
                            }
                            searchAdapter.setResults(list);
                        });
                    }
                });
        } else {
             // Với các độ dài ngắn, tìm trong danh bạ (Local Search giả lập)
             java.util.List<DialerSearchAdapter.SearchResult> list = new java.util.ArrayList<>();
             list.add(new DialerSearchAdapter.SearchResult(query, "Đang tra cứu nhanh...", "Chờ thêm số", false));
             searchAdapter.setResults(list);
        }
    }

    private void setupNumpad(View view) {
        int[] buttonIds = {
                R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4,
                R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9,
                R.id.btn_star, R.id.btn_hash
        };
        String[] buttonValues = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "*", "#"};

        for (int i = 0; i < buttonIds.length; i++) {
            final String val = buttonValues[i];
            Button btn = view.findViewById(buttonIds[i]);
            btn.setOnClickListener(v -> appendDigit(val));
        }

        ImageButton btnBackspace = view.findViewById(R.id.btn_backspace);
        btnBackspace.setOnClickListener(v -> {
            if (currentNumber.length() > 0) {
                currentNumber.deleteCharAt(currentNumber.length() - 1);
                updateDisplay();
            }
        });

        btnBackspace.setOnLongClickListener(v -> {
            currentNumber.setLength(0);
            updateDisplay();
            return true;
        });
    }

    private void appendDigit(String digit) {
        currentNumber.append(digit);
        updateDisplay();
    }

    private void updateDisplay() {
        tvDialedNumber.setText(currentNumber.toString());
    }
}
