package com.example.qlsukien;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NhanVienActivity extends AppCompatActivity {

    // RecyclerView hiển thị danh sách nhân viên
    private RecyclerView recyclerView;

    // Adapter cho RecyclerView
    private NhanVienAdapter adapter;

    // Danh sách nhân viên (ở đây thường chỉ chứa 1 user đăng nhập)
    private List<NhanVien> nhanVienList = new ArrayList<>();

    // Username người dùng vừa đăng nhập
    private String username;

    // API endpoint Node.js
    private static final String API_URL = "https://qlsukien-1.onrender.com/api/nhanvien";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thongtin); // layout chứa RecyclerView
        setTitle("Thông tin tài khoản");

        // Ánh xạ RecyclerView từ layout
        recyclerView = findViewById(R.id.recyclerNhanVien);

        // Gán LayoutManager dạng danh sách
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Khởi tạo adapter với danh sách nhân viên
        adapter = new NhanVienAdapter(this, nhanVienList);

        // Gán adapter cho RecyclerView
        recyclerView.setAdapter(adapter);

        // Lấy username từ Intent (MainActivity gửi sang)
        username = getIntent().getStringExtra("username");

        if (username != null) {
            // Nếu có username, gọi API lấy thông tin user đó
            fetchNhanVienByUsername(username);
        } else {
            // Nếu không có username, báo lỗi
            Toast.makeText(this, "Không có thông tin người dùng", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Gọi API lấy thông tin nhân viên theo username
     */
    private void fetchNhanVienByUsername(String targetUsername) {
        RequestQueue queue = Volley.newRequestQueue(this);

        // Tạo URL query kèm username
        String url = API_URL + "?username=" + targetUsername;

        // JsonArrayRequest vì server trả về mảng JSON
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        nhanVienList.clear(); // xóa danh sách cũ
                        if (response.length() > 0) {
                            JSONObject obj = response.getJSONObject(0); // chỉ lấy phần tử đầu
                            NhanVien nv = new NhanVien(
                                    obj.optString("username"),
                                    obj.optString("fullName"),
                                    obj.optString("email"),
                                    obj.optString("phone"),
                                    obj.optString("password"),
                                    obj.optString("createdAt"),
                                    obj.optString("updatedAt"),
                                    obj.optString("avatar") // avatar
                            );
                            nhanVienList.add(nv);
                        } else {
                            Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                        }
                        adapter.notifyDataSetChanged(); // thông báo adapter cập nhật
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Lỗi khi xử lý dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Lỗi kết nối đến API", Toast.LENGTH_SHORT).show();
                });

        queue.add(request);
    }

    /**
     * Chọn ảnh từ gallery (gọi từ button)
     */
    private void chooseImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 1000);
    }

    /**
     * Xử lý kết quả chọn ảnh
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1000 && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);

                // Resize để tránh ảnh quá lớn
                Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 500, 500, true);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                scaled.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                byte[] imageBytes = baos.toByteArray();
                String base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

                if (!nhanVienList.isEmpty()) {
                    NhanVien nv = nhanVienList.get(0);
                    nv.setAvatar(base64Image);

                    // Gọi phương thức cập nhật avatar lên server
                    updateAvatarOnServer(nv);

                    // Cập nhật RecyclerView
                    adapter.notifyItemChanged(0);
                }

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Lỗi đọc ảnh", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Cập nhật avatar lên server
     */
    public void updateAvatarOnServer(NhanVien nv) {
        if (nv.getAvatar() == null || nv.getAvatar().isEmpty()) {
            Toast.makeText(this, "Avatar trống", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "https://qlsukien-1.onrender.com/api/updateAvatar";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("username", nv.getUsername());
            jsonBody.put("avatar", nv.getAvatar());
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi tạo dữ liệu JSON", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> {
                    Toast.makeText(this, "Cập nhật avatar thành công", Toast.LENGTH_SHORT).show();
                    Log.d("UPDATE_AVATAR", response.toString());
                },
                error -> {
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        String body = new String(error.networkResponse.data);
                        Log.e("UPDATE_AVATAR_ERROR", body);
                    }
                    error.printStackTrace();
                    Toast.makeText(this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
                });

        queue.add(request);
    }
}
