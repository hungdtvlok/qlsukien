package com.example.qlsukien;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import com.google.android.material.navigation.NavigationView;
import android.content.Intent;
import java.util.Collections;

import android.util.Log;

/**
 * Activity quản lý Người Tham Gia (Participant)
 * - Xem danh sách
 * - Tìm kiếm
 * - Thêm, sửa, xóa
 */
public class NguoithamgiaActivity extends AppCompatActivity {

    private EditText etSearchPlate;
    private RecyclerView recyclerNguoithamgia;
    private NguoithamgiaAdapter adapter;
    private ArrayList<Nguoithamgia> listNguoi = new ArrayList<>();

    private static final String BASE_URL = "https://qlsukien-1.onrender.com/api/participants";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nguoithamgia);

        etSearchPlate = findViewById(R.id.etSearchPlate);
        recyclerNguoithamgia = findViewById(R.id.recyclerNguoithamgia);

        recyclerNguoithamgia.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NguoithamgiaAdapter(this, listNguoi, new NguoithamgiaAdapter.OnItemClickListener() {
            @Override
            public void onEdit(Nguoithamgia item) {
                showEditDialog(item);
            }

            @Override
            public void onDelete(Nguoithamgia item) {
                confirmDelete(item);
            }
        });
        recyclerNguoithamgia.setAdapter(adapter);

        loadParticipants();

        etSearchPlate.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // nút thêm
        findViewById(R.id.btnAddNguoithamgia).setOnClickListener(v -> showAddDialog());

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
                // Chuyển sang trang quản lý người đăng ký
                Intent intent = new Intent(this, SuKienDaDangKyAllActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_thamgia) {

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

    /**
     * Load danh sách participants từ API
     */
    private void loadParticipants() {
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, BASE_URL, null,
                response -> {
                    listNguoi.clear();
                    try {
                        JSONArray arr = response.getJSONArray("participants");
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            Nguoithamgia item = new Nguoithamgia(
                                    obj.optString("_id"),
                                    obj.optString("fullName"),
                                    obj.optString("email"),
                                    obj.optString("phone"),
                                    obj.optString("eventName"),
                                    formatDate(obj.optString("startTime")),
                                    formatDate(obj.optString("endTime")),
                                    obj.optString("registeredBy"),
                                    formatDate(obj.optString("createdAt"))
                            );
                            listNguoi.add(item);
                        }

                        // 🔹 Sắp xếp theo tên sự kiện A → Z, nếu trùng thì sắp xếp theo createdAt (cũ → mới)
                        Collections.sort(listNguoi, (p1, p2) -> {
                            int nameCompare = p1.getEventName().compareToIgnoreCase(p2.getEventName());
                            if (nameCompare != 0) {
                                return nameCompare; // sắp xếp theo tên sự kiện
                            } else {
                                try {
                                    // parse từ format đã dùng
                                    SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                                    Date date1 = displayFormat.parse(p1.getCreatedAt());
                                    Date date2 = displayFormat.parse(p2.getCreatedAt());
                                    return date1.compareTo(date2); // cũ → mới
                                } catch (Exception e) {
                                    return 0; // nếu lỗi parse
                                }
                            }
                        });


                        adapter.updateList(listNguoi);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "❌ Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show()
        );

        queue.add(request);
    }


    /**
     * Dialog thêm participant
     */
    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thêm người tham gia");

        View view = getLayoutInflater().inflate(R.layout.dialog_nguoithamgia, null);
        EditText etFullName = view.findViewById(R.id.etFullName);
        EditText etEmail = view.findViewById(R.id.etEmail);
        EditText etPhone = view.findViewById(R.id.etPhone);
        EditText etEventName = view.findViewById(R.id.etEventName);
        EditText etStartTime = view.findViewById(R.id.etStartTime);
        EditText etEndTime = view.findViewById(R.id.etEndTime);
        EditText etRegisteredBy = view.findViewById(R.id.etRegisteredBy);

        // chọn ngày giờ - hiển thị dạng dd/MM/yyyy HH:mm
        View.OnClickListener pickDateTime = v -> {
            final EditText target = (EditText) v;
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePicker = new DatePickerDialog(this,
                    (DatePicker view1, int year, int month, int dayOfMonth) -> {
                        TimePickerDialog timePicker = new TimePickerDialog(this,
                                (TimePicker timeView, int hourOfDay, int minute) -> {
                                    // Hiển thị dạng dd/MM/yyyy HH:mm
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


        builder.setView(view);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            addParticipant(
                    etFullName.getText().toString(),
                    etEmail.getText().toString(),
                    etPhone.getText().toString(),
                    etEventName.getText().toString(),
                    etStartTime.getText().toString(),
                    etEndTime.getText().toString(),
                    etRegisteredBy.getText().toString()
            );
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void addParticipant(String fullName, String email, String phone,
                                String eventName, String startTime, String endTime, String registeredBy) {
        RequestQueue queue = Volley.newRequestQueue(this);

        // 🔹 Chuyển sang ISO trước khi gửi
        String isoStart = toISODate(startTime);
        String isoEnd   = toISODate(endTime);

        JSONObject body = new JSONObject();
        try {
            body.put("fullName", fullName);
            body.put("email", email);
            body.put("phone", phone);
            body.put("eventName", eventName);
            body.put("startTime", isoStart);
            body.put("endTime", isoEnd);
            body.put("registeredBy", registeredBy);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, BASE_URL, body,
                response -> {
                    Toast.makeText(this, "✅ Thêm thành công", Toast.LENGTH_SHORT).show();
                    loadParticipants();
                },
                error -> Toast.makeText(this, "❌ Lỗi khi thêm", Toast.LENGTH_SHORT).show()
        );
        queue.add(request);
    }

    /**
     * Dialog sửa participant
     */
    private void showEditDialog(Nguoithamgia item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sửa thông tin");

        View view = getLayoutInflater().inflate(R.layout.dialog_nguoithamgia, null);
        EditText etFullName = view.findViewById(R.id.etFullName);
        EditText etEmail = view.findViewById(R.id.etEmail);
        EditText etPhone = view.findViewById(R.id.etPhone);
        EditText etEventName = view.findViewById(R.id.etEventName);
        EditText etStartTime = view.findViewById(R.id.etStartTime);
        EditText etEndTime = view.findViewById(R.id.etEndTime);
        EditText etRegisteredBy = view.findViewById(R.id.etRegisteredBy);

        etFullName.setText(item.getFullName());
        etEmail.setText(item.getEmail());
        etPhone.setText(item.getPhone());
        etEventName.setText(item.getEventName());
        etStartTime.setText(formatDate(item.getStartTime()));
        etEndTime.setText(formatDate(item.getEndTime()));
        etRegisteredBy.setText(item.getRegisteredBy());

        // chọn ngày giờ - hiển thị dạng dd/MM/yyyy HH:mm
        View.OnClickListener pickDateTime = v -> {
            final EditText target = (EditText) v;
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePicker = new DatePickerDialog(this,
                    (DatePicker view1, int year, int month, int dayOfMonth) -> {
                        TimePickerDialog timePicker = new TimePickerDialog(this,
                                (TimePicker timeView, int hourOfDay, int minute) -> {
                                    // Hiển thị dạng dd/MM/yyyy HH:mm
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


        builder.setView(view);

        builder.setPositiveButton("Cập nhật", (dialog, which) -> {
            updateParticipant(
                    item.getId(),
                    etFullName.getText().toString(),
                    etEmail.getText().toString(),
                    etPhone.getText().toString(),
                    etEventName.getText().toString(),
                    etStartTime.getText().toString(),
                    etEndTime.getText().toString(),
                    etRegisteredBy.getText().toString()
            );
        });



        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    /**
     * API cập nhật participant
     */
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
    private void updateParticipant(String id, String fullName, String email, String phone,
                                   String eventName, String startTime, String endTime, String registeredBy) {

        String url = "https://qlsukien-1.onrender.com/api/participants/" + id;

        // 🔹 Chuyển sang ISO trước khi gửi
        String isoStart = toISODate(startTime);
        String isoEnd   = toISODate(endTime);

        JSONObject putData = new JSONObject();
        try {
            putData.put("fullName", fullName);
            putData.put("email", email);
            putData.put("phone", phone);
            putData.put("eventName", eventName);
            putData.put("startTime", isoStart);
            putData.put("endTime", isoEnd);
            putData.put("registeredBy", registeredBy);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi tạo JSON update", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                putData,
                response -> {
                    Toast.makeText(this, "✅ Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    loadParticipants();
                    },
                error -> {
                    String errorMsg = "❌ Lỗi khi cập nhật";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        try {
                            String data = new String(error.networkResponse.data);
                            JSONObject obj = new JSONObject(data);
                            if (obj.has("message")) errorMsg = obj.getString("message");
                        } catch (Exception ignored) {}
                    }
                    Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
                    Log.e("VOLLEY_ERROR", errorMsg, error);
                }
        );

        queue.add(request);
    }



    private void confirmDelete(Nguoithamgia item) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa người tham gia")
                .setMessage("Bạn có chắc muốn xóa " + item.getFullName() + " không?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteParticipant(item))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteParticipant(Nguoithamgia item) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = BASE_URL + "/" + item.getId();

        StringRequest request = new StringRequest(Request.Method.DELETE, url,
                response -> {
                    Toast.makeText(this, "✅ Đã xóa thành công", Toast.LENGTH_SHORT).show();
                    loadParticipants();
                },
                error -> Toast.makeText(this, "❌ Lỗi khi xóa", Toast.LENGTH_SHORT).show()
        );
        queue.add(request);
    }

    private String formatDate(String isoDate) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.getDefault());
            Date date = inputFormat.parse(isoDate);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return isoDate;
        }
    }
}
