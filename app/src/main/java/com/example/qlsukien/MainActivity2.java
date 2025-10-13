package com.example.qlsukien;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity2 extends AppCompatActivity {

    private CardView cardTaiKhoan, cardSuKien, cardDangKy, cardThoat;
    private String username; // Nhận từ LoginActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);

        // Xử lý edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Nhận username từ LoginActivity
        username = getIntent().getStringExtra("username");

        // Ánh xạ CardView
        cardTaiKhoan = findViewById(R.id.card_taikhoan);
        cardSuKien = findViewById(R.id.card_sukien);
        cardDangKy = findViewById(R.id.card_dangky);
        cardThoat = findViewById(R.id.card_thoat); // ✅ thêm dấu ;

        // Sự kiện click CardView 1
        cardTaiKhoan.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity2.this, NhanVienActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        });

        // CardView 2 - Quản lý sự kiện
        cardSuKien.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity2.this, SuKienActivity.class);
            intent.putExtra("username", username);
            intent.putExtra("canEdit", false);
            intent.putExtra("canRegister", true);
            startActivity(intent);
        });

        // CardView 3 - Danh sách sự kiện đã đăng ký
        cardDangKy.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity2.this, SuKienDaDangKyActivity.class);
            intent.putExtra("username", username);
            intent.putExtra("canUnsubscribe", true);
            intent.putExtra("canSuaxoa", false);
            startActivity(intent);
        });

        // ✅ CardView 4 - Thoát
        cardThoat.setOnClickListener(v -> showExitDialog());
    }

    // Hàm hiển thị hộp thoại xác nhận thoát
    private void showExitDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận thoát")
                .setMessage("Bạn có chắc muốn đăng xuất và quay lại màn hình đăng nhập không?")
                .setPositiveButton("Có", (dialog, which) -> {
                    Intent intent = new Intent(MainActivity2.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish(); // Đóng MainActivity2
                })
                .setNegativeButton("Không", null)
                .show();
    }
}
