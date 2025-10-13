package com.example.qlsukien;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NhanVienAdapter extends RecyclerView.Adapter<NhanVienAdapter.ViewHolder> {

    private Context context;
    private List<NhanVien> nhanVienList;

    public NhanVienAdapter(Context context, List<NhanVien> nhanVienList) {
        this.context = context;
        this.nhanVienList = nhanVienList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_nhanvien, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NhanVien nv = nhanVienList.get(position);

        // Hiển thị thông tin
        holder.tvUsername.setText("Tên đăng nhập: " + nv.getUsername());
        holder.tvFullName.setText("Họ và tên: " + nv.getFullName());
        holder.tvEmail.setText("Email: " + nv.getEmail());
        holder.tvPhone.setText("SĐT: " + nv.getPhone());
        holder.tvCreatedAt.setText("Ngày tạo: " + nv.getCreatedAt());
        holder.tvUpdatedAt.setText("Ngày cập nhật: " + nv.getUpdatedAt());

        // Hiển thị ảnh avatar (nếu có)
        if(nv.getAvatarBase64() != null && !nv.getAvatarBase64().isEmpty()){
            byte[] decodedString = Base64.decode(nv.getAvatarBase64(), Base64.DEFAULT);
            holder.imgAvatar.setImageBitmap(android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));
        } else {
            holder.imgAvatar.setImageResource(R.mipmap.ic_launcher_round); // ảnh mặc định
        }

        // Sửa thông tin
        holder.btnEdit.setOnClickListener(v -> showEditDialog(nv, position));

        // Đổi mật khẩu
        holder.btnChangePassword.setOnClickListener(v -> showChangePasswordDialog(nv, position));

        // Chọn ảnh
        holder.imgAvatar.setOnClickListener(v -> selectImageFromGallery(nv, position, holder.imgAvatar));
    }

    @Override
    public int getItemCount() {
        return nhanVienList.size();
    }

    // ===================== Chọn ảnh từ gallery =====================
    private void selectImageFromGallery(NhanVien nv, int position, ImageView imgAvatar){
        // Intent chọn ảnh
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        ((MainActivity) context).startActivityForResult(intent, 1000); // 1000 là request code

        // Trong MainActivity.onActivityResult() xử lý:
        // Uri selectedImageUri = data.getData();
        // Chuyển thành Base64 và gọi updateAvatarOnServer(nv, base64Image);
    }

    // ===================== Cập nhật avatar lên server =====================
    public void updateAvatarOnServer(NhanVien nv, String base64Image){
        String url = "https://qlsukien-1.onrender.com/api/updateAvatar"; // POST endpoint mới

        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> Toast.makeText(context, "Cập nhật ảnh thành công", Toast.LENGTH_SHORT).show(),
                error -> Toast.makeText(context, "Lỗi kết nối server", Toast.LENGTH_SHORT).show()){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", nv.getUsername());
                params.put("avatar", base64Image); // gửi ảnh Base64
                return params;
            }
        };
        queue.add(request);
    }

    // ===================== Sửa thông tin nhân viên =====================
    private void showEditDialog(NhanVien nv, int position) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_nhanvien, null);
        EditText etFullName = view.findViewById(R.id.etFullName);
        EditText etEmail = view.findViewById(R.id.etEmail);
        EditText etPhone = view.findViewById(R.id.etPhone);

        etFullName.setText(nv.getFullName());
        etEmail.setText(nv.getEmail());
        etPhone.setText(nv.getPhone());

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Sửa thông tin")
                .setView(view)
                .setPositiveButton("Lưu", null)
                .setNegativeButton("Hủy", (d, w) -> d.dismiss())
                .create();

        dialog.setOnShowListener(d -> {
            Button btnSave = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btnSave.setOnClickListener(v -> {
                String fullName = etFullName.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String phone = etPhone.getText().toString().trim();

                if(fullName.isEmpty() || email.isEmpty() || phone.isEmpty()){
                    Toast.makeText(context, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                    return;
                }

                nv.setFullName(fullName);
                nv.setEmail(email);
                nv.setPhone(phone);
                notifyItemChanged(position);

                updateNhanVienOnServer(nv);
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void updateNhanVienOnServer(NhanVien nv){
        String url = "https://qlsukien-1.onrender.com/api/updateNhanVien";

        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> Toast.makeText(context, "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show(),
                error -> Toast.makeText(context, "Lỗi kết nối server", Toast.LENGTH_SHORT).show()){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", nv.getUsername());
                params.put("fullName", nv.getFullName());
                params.put("email", nv.getEmail());
                params.put("phone", nv.getPhone());
                return params;
            }
        };
        queue.add(request);
    }

    // ===================== Đổi mật khẩu =====================
    private void showChangePasswordDialog(NhanVien nv, int position) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_change_password, null);
        EditText etNewPassword = view.findViewById(R.id.edtNewPassword);
        EditText etConfirmPassword = view.findViewById(R.id.edtConfirmPassword);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Đổi mật khẩu")
                .setView(view)
                .setPositiveButton("Lưu", null)
                .setNegativeButton("Hủy", (d, w) -> d.dismiss())
                .create();

        dialog.setOnShowListener(d -> {
            Button btnSave = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btnSave.setOnClickListener(v -> {
                String newPass = etNewPassword.getText().toString().trim();
                String confirmPass = etConfirmPassword.getText().toString().trim();

                if(newPass.isEmpty() || confirmPass.isEmpty()){
                    Toast.makeText(context, "Vui lòng nhập đầy đủ mật khẩu", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(!newPass.equals(confirmPass)){
                    Toast.makeText(context, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
                    return;
                }

                nv.setPassword(newPass);
                notifyItemChanged(position);
                updatePasswordOnServer(nv.getUsername(), newPass);
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void updatePasswordOnServer(String username, String newPassword){
        String url = "https://qlsukien-1.onrender.com/api/changePassword";

        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> Toast.makeText(context, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show(),
                error -> Toast.makeText(context, "Lỗi kết nối server", Toast.LENGTH_SHORT).show()){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("newPassword", newPassword);
                return params;
            }
        };
        queue.add(request);
    }

    // ===================== ViewHolder =====================
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvFullName, tvEmail, tvPhone, tvCreatedAt, tvUpdatedAt;
        Button btnEdit, btnChangePassword;
        ImageView imgAvatar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvFullName = itemView.findViewById(R.id.tvFullName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
            tvUpdatedAt = itemView.findViewById(R.id.tvUpdatedAt);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnChangePassword = itemView.findViewById(R.id.btnChangePassword);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
        }
    }
}
