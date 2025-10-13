package com.example.qlsukien;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    private Button btnQuanlysukien, btnDanhsachdk, btnNguoithamgia, btnThongke;;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ánh xạ
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        toolbar = findViewById(R.id.toolbar);

        // Thiết lập Toolbar làm ActionBar
        setSupportActionBar(toolbar);

        // Nút 3 gạch mở Navigation Drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Xử lý click menu Navigation
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_sukien) {
                    openSuKien();
                } else if (id == R.id.nav_dangky) {
                    openDangKy();
                } else if (id == R.id.nav_thamgia) {
                    openThamGia();
                } else if (id == R.id.nav_thongke) {
                    openThongKe();
                } else if (id == R.id.nav_thoat) {
                    showExitDialog();
                }

                drawerLayout.closeDrawers();
                return true;
            }
        });

        // Xử lý các nút ở giữa màn hình
        btnQuanlysukien = findViewById(R.id.button);
        btnQuanlysukien.setOnClickListener(v -> openSuKien());

        btnNguoithamgia = findViewById(R.id.button2);
        btnNguoithamgia.setOnClickListener(v -> openThamGia());

        btnDanhsachdk = findViewById(R.id.button3);
        btnDanhsachdk.setOnClickListener(v -> openDangKy());

        btnThongke = findViewById(R.id.button4);
        btnThongke.setOnClickListener(v -> openThongKe());

    }

    private void openSuKien() {
        Intent intent = new Intent(MainActivity.this, SuKienActivity.class);
        intent.putExtra("canEdit", true);
        intent.putExtra("canRegister", false);
        startActivity(intent);
    }

    private void openDangKy() {
        Intent intent = new Intent(MainActivity.this, SuKienDaDangKyAllActivity.class);
        intent.putExtra("canUnsubscribe", false);
        intent.putExtra("canSuaxoa", true);
        startActivity(intent);
    }

    private void openThamGia() {
        Intent intent = new Intent(MainActivity.this, NguoithamgiaActivity.class);
        startActivity(intent);
    }

    private void openThongKe() {
        Intent intent = new Intent(MainActivity.this, ThongKeActivity.class);
        startActivity(intent);
    }


    // 🧭 Hàm hiển thị hộp thoại xác nhận thoát
    private void showExitDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận đăng xuất")
                .setMessage("Bạn có chắc muốn đăng xuất và quay lại màn hình đăng nhập không?")
                .setPositiveButton("Có", (dialog, which) -> {
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish(); // Đóng Activity hiện tại
                })
                .setNegativeButton("Không", null)
                .show();
    }

}
