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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.json.JSONObject;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.Editable;
import android.util.Log;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import retrofit2.Retrofit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.converter.gson.GsonConverterFactory;


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

        holder.tvUsername.setText("Tên đăng nhập: " + nv.getUsername());
        holder.tvFullName.setText("Họ và tên: " + nv.getFullName());
        holder.tvEmail.setText("Email: " + nv.getEmail());
        holder.tvPhone.setText("SĐT: " + nv.getPhone());
        holder.tvCreatedAt.setText("Ngày tạo: " + formatDate(nv.getCreatedAt()));
        holder.tvUpdatedAt.setText("Cập nhật: " + formatDate(nv.getUpdatedAt()));

        // Hiển thị avatar nếu có
        if (nv.getAvatar() != null && !nv.getAvatar().isEmpty()) {
            try {
                byte[] imageBytes = Base64.decode(nv.getAvatar(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                holder.imgAvatar.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
                holder.imgAvatar.setImageResource(R.drawable.avt); // ảnh mặc định
            }
        } else {
            holder.imgAvatar.setImageResource(R.drawable.avt);
        }

        // Click đổi avatar
        holder.imgAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            ((NhanVienActivity) context).startActivityForResult(intent, 1000);
        });

        holder.btnEdit.setOnClickListener(v -> showEditDialog(nv, position));
        holder.btnChangePassword.setOnClickListener(v -> showChangePasswordDialog(nv, position));
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

    private void updateNhanVienOnServer(NhanVien nv) {
        String url = "https://qlsukien-1.onrender.com/api/updateNhanVien";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("username", nv.getUsername());
            jsonBody.put("fullName", nv.getFullName());
            jsonBody.put("email", nv.getEmail());
            jsonBody.put("phone", nv.getPhone());
            jsonBody.put("updatedAt", nv.getUpdatedAt());

            if (nv.getAvatar() != null) {   // thêm avatar nếu có
                jsonBody.put("avatar", nv.getAvatar());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Lỗi tạo dữ liệu JSON", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(context);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> Toast.makeText(context, "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show(),
                error -> {
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        String body = new String(error.networkResponse.data);
                        Log.e("UPDATE_NV_ERROR", body);
                    }
                    error.printStackTrace();
                    Toast.makeText(context, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
                });

        queue.add(request);
    }



    // ===================== Đổi mật khẩu =====================
    private void showChangePasswordDialog(NhanVien nv, int position) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_change_password, null);

        EditText etOld = view.findViewById(R.id.etOldPassword);
        EditText etNew = view.findViewById(R.id.etNewPassword);
        EditText etConfirm = view.findViewById(R.id.etConfirmPassword);
        ImageView ivToggleOld = view.findViewById(R.id.ivToggleOld);
        ImageView ivToggleNew = view.findViewById(R.id.ivToggleNew);
        ImageView ivToggleConfirm = view.findViewById(R.id.ivToggleConfirm);

        final boolean[] isOldVisible = {false};
        final boolean[] isNewVisible = {false};
        final boolean[] isConfirmVisible = {false};

        // Toggle các ô mật khẩu
        ivToggleOld.setOnClickListener(v -> {
            isOldVisible[0] = !isOldVisible[0];
            etOld.setInputType(isOldVisible[0] ?
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            ivToggleOld.setImageResource(isOldVisible[0] ? R.drawable.mat : R.drawable.matan);
            etOld.setSelection(etOld.getText().length());
        });

        ivToggleNew.setOnClickListener(v -> {
            isNewVisible[0] = !isNewVisible[0];
            etNew.setInputType(isNewVisible[0] ?
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            ivToggleNew.setImageResource(isNewVisible[0] ? R.drawable.mat : R.drawable.matan);
            etNew.setSelection(etNew.getText().length());
        });

        ivToggleConfirm.setOnClickListener(v -> {
            isConfirmVisible[0] = !isConfirmVisible[0];
            etConfirm.setInputType(isConfirmVisible[0] ?
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            ivToggleConfirm.setImageResource(isConfirmVisible[0] ? R.drawable.mat : R.drawable.matan);
            etConfirm.setSelection(etConfirm.getText().length());
        });

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Đổi mật khẩu")
                .setView(view)
                .setPositiveButton("Lưu", null)
                .setNegativeButton("Hủy", (d, w) -> d.dismiss())
                .create();

        dialog.setOnShowListener(d -> {
            Button btnSave = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btnSave.setEnabled(false); // Ban đầu vô hiệu hóa

            // TextWatcher theo dõi cả 3 ô
            TextWatcher watcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String oldPass = etOld.getText().toString().trim();
                    String newPass = etNew.getText().toString().trim();
                    String confirmPass = etConfirm.getText().toString().trim();
                    // Enable nút nếu cả 3 ô không rỗng
                    btnSave.setEnabled(!oldPass.isEmpty() && !newPass.isEmpty() && !confirmPass.isEmpty());
                }
                @Override
                public void afterTextChanged(Editable s) {}
            };

            etOld.addTextChangedListener(watcher);
            etNew.addTextChangedListener(watcher);
            etConfirm.addTextChangedListener(watcher);

            // Click Lưu
            btnSave.setOnClickListener(v -> {
                String oldPass = etOld.getText().toString().trim();
                String newPass = etNew.getText().toString().trim();
                String confirmPass = etConfirm.getText().toString().trim();

                if(!newPass.equals(confirmPass)){
                    Toast.makeText(context, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Cập nhật mật khẩu
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

        // Tạo JSON body
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("username", username);
            jsonBody.put("newPassword", newPassword);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Lỗi tạo dữ liệu JSON", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo RequestQueue
        RequestQueue queue = Volley.newRequestQueue(context);

        // JsonObjectRequest POST
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> Toast.makeText(context, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show(),
                error -> {
                    error.printStackTrace();
                    Toast.makeText(context, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
                });

        queue.add(request);
    }


    // Hàm định dạng ngày từ ISO sang "dd/MM/yyyy HH:mm"
    private String formatDate(String isoDate) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.getDefault());
            Date date = inputFormat.parse(isoDate);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return isoDate;
        }
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
