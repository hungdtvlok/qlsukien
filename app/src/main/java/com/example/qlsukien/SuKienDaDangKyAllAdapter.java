package com.example.qlsukien;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SuKienDaDangKyAllAdapter extends RecyclerView.Adapter<SuKienDaDangKyAllAdapter.ViewHolder>{


    private Context context;
    private List<SuKienDaDangKyAll> list;
    private List<SuKienDaDangKyAll> listFull; // danh sách gốc để lọc

    private boolean canUnsubscribe = false; // ẩn/hiện nút hủy đăng ký

    private boolean canSuaxoa = false;

    public SuKienDaDangKyAllAdapter(Context context, List<SuKienDaDangKyAll> list) {
        this.context = context;
        this.list = list;
        this.listFull = new ArrayList<>(list);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_sukien_da_dang_ky, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SuKienDaDangKyAll item = list.get(position);

        holder.tvFullName.setText("Họ tên: " + item.getFullName());
        holder.tvEmail.setText("Email: " + item.getEmail());
        holder.tvPhone.setText("SĐT: " + item.getPhone());
        holder.tvEventName.setText("Sự kiện: " + item.getEventName());
        holder.tvStartTime.setText("Bắt đầu: " + SuKienDaDangKyAllActivity.formatDateForSearch(item.getStartTime()));
        holder.tvEndTime.setText("Kết thúc: " + SuKienDaDangKyAllActivity.formatDateForSearch(item.getEndTime()));
        holder.tvCreatedAt.setText("Đăng ký lúc: " + SuKienDaDangKyAllActivity.formatDateForSearch(item.getCreatedAt()));

        holder.btnUnregister.setVisibility(canUnsubscribe ? View.VISIBLE : View.GONE);


        holder.btnDelete.setOnClickListener(v -> {
            confirmDelete(item, position); // chỉ gọi dialog xác nhận
        });

        // hàm them
        holder.btnAdd.setOnClickListener(v -> {
            String url = "https://qlsukien-1.onrender.com/api/addParticipant";

            JSONObject postData = new JSONObject();
            try {
                // Gửi đủ dữ liệu bắt buộc
                postData.put("fullName", item.getFullName());
                postData.put("email", item.getEmail());
                postData.put("phone", item.getPhone());
                postData.put("eventName", item.getEventName());
                postData.put("startTime", item.getStartTime());
                postData.put("endTime", item.getEndTime());
                postData.put("status", "joined");

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(context, "Lỗi tạo dữ liệu JSON", Toast.LENGTH_SHORT).show();
                return;
            }

            RequestQueue queue = Volley.newRequestQueue(context);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, postData,
                    response -> {
                        Toast.makeText(context, "✅ Thêm participant thành công", Toast.LENGTH_LONG).show();
                    },
                    error -> {
                        String errorMsg = "Lỗi khi thêm participant";
                        if (error.networkResponse != null && error.networkResponse.data != null) {
                            try {
                                String data = new String(error.networkResponse.data);
                                JSONObject obj = new JSONObject(data);
                                if (obj.has("message")) errorMsg = obj.getString("message");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show();
                        Log.e("VOLLEY_ERROR", errorMsg, error);
                    }
            );

            queue.add(request);
        });




    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFullName, tvEmail, tvPhone, tvEventName, tvStartTime, tvEndTime, tvCreatedAt;
        Button btnUnregister, btnDelete, btnAdd;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFullName = itemView.findViewById(R.id.tvFullName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvStartTime = itemView.findViewById(R.id.tvStartTime);
            tvEndTime = itemView.findViewById(R.id.tvEndTime);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);

            btnUnregister = itemView.findViewById(R.id.btnUnregister);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnAdd = itemView.findViewById(R.id.btnAdd);
        }
    }



    // Hàm confirm hiển thị hộp thoại
    private void confirmDelete(SuKienDaDangKyAll item, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Xóa người tham gia")
                .setMessage("Bạn có chắc muốn xóa " + item.getFullName() + " không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    // Gọi API hủy đăng ký
                    unregisterEvent(item.getUserId(), item.getEventId(), position);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }



    // Hàm xóa
    private void unregisterEvent(String userId, String eventId, int position) {
        if(userId == null || userId.isEmpty() || eventId == null || eventId.isEmpty()) {
            Toast.makeText(context, "Thông tin không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }


        String url = "https://qlsukien-1.onrender.com/api/unregisterEvent";
        JSONObject postData = new JSONObject();
        try {

            postData.put("userId", userId);
            postData.put("eventId", eventId);
            Log.d("DEBUG_POSTDATA", postData.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(context, "Lỗi tạo dữ liệu JSON", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, postData,
                response -> {
                    Toast.makeText(context, "Xóa đăng ký thành công", Toast.LENGTH_SHORT).show();
                    if(position >= 0 && position < list.size()) {
                        list.remove(position);
                        notifyItemRemoved(position);
                    }
                },
                error -> {
                    String errorMsg = "Lỗi hủy đăng ký";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        try {
                            String data = new String(error.networkResponse.data);
                            JSONObject obj = new JSONObject(data);
                            if (obj.has("message")) errorMsg = obj.getString("message");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show();
                    Log.e("VOLLEY_ERROR", errorMsg, error);
                }
        );

        queue.add(request);
    }
}
