package com.example.qlsukien;

import android.os.Bundle;
import android.util.Log;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class SuKienDaDangKyActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SuKienDaDangKyAdapter adapter;
    private List<SuKienDaDangKy> suKienList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sukien_da_dang_ky);

        recyclerView = findViewById(R.id.recyclerSuKienDaDangKy);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new SuKienDaDangKyAdapter(this, suKienList);

        recyclerView.setAdapter(adapter);

        fetchSuKienDaDangKy();
    }

    private void fetchSuKienDaDangKy() {
        String username = getIntent().getStringExtra("username");

        if (username == null || username.isEmpty()) {
            Toast.makeText(this, "Username trống", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "https://qlsukien-1.onrender.com/api/registerEvent/" + username;

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        suKienList.clear();
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

                                // Thời gian đăng ký
                                String createdAt = obj.optString("registeredAt", "");

                                String status = obj.optString("status", "joined");

                                // Tạo đối tượng và thêm vào danh sách
                                SuKienDaDangKy sk = new SuKienDaDangKy(
                                        userId,username, fullName, email, phone,
                                        eventId, eventName, startTime, endTime,
                                        createdAt,status
                                );
                                suKienList.add(sk);
                            }
                        }

                        // Cập nhật RecyclerView
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Lỗi xử lý dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "Không kết nối server", Toast.LENGTH_SHORT).show();
                });

        queue.add(request);
    }




    // Hàm chuyển ISO -> dd/MM/yyyy HH:mm
    public static String formatDateForSearch(String isoDate) {
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US);
            isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = isoFormat.parse(isoDate);

            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            return outputFormat.format(date);
        } catch (Exception e) {
            return isoDate;
        }
    }
}
