package com.example.qlsukien;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NhanVienActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NhanVienAdapter adapter;
    private List<NhanVien> nhanVienList = new ArrayList<>();
    private static final String API_URL = "https://qlsukien-1.onrender.com/nhanvien";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.thongtinnhanvien);
        setTitle("Thông tin tài khoản");

        recyclerView = findViewById(R.id.recyclerNhanVien);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new NhanVienAdapter(this, nhanVienList);
        recyclerView.setAdapter(adapter);

        // Lấy username từ Intent nếu có
        String username = getIntent().getStringExtra("username");
        if (username != null) {
            fetchNhanVienByUsername(username);
        } else {
            fetchAllNhanVien();
        }
    }

    // Lấy tất cả nhân viên
    private void fetchAllNhanVien() {
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, API_URL, null,
                response -> {
                    try {
                        nhanVienList.clear();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            NhanVien nv = new NhanVien(
                                    obj.optString("username"),
                                    obj.optString("password"),
                                    obj.optString("fullName"),
                                    obj.optString("email"),
                                    obj.optString("phone"),
                                    obj.optString("createdAt"),
                                    obj.optString("updatedAt")
                            );
                            nhanVienList.add(nv);
                        }
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Lỗi khi xử lý dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Lỗi kết nối đến API", Toast.LENGTH_SHORT).show();
                }
        );

        queue.add(request);
    }

    // Lấy nhân viên theo username
    private void fetchNhanVienByUsername(String targetUsername) {
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, API_URL, null,
                response -> {
                    try {
                        nhanVienList.clear();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            if (obj.optString("username").equals(targetUsername)) {
                                NhanVien nv = new NhanVien(
                                        obj.optString("username"),
                                        obj.optString("password"),
                                        obj.optString("fullName"),
                                        obj.optString("email"),
                                        obj.optString("phone"),
                                        obj.optString("createdAt"),
                                        obj.optString("updatedAt")
                                );
                                nhanVienList.add(nv);
                                break;
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Lỗi khi xử lý dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Lỗi kết nối đến API", Toast.LENGTH_SHORT).show();
                }
        );

        queue.add(request);
    }
}
