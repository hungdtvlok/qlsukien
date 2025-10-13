package com.example.qlsukien;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Collections;

import java.util.ArrayList;
import java.util.Collections;

public class ThongKeActivity extends AppCompatActivity {

    // 🔹 Khai báo các thành phần giao diện
    private RecyclerView recyclerView; // Danh sách hiển thị thống kê
    private ThongKeAdapter adapter; // Adapter cho RecyclerView
    private ArrayList<ThongKe> listThongKe; // Danh sách gốc từ API
    private ArrayList<ThongKe> filteredList; // Danh sách sau khi lọc tìm kiếm
    private EditText searchView; // Ô nhập để tìm kiếm

    // 🔹 URL API thống kê
    private static final String BASE_URL = "https://qlsukien-1.onrender.com/api/statistics";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thongke); // Gán layout giao diện

        // 🔹 Ánh xạ RecyclerView và cấu hình hiển thị dạng danh sách dọc
        recyclerView = findViewById(R.id.recyclerThongKe);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 🔹 Ánh xạ ô tìm kiếm
        searchView = findViewById(R.id.searchView);

        // 🔹 Khởi tạo danh sách
        listThongKe = new ArrayList<>();   // Danh sách dữ liệu từ API
        filteredList = new ArrayList<>();  // Danh sách hiển thị thực tế

        // 🔹 Gán adapter cho RecyclerView (hiển thị danh sách filteredList)
        adapter = new ThongKeAdapter(filteredList);
        recyclerView.setAdapter(adapter);

        // 🔹 Gọi API để lấy dữ liệu thống kê
        loadStatistics();

        // 🔹 Lắng nghe thay đổi trong ô tìm kiếm để lọc danh sách
        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Không cần xử lý trước khi text thay đổi
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Mỗi khi người dùng nhập ký tự, gọi hàm lọc danh sách
                filterList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Không cần xử lý sau khi text thay đổi
            }
        });

        // 🔹 Ánh xạ Toolbar và đặt làm ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar_thongke);
        setSupportActionBar(toolbar);

        // 🔹 Cấu hình DrawerLayout và NavigationView (menu bên trái)
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);

        // 🔹 Tạo nút 3 gạch mở menu (hamburger button)
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // 🔹 Xử lý sự kiện khi chọn item trong Navigation Drawer
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId(); // Lấy ID menu được chọn

            if (id == R.id.nav_trangchu) {
                // Chuyển sang trang chủ
                startActivity(new Intent(this, MainActivity.class));
            } else if (id == R.id.nav_sukien) {
                // Chuyển sang trang quản lý sự kiện
                startActivity(new Intent(this, SuKienActivity.class));
            } else if (id == R.id.nav_dangky) {
                // Chuyển sang danh sách sự kiện đã đăng ký
                startActivity(new Intent(this, SuKienDaDangKyAllActivity.class));
            } else if (id == R.id.nav_thamgia) {
                // Chuyển sang trang người tham gia
                startActivity(new Intent(this, NguoithamgiaActivity.class));
            } else if (id == R.id.nav_thongke) {
                // Đang ở trang thống kê → không cần chuyển
            } else if (id == R.id.nav_thoat) {
                // Hiển thị hộp thoại xác nhận thoát
                showExitDialog();
            }

            // 🔹 Đóng menu sau khi chọn
            drawerLayout.closeDrawers();
            return true;
        });
    }

    /**
     * 🔹 Gọi API lấy danh sách thống kê
     */
    private void loadStatistics() {
        // Tạo RequestQueue để gửi yêu cầu mạng
        RequestQueue queue = Volley.newRequestQueue(this);

        // Gửi request GET đến API
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                BASE_URL,
                null,
                response -> { // ✅ Xử lý khi API trả về thành công
                    try {
                        // Lấy mảng JSON "statistics" từ kết quả
                        JSONArray arr = response.getJSONArray("statistics");
                        listThongKe.clear(); // Xóa danh sách cũ

                        // Duyệt qua từng phần tử JSON
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            String eventName = obj.optString("eventName", "Không xác định");
                            int count = obj.optInt("count", 0);

                            // Tạo đối tượng ThongKe và thêm vào danh sách
                            listThongKe.add(new ThongKe(eventName, count));
                        }

                        // 🔹 Sắp xếp listThongKe theo tên sự kiện A → Z
                        Collections.sort(listThongKe, (a, b) -> a.getEventName().compareToIgnoreCase(b.getEventName()));

                        // Ban đầu hiển thị toàn bộ danh sách đã sắp xếp
                        filteredList.clear();
                        filteredList.addAll(listThongKe);
                        adapter.notifyDataSetChanged(); // Cập nhật RecyclerView


                        // Nếu không có dữ liệu
                        if (listThongKe.isEmpty()) {
                            Toast.makeText(this, "📭 Không có dữ liệu thống kê", Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "❌ Lỗi phân tích dữ liệu JSON", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> { // ❌ Xử lý lỗi khi gọi API thất bại
                    error.printStackTrace();
                    Toast.makeText(this, "❌ Lỗi kết nối API: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
        );

        // Thêm request vào hàng đợi
        queue.add(request);
    }

    /**
     * 🔍 Lọc danh sách theo từ khóa nhập trong ô tìm kiếm
     */
    private void filterList(String query) {
        filteredList.clear(); // Xóa danh sách hiển thị hiện tại

        if (query.isEmpty()) {
            // Nếu chuỗi tìm kiếm rỗng → hiển thị toàn bộ
            filteredList.addAll(listThongKe);
        } else {
            // Chuyển chuỗi nhập về chữ thường để so sánh không phân biệt hoa/thường
            String lowerQuery = query.toLowerCase().trim();

            // Duyệt toàn bộ danh sách gốc và thêm item có tên phù hợp
            for (ThongKe tk : listThongKe) {
                if (tk.getEventName().toLowerCase().contains(lowerQuery)) {
                    filteredList.add(tk);
                }
            }
        }

        // Cập nhật giao diện RecyclerView
        adapter.notifyDataSetChanged();
    }

    /**
     * 🧭 Hiển thị hộp thoại xác nhận đăng xuất
     */
    private void showExitDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận đăng xuất")
                .setMessage("Bạn có chắc muốn đăng xuất và quay lại màn hình đăng nhập không?")
                .setPositiveButton("Có", (dialog, which) -> {
                    // Chuyển về màn hình đăng nhập và xóa toàn bộ Activity cũ
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Không", null) // Bấm “Không” thì đóng dialog
                .show();
    }
}
