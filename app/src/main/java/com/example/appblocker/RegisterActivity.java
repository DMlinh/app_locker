package com.example.appblocker;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends BaseActivity {

    EditText edtUser, edtPass, edtConfirm;
    Button btnRegister;
    UserDatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtUser = findViewById(R.id.edtUsername);
        edtPass = findViewById(R.id.edtPassword);
        edtConfirm = findViewById(R.id.edtConfirm);
        btnRegister = findViewById(R.id.btnRegister);

        db = new UserDatabaseHelper(this);

        btnRegister.setOnClickListener(v -> {
            String u = edtUser.getText().toString();
            String p = edtPass.getText().toString();
            String c = edtConfirm.getText().toString();

            if (u.isEmpty() || p.isEmpty() || c.isEmpty()) {
                Toast.makeText(this, "Không được để trống!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!p.equals(c)) {
                Toast.makeText(this, "Mật khẩu không trùng!", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean created = db.registerUser(u, p);
            if (created) {
                Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                finish(); // Quay lại LoginActivity
            } else {
                Toast.makeText(this, "Tên đăng nhập đã tồn tại", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
