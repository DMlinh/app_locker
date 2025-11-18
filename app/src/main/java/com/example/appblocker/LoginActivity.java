package com.example.appblocker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends BaseActivity {

    EditText edtUser, edtPass;
    Button btnLogin, btnGoRegister;
    UserDatabaseHelper db;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtUser = findViewById(R.id.edtUsername);
        edtPass = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoRegister = findViewById(R.id.btnGoRegister);

        db = new UserDatabaseHelper(this);
        prefs = getSharedPreferences("USER_SESSION", MODE_PRIVATE);

        // Nếu đã login → vào thẳng app
        if (prefs.getString("current_user", null) != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        btnGoRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );

        btnLogin.setOnClickListener(v -> {
            String u = edtUser.getText().toString();
            String p = edtPass.getText().toString();

            if (db.loginUser(u, p)) {
                prefs.edit().putString("current_user", u).apply();
                Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
