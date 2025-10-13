package com.example.qlsukien;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import android.content.SharedPreferences;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import com.google.android.material.navigation.NavigationView;
import android.content.Intent;
import android.widget.TextView;
import java.util.Collections;
import java.util.Comparator;



public class SuKienActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SuKienAdapter adapter;
    private List<SuKien> suKienList = new ArrayList<>();
    private static final String API_URL = "https://qlsukien-1.onrender.com/api/events";

    private Button btnAddSuKien; // nút thêm sự kiện
    private EditText etSearchPlate; // tim kiếm
    private List<SuKien> suKienListFull = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sukien);

        recyclerView = findViewById(R.id.recyclerSuKien);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnAddSuKien = findViewById(R.id.btnAddSuKien);
        etSearchPlate = findViewById(R.id.etSearchPlate);

        boolean canEdit = getIntent().getBooleanExtra("canEdit", false);
        boolean canRegister = getIntent().getBooleanExtra("canRegister", false);

        // Ẩn nút thêm nếu không được quyền
        btnAddSuKien.setVisibility(canEdit ? View.VISIBLE : View.GONE);


        // Lắng nghe khi người dùng nhập
        etSearchPlate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterEvents(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });





        // Bắt sự kiện nút thêm
        btnAddSuKien.setOnClickListener(v -> showAddDialog());


        adapter = new SuKienAdapter(this, suKienList, new SuKienAdapter.SuKienAdapterListener() {
            @Override
            public void onEdit(SuKien suKien) {
                showEditDialog(suKien);
            }

            @Override
            public void onDelete(SuKien suKien) {
                confirmDeleteEvent(suKien.getId());
            }

            public void onRegister(SuKien suKien) {
                registerSuKien(suKien.getId());  // chỉ truyền eventId
            }
        }, canEdit, canRegister);

        recyclerView.setAdapter(adapter);

        fetchSuKien();


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

            } else if (id == R.id.nav_dangky) {
                // Chuyển sang trang quản lý người đăng ký
                Intent intent = new Intent(this, SuKienDaDangKyAllActivity.class);
                startActivity(intent);
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
    // thoát
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



    // Hiển thị dialog nhập sự kiện
    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_edit_sukien, null);
        builder.setView(dialogView);

        EditText etName = dialogView.findViewById(R.id.edtName);
        EditText etStartTime = dialogView.findViewById(R.id.edtStartTime);
        EditText etEndTime = dialogView.findViewById(R.id.edtEndTime);
        EditText etLocation = dialogView.findViewById(R.id.edtLocation);
        EditText etDescription = dialogView.findViewById(R.id.edtDescription);
        Button btnSave = dialogView.findViewById(R.id.btnSave);

        AlertDialog dialog = builder.create();

        // Hàm chọn ngày giờ chung
        View.OnClickListener pickDateTime = v -> {
            final EditText target = (EditText) v;
            Calendar calendar = Calendar.getInstance();

            DatePickerDialog datePicker = new DatePickerDialog(this,
                    (DatePicker view, int year, int month, int dayOfMonth) -> {
                        TimePickerDialog timePicker = new TimePickerDialog(this,
                                (TimePicker timeView, int hourOfDay, int minute) -> {
                                    String dateTime = String.format(Locale.getDefault(),
                                            "%02d/%02d/%04d %02d:%02d",
                                            dayOfMonth, month + 1, year, hourOfDay, minute);
                                    target.setText(dateTime);
                                },
                                calendar.get(Calendar.HOUR_OF_DAY),
                                calendar.get(Calendar.MINUTE),
                                true);
                        timePicker.show();
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePicker.show();
        };

        etStartTime.setOnClickListener(pickDateTime);
        etEndTime.setOnClickListener(pickDateTime);

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String startTime = etStartTime.getText().toString().trim();
            String endTime = etEndTime.getText().toString().trim();
            String location = etLocation.getText().toString().trim();
            String description = etDescription.getText().toString().trim();

            if (name.isEmpty() || startTime.isEmpty() || endTime.isEmpty() || location.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            addSuKien(name, startTime, endTime, location, description);
            dialog.dismiss();
        });

        dialog.show();
    }

    // Hàm chuyển định dạng sang ISO UTC gửi lên server
    private String toISODate(String dateTime) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
            Date date = inputFormat.parse(dateTime);

            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            isoFormat.setTimeZone(TimeZone.getTimeZone("UTC")); // ép UTC
            return isoFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return dateTime;
        }
    }

    // Gọi API thêm sự kiện
    private void addSuKien(String name, String startTime, String endTime, String location, String description) {
        RequestQueue queue = Volley.newRequestQueue(this);

        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("startTime", toISODate(startTime));
        params.put("endTime", toISODate(endTime));
        params.put("location", location);
        params.put("description", description);

        JSONObject jsonObject = new JSONObject(params);
        System.out.println("📤 JSON gửi đi: " + jsonObject.toString());

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, API_URL, jsonObject,
                response -> {
                    System.out.println("✅ Server trả về: " + response.toString());
                    Toast.makeText(this, "Thêm sự kiện thành công", Toast.LENGTH_SHORT).show();
                    fetchSuKien();
                },
                error -> {
                    error.printStackTrace();
                    if (error.networkResponse != null) {
                        String body = new String(error.networkResponse.data);
                        Toast.makeText(this, "Lỗi: " + body, Toast.LENGTH_LONG).show();
                        System.err.println("❌ Server error: " + body);
                    } else {
                        Toast.makeText(this, "Không kết nối server: " + error.toString(), Toast.LENGTH_LONG).show();
                        System.err.println("❌ Error: " + error.toString());
                    }
                });
        queue.add(request);
    }


    // Lấy danh sách sự kiện
    private void fetchSuKien() {
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, API_URL, null,
                response -> {
                    try {
                        suKienList.clear();

                        JSONArray eventsArray = response.getJSONArray("events");

                        for (int i = 0; i < eventsArray.length(); i++) {
                            JSONObject obj = eventsArray.getJSONObject(i);
                            SuKien sk = new SuKien(
                                    obj.getString("_id"),
                                    obj.getString("name"),
                                    formatDateForSearch(obj.getString("startTime")), // chuyển luôn khi fetch
                                    formatDateForSearch(obj.getString("endTime")),
                                    obj.getString("location"),
                                    obj.optString("description", "")
                            );
                            suKienList.add(sk);
                        }

                        // Cập nhật danh sách đầy đủ (backup cho tìm kiếm)
                        suKienListFull.clear();
                        suKienListFull.addAll(suKienList);

                        // Sắp xếp theo tên
                        Collections.sort(suKienList, new Comparator<SuKien>() {
                            @Override
                            public int compare(SuKien s1, SuKien s2) {
                                return s1.getName().compareToIgnoreCase(s2.getName());
                            }
                        });

                        adapter.notifyDataSetChanged();

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Lỗi xử lý dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    if (error.networkResponse != null) {
                        String body = new String(error.networkResponse.data);
                        Toast.makeText(this, "Server error: " + body, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Không kết nối được server", Toast.LENGTH_LONG).show();
                    }
                });

        queue.add(request);
    }

    //hàm sửa

    private void showEditDialog(SuKien suKien) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_edit_sukien, null);
        builder.setView(dialogView);

        // ✅ Lấy TextView tiêu đề và đổi text
        TextView tvTitle = dialogView.findViewById(R.id.tvTitle);
        tvTitle.setText("Sửa sự kiện");

        EditText etName = dialogView.findViewById(R.id.edtName);
        EditText etStartTime = dialogView.findViewById(R.id.edtStartTime);
        EditText etEndTime = dialogView.findViewById(R.id.edtEndTime);
        EditText etLocation = dialogView.findViewById(R.id.edtLocation);
        EditText etDescription = dialogView.findViewById(R.id.edtDescription);
        Button btnSave = dialogView.findViewById(R.id.btnSave);

        // ✅ Đổi text nút lưu
        btnSave.setText("Sửa sự kiện");

        // Gán giá trị cũ
        etName.setText(suKien.getName());
        etStartTime.setText(formatDateForSearch(suKien.getStartTime()));
        etEndTime.setText(formatDateForSearch(suKien.getEndTime()));
        etLocation.setText(suKien.getLocation());
        etDescription.setText(suKien.getDescription());

        AlertDialog dialog = builder.create();

        // Chọn ngày giờ
        View.OnClickListener pickDateTime = v -> {
            final EditText target = (EditText) v;
            Calendar calendar = Calendar.getInstance();

            new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) -> {
                        new TimePickerDialog(this,
                                (timeView, hourOfDay, minute) -> {
                                    String dateTime = String.format(Locale.getDefault(),
                                            "%02d/%02d/%04d %02d:%02d",
                                            dayOfMonth, month + 1, year, hourOfDay, minute);
                                    target.setText(dateTime);
                                },
                                calendar.get(Calendar.HOUR_OF_DAY),
                                calendar.get(Calendar.MINUTE),
                                true).show();
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show();
        };

        etStartTime.setOnClickListener(pickDateTime);
        etEndTime.setOnClickListener(pickDateTime);

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String startTimeStr = etStartTime.getText().toString().trim();
            String endTimeStr = etEndTime.getText().toString().trim();
            String location = etLocation.getText().toString().trim();
            String description = etDescription.getText().toString().trim();

            if(name.isEmpty() || startTimeStr.isEmpty() || endTimeStr.isEmpty() || location.isEmpty()){
                Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            String startTimeISO = toISODate(startTimeStr);
            String endTimeISO = toISODate(endTimeStr);

            updateSuKien(suKien.getId(), name, startTimeISO, endTimeISO, location, description);
            dialog.dismiss();
        });

        dialog.show();
    }


    // Hàm cập nhật hàm sửa lên server
    private void updateSuKien(String id, String name, String startTimeISO, String endTimeISO, String location, String description) {
        RequestQueue queue = Volley.newRequestQueue(this);

        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("startTime", startTimeISO); // đã là ISO
        params.put("endTime", endTimeISO);
        params.put("location", location);
        params.put("description", description);

        JSONObject jsonObject = new JSONObject(params);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, API_URL + "/" + id, jsonObject,
                response -> {
                    Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    fetchSuKien();
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                }
        );

        queue.add(request);
    }


    // Hàm confirm hiển thị hộp thoại
    private void confirmDeleteEvent(String id) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa sự kiện")
                .setMessage("Bạn có chắc muốn xóa sự kiện này không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    deleteSuKien(id);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }


    // hàm xóa
    private void deleteSuKien(String id) {
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE, API_URL + "/" + id, null,
                response -> {
                    Toast.makeText(this, "Xóa thành công", Toast.LENGTH_SHORT).show();
                    fetchSuKien();
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Xóa thất bại", Toast.LENGTH_SHORT).show();
                }
        );

        queue.add(request);
    }
    // hàm đăng ký sự kiện
    private void registerSuKien(String eventId) {
        String url = "https://qlsukien-1.onrender.com/api/registerEvent";

        // Lấy username từ Intent
        String username = getIntent().getStringExtra("username");
        Log.d("DEBUG_USERNAME", "Username lấy từ Intent: " + username);
        Toast.makeText(this, "Username: " + username, Toast.LENGTH_SHORT).show();

        if (username == null || username.isEmpty()) {
            Toast.makeText(this, "❌ Username trống, cần đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject postData = new JSONObject();
        try {
            postData.put("username", username);
            postData.put("eventId", eventId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                postData,
                response -> {
                    try {
                        String msg = response.getString("message");
                        Toast.makeText(this, "✅ " + msg, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    if (error.networkResponse != null) {
                        String body = new String(error.networkResponse.data); // Lấy body trả về từ server
                        try {
                            JSONObject errObj = new JSONObject(body);       // Chuyển body sang JSON
                            String msg = errObj.optString("message", "Lỗi đăng ký"); // Lấy message
                            Toast.makeText(this, "❌ " + msg, Toast.LENGTH_SHORT).show(); // Hiển thị toast
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(this, "❌ Lỗi xử lý phản hồi server", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "❌ Lỗi kết nối: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );

        Volley.newRequestQueue(this).add(request);
    }








    // tìm kiếm
    private void filterEvents(String query) {
        if (suKienListFull == null || suKienListFull.isEmpty()) return;

        List<SuKien> filtered = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        for (SuKien sk : suKienListFull) {
            String name = sk.getName() != null ? sk.getName().toLowerCase() : "";
            String startIso = sk.getStartTime() != null ? sk.getStartTime() : "";
            String endIso = sk.getEndTime() != null ? sk.getEndTime() : "";

            // Chuyển ISO → dd/MM/yyyy để tìm kiếm
            String startFormatted = formatDateForSearch(startIso).toLowerCase();
            String endFormatted = formatDateForSearch(endIso).toLowerCase();

            if (name.contains(lowerQuery) ||
                    startIso.toLowerCase().contains(lowerQuery) ||
                    endIso.toLowerCase().contains(lowerQuery) ||
                    startFormatted.contains(lowerQuery) ||
                    endFormatted.contains(lowerQuery)) {
                filtered.add(sk);
            }
        }

        suKienList.clear();
        suKienList.addAll(filtered);
        adapter.notifyDataSetChanged();
    }

    // Hàm chuyển ISO sang dd/MM/yyyy
    private String formatDateForSearch(String isoDate) {
        try {
            java.text.SimpleDateFormat isoFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US);
            isoFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            Date date = isoFormat.parse(isoDate);

            java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault());
            return outputFormat.format(date);
        } catch (Exception e) {
            return isoDate;
        }
    }

}
