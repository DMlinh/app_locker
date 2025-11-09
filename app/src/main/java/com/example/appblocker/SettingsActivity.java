package com.example.appblocker;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputFilter;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class SettingsActivity extends BaseActivity {

    private Switch switchPin;
    private TextView tvCurrentPin;
    private Button btnChangePin, btnCheckPermissions;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setupBottomNav(R.id.nav_settings);
        prefs = getSharedPreferences("AppBlockerPrefs", MODE_PRIVATE);

        // Ánh xạ view
        switchPin = findViewById(R.id.switch_pin);
        tvCurrentPin = findViewById(R.id.tvCurrentPin);
        btnChangePin = findViewById(R.id.btnChangePin);
        btnCheckPermissions = findViewById(R.id.btnCheckPermissions);

        // Khôi phục trạng thái PIN
        boolean isPinEnabled = prefs.getBoolean("require_pin", false);
        switchPin.setChecked(isPinEnabled);

        // Hiển thị PIN hiện tại
        updatePinDisplay();

        // Khi bật/tắt PIN
        switchPin.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("require_pin", isChecked).apply();
            if (isChecked) {
                String currentPin = prefs.getString("pin_code", "");
                if (!currentPin.isEmpty()) {
                    Toast.makeText(this, "PIN hiện tại đã bật", Toast.LENGTH_SHORT).show();
                } else {
                    showSetPinDialog();
                }
            } else {
                Toast.makeText(this, "Đã tắt bảo vệ bằng PIN", Toast.LENGTH_SHORT).show();
            }
            updatePinDisplay();
        });

        // Nút đổi PIN
        btnChangePin.setOnClickListener(v -> showSetPinDialog());

        // Kiểm tra quyền
        btnCheckPermissions.setOnClickListener(v -> checkPermissions());
    }

    private void updatePinDisplay() {
        String currentPin = prefs.getString("pin_code", "");
        if (!currentPin.isEmpty() && prefs.getBoolean("require_pin", false)) {
            tvCurrentPin.setText("PIN hiện tại: " + currentPin);
        } else {
            tvCurrentPin.setText("Chưa thiết lập PIN");
        }
    }

    private void checkPermissions() {
        boolean hasUsageAccess = PermissionUtils.hasUsageStatsPermission(this);
        boolean hasAccessibility = PermissionUtils.isAccessibilityServiceEnabled(this, BlockerAccessibilityService.class);

        String msg = "• Quyền Usage Access: " + (hasUsageAccess ? "✅ Bật\n" : "❌ Tắt\n") +
                "• Quyền Accessibility: " + (hasAccessibility ? "✅ Bật" : "❌ Tắt");

        new AlertDialog.Builder(this)
                .setTitle("Tình trạng quyền")
                .setMessage(msg)
                .setPositiveButton("Mở cài đặt", (d, w) -> {
                    Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                    startActivity(intent);
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void showSetPinDialog() {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER); // chỉ số, hiện đầy đủ không giấu
        input.setHint("Nhập mã PIN (4 chữ số)");

        // Hiển thị PIN hiện tại nếu đã lưu
        String currentPin = prefs.getString("pin_code", "");
        if (!currentPin.isEmpty()) {
            input.setText(currentPin);
            input.setSelection(currentPin.length());
        }

        // Giới hạn tối đa 4 chữ số
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});

        LinearLayout container = new LinearLayout(this);
        container.setPadding(50, 20, 50, 0);
        container.addView(input);

        new AlertDialog.Builder(this)
                .setTitle("Đặt/Đổi mã PIN bảo vệ")
                .setView(container)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String pin = input.getText().toString().trim();
                    if (pin.length() != 4) {
                        Toast.makeText(this, "PIN phải có 4 chữ số", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    prefs.edit().putString("pin_code", pin).apply();
                    prefs.edit().putBoolean("require_pin", true).apply();
                    Toast.makeText(this, "Đã lưu mã PIN", Toast.LENGTH_SHORT).show();
                    updatePinDisplay();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
