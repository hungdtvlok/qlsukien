package com.example.qlsukien;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    TextView txtPhone, txtFacebook, txtZalo;
    Button btnBackLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        // Ánh xạ view
        txtPhone = findViewById(R.id.txtPhone);
        txtFacebook = findViewById(R.id.txtFacebook);
        txtZalo = findViewById(R.id.txtZalo);
        btnBackLogin = findViewById(R.id.btnBackLogin);

        // 📞 Nhấn vào số điện thoại -> mở app gọi
        txtPhone.setOnClickListener(v -> {
            String phone = "0343875561"; // thay bằng số thật
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phone));
            startActivity(intent);
        });

        // 🌐 Nhấn vào Facebook -> mở link
        txtFacebook.setOnClickListener(v -> {
            String fbUrl = "https://www.facebook.com/mai.huy.chuong.2025"; // thay bằng link thật
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(fbUrl));
            startActivity(intent);
        });

        // 💬 Nhấn vào Zalo -> mở link Zalo (zalo.me)
        txtZalo.setOnClickListener(v -> {
            String zaloUrl = "https://zalo.me/0343875561"; // thay bằng link/sdt thật
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(zaloUrl));
            startActivity(intent);
        });

        // 🔙 Nút quay lại -> về màn hình Login
        btnBackLogin.setOnClickListener(v -> {
            finish(); // đóng RegisterActivity, quay lại LoginActivity
        });
    }
}
