package com.example.appblocker;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputFilter;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

public class SettingsActivity extends BaseActivity {

    private Switch switchPin;
    private TextView tvCurrentPin, tvAllowedWindow;
    private Button btnChangePin, btnCheckPermissions, btnAllowedTime;
    private ImageView ivTogglePin;
    private SharedPreferences prefs;
    private boolean pinVisible = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setupBottomNav(R.id.nav_settings);

        prefs = getSharedPreferences("AppBlockerPrefs", MODE_PRIVATE);

        initViews();
        loadInitialState();
        setupListeners();
    }

    // ============================================================
    // INIT VIEW
    // ============================================================

    private void initViews() {
        switchPin = findViewById(R.id.switch_pin);
        tvCurrentPin = findViewById(R.id.tvCurrentPin);
        ivTogglePin = findViewById(R.id.ivTogglePin);

        btnChangePin = findViewById(R.id.btnChangePin);
        btnCheckPermissions = findViewById(R.id.btnCheckPermissions);
        btnAllowedTime = findViewById(R.id.btnAllowedTime);

        tvAllowedWindow = findViewById(R.id.tvAllowedWindow);
    }

    private void loadInitialState() {
        switchPin.setChecked(prefs.getBoolean("require_pin", false));
        updatePinText();
        refreshAllowedWindowText();
    }

    private void setupListeners() {

        switchPin.setOnCheckedChangeListener((b, checked) -> handlePinToggle(checked));

        btnChangePin.setOnClickListener(v -> showChangePinDialog());

        ivTogglePin.setOnClickListener(v -> {
            boolean enabled = prefs.getBoolean("require_pin", false);
            if (!enabled) return;

            if (!pinVisible) {
                // đang ẩn, user muốn xem → yêu cầu PIN
                askPinToReveal();
            } else {
                // đang hiện → tắt xem ngay, không cần xác minh
                pinVisible = false;
                updatePinText();
            }
        });


        btnCheckPermissions.setOnClickListener(v -> checkPermissions());

        btnAllowedTime.setOnClickListener(v -> openAllowedTimeDialog());
    }


    // ============================================================
    // PIN HANDLING
    // ============================================================

    private void handlePinToggle(boolean enabled) {

        String pin = prefs.getString("pin_code", "");

        if (enabled) {
            // User bật PIN
            if (pin.isEmpty()) {
                // Chưa có PIN → bắt nhập PIN mới
                showSetNewPinDialog();
            } else {
                prefs.edit().putBoolean("require_pin", true).apply();
                Toast.makeText(this, "Đã bật bảo vệ PIN", Toast.LENGTH_SHORT).show();
            }
            updatePinText();
            return;
        }

        // ----------------------------------------------------
        // Trường hợp user muốn TẮT PIN → hỏi PIN để xác minh
        // ----------------------------------------------------
        if (!pin.isEmpty()) {

            EditText et = buildEditText("Nhập PIN để tắt", InputType.TYPE_CLASS_NUMBER, 4);

            new AlertDialog.Builder(this)
                    .setTitle("Xác minh PIN")
                    .setView(et)
                    .setCancelable(false)
                    .setPositiveButton("OK", (d, w) -> {
                        if (verifyPin(et.getText().toString().trim())) {
                            prefs.edit().putBoolean("require_pin", false).apply();
                            Toast.makeText(this, "Đã tắt bảo vệ PIN", Toast.LENGTH_SHORT).show();
                            switchPin.setChecked(false); // cập nhật UI
                            updatePinText();
                        } else {
                            Toast.makeText(this, "Sai PIN!", Toast.LENGTH_SHORT).show();
                            switchPin.setChecked(true); // giữ nguyên bật
                        }
                    })
                    .setNegativeButton("Hủy", (d, w) -> {
                        switchPin.setChecked(true); // nếu bấm hủy → giữ bật
                    })
                    .show();

            return;
        }

        // ----------------------------------------------------
        // Nếu không có PIN → tắt ngay
        // ----------------------------------------------------
        prefs.edit().putBoolean("require_pin", false).apply();
        Toast.makeText(this, "Đã tắt bảo vệ PIN", Toast.LENGTH_SHORT).show();
        updatePinText();
    }


    private void updatePinText() {
        boolean enabled = prefs.getBoolean("require_pin", false);
        String pin = prefs.getString("pin_code", "");

        if (!enabled || pin.isEmpty()) {
            tvCurrentPin.setText("Chưa thiết lập PIN");
            ivTogglePin.setImageResource(R.drawable.ic_eye_closed);
            return;
        }

        if (pinVisible) {
            tvCurrentPin.setText("PIN hiện tại: " + pin);
            ivTogglePin.setImageResource(R.drawable.ic_eye_open);
        } else {
            tvCurrentPin.setText("PIN hiện tại: ****");
            ivTogglePin.setImageResource(R.drawable.ic_eye_closed);
        }
    }

    private boolean verifyPin(String input) {
        return prefs.getString("pin_code", "").equals(input);
    }

    // ------------------------------------------------------------
    // YÊU CẦU NHẬP PIN TRƯỚC KHI XEM
    // ------------------------------------------------------------

    private void askPinToReveal() {
        EditText et = buildEditText("Nhập PIN để xem", InputType.TYPE_CLASS_NUMBER, 4);

        new AlertDialog.Builder(this)
                .setTitle("Xác minh PIN")
                .setView(et)
                .setPositiveButton("OK", (d, w) -> {
                    if (verifyPin(et.getText().toString().trim())) {
                        pinVisible = true;
                        updatePinText();
                    } else {
                        Toast.makeText(this, "Sai PIN!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }


    // ------------------------------------------------------------
    // TẠO PIN LẦN ĐẦU
    // ------------------------------------------------------------

    private void showSetNewPinDialog() {

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 30, 50, 0);

        EditText etPin1 = buildEditText("Nhập PIN (4 số)", InputType.TYPE_CLASS_NUMBER, 4);
        EditText etPin2 = buildEditText("Nhập lại PIN", InputType.TYPE_CLASS_NUMBER, 4);

        layout.addView(etPin1);
        layout.addView(etPin2);

        new AlertDialog.Builder(this)
                .setTitle("Thiết lập PIN mới")
                .setView(layout)
                .setPositiveButton("Lưu", (d, w) -> {
                    String p1 = etPin1.getText().toString();
                    String p2 = etPin2.getText().toString();

                    if (p1.length() != 4 || !p1.equals(p2)) {
                        Toast.makeText(this, "PIN không hợp lệ!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    prefs.edit().putString("pin_code", p1).apply();
                    switchPin.setChecked(true);
                    updatePinText();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // ------------------------------------------------------------
    // ĐỔI PIN = nhập PIN cũ → PIN mới → xác nhận
    // ------------------------------------------------------------

    private void showChangePinDialog() {

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 30, 50, 0);

        EditText etOld = buildEditText("PIN hiện tại", InputType.TYPE_CLASS_NUMBER, 4);
        EditText etNew1 = buildEditText("PIN mới", InputType.TYPE_CLASS_NUMBER, 4);
        EditText etNew2 = buildEditText("Nhập lại PIN mới", InputType.TYPE_CLASS_NUMBER, 4);

        layout.addView(etOld);
        layout.addView(etNew1);
        layout.addView(etNew2);

        new AlertDialog.Builder(this)
                .setTitle("Đổi mã PIN")
                .setView(layout)
                .setPositiveButton("Lưu", (d, w) -> {
                    String old = etOld.getText().toString();
                    String p1 = etNew1.getText().toString();
                    String p2 = etNew2.getText().toString();

                    if (!verifyPin(old)) {
                        Toast.makeText(this, "Sai PIN hiện tại!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (p1.length() != 4 || !p1.equals(p2)) {
                        Toast.makeText(this, "PIN mới không hợp lệ!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    prefs.edit().putString("pin_code", p1).apply();
                    Toast.makeText(this, "Đổi PIN thành công!", Toast.LENGTH_SHORT).show();
                    updatePinText();

                })
                .setNegativeButton("Hủy", null)
                .show();
    }


    private EditText buildEditText(String hint, int type, int maxLen) {
        EditText et = new EditText(this);
        et.setHint(hint);
        et.setInputType(type);
        if (maxLen > 0) et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLen)});
        return et;
    }


    // ============================================================
    // TIME WINDOW
    // ============================================================

    private void refreshAllowedWindowText() {
        boolean enabled = AllowedTimeManager.isEnabled(this);

        if (!enabled) {
            tvAllowedWindow.setText("Khung giờ được phép: (Không bật)");
        } else {
            tvAllowedWindow.setText("Khung giờ được phép: " + AllowedTimeManager.formatWindow(this));
        }
    }

    private void openAllowedTimeDialog() {
        int[] start = AllowedTimeManager.getStart(this);
        int[] end = AllowedTimeManager.getEnd(this);

        TimePickerDialog pickStart = new TimePickerDialog(
                this,
                (vp, sh, sm) -> openEndTimePicker(sh, sm, end),
                start[0], start[1], true
        );

        new AlertDialog.Builder(this)
                .setTitle("Chế độ khung giờ")
                .setMessage("Bật khung giờ hạn chế?")
                .setPositiveButton("Bật", (d, w) -> pickStart.show())
                .setNegativeButton("Tắt", (d, w) -> handleDisableTime())
                .setNeutralButton("Hủy", null)
                .show();
    }

    private void openEndTimePicker(int sh, int sm, int[] end) {
        new TimePickerDialog(
                this,
                (vp, eh, em) -> {
                    AllowedTimeManager.saveWindow(this, sh, sm, eh, em, true);
                    refreshAllowedWindowText();
                    Toast.makeText(this, "Đã lưu", Toast.LENGTH_SHORT).show();
                },
                end[0], end[1], true
        ).show();
    }

    private void handleDisableTime() {
        boolean pinEnabled = prefs.getBoolean("require_pin", false);

        if (!pinEnabled) {
            disableTimeWindow();
            return;
        }

        EditText et = buildEditText("Nhập PIN để tắt", InputType.TYPE_CLASS_NUMBER, 4);

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận PIN")
                .setView(et)
                .setPositiveButton("OK", (d, w) -> {
                    if (verifyPin(et.getText().toString().trim())) {
                        disableTimeWindow();
                    } else {
                        Toast.makeText(this, "Sai PIN!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void disableTimeWindow() {
        AllowedTimeManager.saveWindow(this, 0, 0, 23, 59, false);
        refreshAllowedWindowText();
        Toast.makeText(this, "Đã tắt giới hạn giờ", Toast.LENGTH_SHORT).show();
    }


    // ============================================================
    // PERMISSION
    // ============================================================

    private void checkPermissions() {
        boolean usage = PermissionUtils.hasUsageStatsPermission(this);
        boolean acc = PermissionUtils.isAccessibilityServiceEnabled(this, BlockerAccessibilityService.class);

        String msg =
                "• Usage Access: " + (usage ? "Đã bật\n" : "Chưa bật\n") +
                        "• Accessibility: " + (acc ? "Đã bật" : "Chưa bật");

        new AlertDialog.Builder(this)
                .setTitle("Tình trạng quyền")
                .setMessage(msg)
                .setPositiveButton("Mở cài đặt", (d, w) ->
                        startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)))
                .setNegativeButton("Đóng", null)
                .show();
    }
}
