package com.example.qlsukien;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import com.google.android.material.navigation.NavigationView;
import android.content.Intent;
import java.util.Collections;
import java.util.Comparator;


public class SuKienDaDangKyAllActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SuKienDaDangKyAllAdapter adapter;
    private List<SuKienDaDangKyAll> suKienList = new ArrayList<>();

    private List<SuKienDaDangKyAll> suKienListFull = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sukien_da_dang_ky_all);

        recyclerView = findViewById(R.id.recyclerSuKienDaDangKy);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new SuKienDaDangKyAllAdapter(this, suKienList);
        recyclerView.setAdapter(adapter);

        fetchSuKienDaDangKy();

        EditText etSearch = findViewById(R.id.etSearchPlate);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterEvents(s.toString()); // <-- dùng hàm filterEvents mới
            }

            @Override public void afterTextChanged(Editable s) {}
        });

        // chỗ 3 gạch

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);

// Tạo nút 3 gạch
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

// Bắt sự kiện chọn menu
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_trangchu) {
                // Chuyển sang trang quản lý sự kiện
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_sukien) {
                // Chuyển sang trang quản lý sự kiện
                Intent intent = new Intent(this, SuKienActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_dangky) {

            } else if (id == R.id.nav_thamgia) {
                // Chuyển sang trang danh sách tham gia
                Intent intent = new Intent(this, NguoithamgiaActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_thongke) {

                Intent intent = new Intent(this, ThongKeActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_thoat) {

                showExitDialog();
            }

            // Đóng menu sau khi chọn
            drawerLayout.closeDrawers();
            return true;
        });

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


    private void fetchSuKienDaDangKy() {
        String url = "https://qlsukien-1.onrender.com/api/allregisterEvent";
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        List<SuKienDaDangKyAll> tempList = new ArrayList<>();

                        JSONArray arr = response.optJSONArray("registrations");
                        if (arr != null) {
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject obj = arr.optJSONObject(i);
                                if (obj == null) continue;

                                // Lấy thông tin user
                                JSONObject user = obj.optJSONObject("userId");
                                String userId = user != null ? user.optString("_id", "") : "";
                                String fullName = user != null ? user.optString("fullName", "") : "";
                                String email = user != null ? user.optString("email", "") : "";
                                String phone = user != null ? user.optString("phone", "") : "";

                                // Lấy thông tin event
                                JSONObject event = obj.optJSONObject("eventId");
                                String eventId = event != null ? event.optString("_id", "") : "";
                                String eventName = event != null ? event.optString("name", "") : "";
                                String startTime = event != null ? event.optString("startTime", "") : "";
                                String endTime = event != null ? event.optString("endTime", "") : "";

                                // Thông tin đăng ký
                                String createdAt = obj.optString("registeredAt", "");
                                String registrationId = obj.optString("_id", ""); // id đăng ký



                                // Tạo object và thêm vào danh sách tạm
                                tempList.add(new SuKienDaDangKyAll(
                                        userId,
                                        fullName,
                                        email,
                                        phone,
                                        eventId,
                                        eventName,
                                        startTime,
                                        endTime,
                                        createdAt,
                                        registrationId

                                ));
                            }
                        }

                        // Cập nhật danh sách hiển thị
                        suKienList.clear();
                        suKienList.addAll(tempList);

                        // ✅ Sắp xếp theo tên sự kiện (A → Z)
                        // Sắp xếp theo tên sự kiện, nếu trùng thì sắp xếp theo createdAt (cũ → mới)
                        Collections.sort(suKienList, new Comparator<SuKienDaDangKyAll>() {
                            @Override
                            public int compare(SuKienDaDangKyAll s1, SuKienDaDangKyAll s2) {
                                // So sánh tên sự kiện
                                int nameCompare = s1.getEventName().compareToIgnoreCase(s2.getEventName());
                                if (nameCompare != 0) {
                                    return nameCompare; // tên khác -> dùng kết quả này
                                } else {
                                    // Tên trùng -> so sánh theo createdAt (cũ trước mới)
                                    try {
                                        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
                                        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                                        Date date1 = isoFormat.parse(s1.getCreatedAt());
                                        Date date2 = isoFormat.parse(s2.getCreatedAt());
                                        return date1.compareTo(date2); // cũ → mới
                                    } catch (Exception e) {
                                        return 0; // lỗi parse thì coi như bằng nhau
                                    }
                                }
                            }
                        });


                        adapter.notifyDataSetChanged();

                        // Lưu bản gốc để filter
                        suKienListFull.clear();
                        suKienListFull.addAll(tempList);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Lỗi xử lý dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Không kết nối server", Toast.LENGTH_SHORT).show()
        );

        queue.add(request);
    }



    private void filterEvents(String query) {
        if (suKienListFull == null || suKienListFull.isEmpty()) return;

        List<SuKienDaDangKyAll> filtered = new ArrayList<>();
        // Tách chuỗi tìm kiếm thành nhiều từ khóa
        String[] keywords = query.toLowerCase().trim().split("\\s+");

        for (SuKienDaDangKyAll sk : suKienListFull) {
            // Lấy dữ liệu các field, tránh null
            String fullName = sk.getFullName() != null ? sk.getFullName().toLowerCase() : "";
            String eventName = sk.getEventName() != null ? sk.getEventName().toLowerCase() : "";
            String email = sk.getEmail() != null ? sk.getEmail().toLowerCase() : "";
            String phone = sk.getPhone() != null ? sk.getPhone().toLowerCase() : "";

            // Format ngày sang dd/MM/yyyy để dễ tìm
            String startDate = formatDateForSearch(sk.getStartTime());
            String endDate = formatDateForSearch(sk.getEndTime());
            String createdDate = formatDateForSearch(sk.getCreatedAt());

            boolean matchAll = true;
            for (String keyword : keywords) {
                if (!(fullName.contains(keyword) ||
                        email.contains(keyword) ||
                        phone.contains(keyword) ||
                        eventName.contains(keyword) ||
                        startDate.contains(keyword) ||
                        endDate.contains(keyword) ||
                        createdDate.contains(keyword))) {
                    matchAll = false;
                    break;
                }
            }


            if (matchAll) {
                filtered.add(sk);
            }
        }

        suKienList.clear();
        suKienList.addAll(filtered);
        adapter.notifyDataSetChanged();
    }



    // Chuyển ISO -> dd/MM/yyyy HH:mm
    public static String formatDateForSearch(String isoDate) {
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = isoFormat.parse(isoDate);

            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            return outputFormat.format(date);
        } catch (Exception e) {
            return isoDate;
        }
    }
}
