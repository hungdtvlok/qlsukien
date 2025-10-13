package com.example.qlsukien;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    EditText edtUsername, edtPassword;
    TextView txtRegister;
    Button btnLogin;
    RadioGroup radioRole;
    RadioButton radioUser, radioAdmin;

    // URL API login
    String URL = "https://qlsukien-1.onrender.com/api/login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // ===== Ánh xạ các view =====
        edtUsername = findViewById(R.id.Username);
        edtPassword = findViewById(R.id.Password);
        txtRegister = findViewById(R.id.txtRegister);
        btnLogin = findViewById(R.id.btnLogin);
        radioRole = findViewById(R.id.radioRole);
        radioUser = findViewById(R.id.radioUser);
        radioAdmin = findViewById(R.id.radioAdmin);

        // ===== Nút đăng nhập =====
        btnLogin.setOnClickListener(v -> {
            String username = edtUsername.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            } else {
                loginUser(username, password);
            }
        });

        // ===== Nút chuyển sang đăng ký =====
        txtRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
    }

    // ===== Hàm login =====
    private void loginUser(String username, String password) {
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("username", username);
            jsonBody.put("password", password);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, jsonBody,
                    response -> {
                        try {
                            String message = response.optString("message", "");
                            String token = response.optString("token", "");
                            String serverRole = response.optString("role", "user").trim().toLowerCase();

                            // Lấy radio đang chọn
                            int selectedId = radioRole.getCheckedRadioButtonId();
                            if (selectedId == -1) {
                                Toast.makeText(LoginActivity.this, "Vui lòng chọn vai trò", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            RadioButton selectedRadio = findViewById(selectedId);
                            String selectedRole = selectedRadio.getText().toString().trim().toLowerCase();

                            System.out.println("SelectedRole: '" + selectedRole + "' | ServerRole: '" + serverRole + "'");

                            if (!token.isEmpty()) {
                                // Nếu radio chọn khác role server → không đăng nhập
                                if (!selectedRole.equals(serverRole)) {
                                    Toast.makeText(LoginActivity.this,
                                            "Vai trò không đúng cho tài khoản này", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                // Role trùng → đăng nhập
                                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                                Intent intent;
                                if (serverRole.equals("admin")) {
                                    intent = new Intent(LoginActivity.this, MainActivity.class);
                                } else {
                                    intent = new Intent(LoginActivity.this, MainActivity2.class);
                                }

                                intent.putExtra("username", username);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this,
                                        message.isEmpty() ? "Sai tài khoản hoặc mật khẩu" : message,
                                        Toast.LENGTH_SHORT).show();
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(LoginActivity.this, "Lỗi xử lý phản hồi", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        if (error.networkResponse != null) {
                            String errorMsg = new String(error.networkResponse.data);
                            int statusCode = error.networkResponse.statusCode;
                            if (statusCode == 400 || statusCode == 401) {
                                Toast.makeText(LoginActivity.this, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(LoginActivity.this, "Lỗi server: " + errorMsg, Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Không thể kết nối đến server", Toast.LENGTH_LONG).show();
                        }
                    });

            RequestQueue queue = Volley.newRequestQueue(this);
            queue.add(request);

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi tạo dữ liệu đăng nhập", Toast.LENGTH_SHORT).show();
        }
    }

}
