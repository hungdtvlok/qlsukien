package com.example.qlsukien;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.List;
import android.util.Log;
import org.json.JSONException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SuKienDaDangKyAdapter extends RecyclerView.Adapter<SuKienDaDangKyAdapter.ViewHolder> {

    private Context context;
    private List<SuKienDaDangKy> list;

    private boolean canSuaxoa = false;

    public SuKienDaDangKyAdapter(Context context, List<SuKienDaDangKy> list) {
        this.context = context;
        this.list = list;
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
        SuKienDaDangKy item = list.get(position);

        holder.tvFullName.setText("Họ tên: " + item.getFullName());
        holder.tvEmail.setText("Email: " + item.getEmail());
        holder.tvPhone.setText("SĐT: " + item.getPhone());
        holder.tvEventName.setText("Sự kiện: " + item.getEventName());
        holder.tvStartTime.setText("Bắt đầu: " + SuKienDaDangKyActivity.formatDateForSearch(item.getStartTime()));
        holder.tvEndTime.setText("Kết thúc: " + SuKienDaDangKyActivity.formatDateForSearch(item.getEndTime()));
        holder.tvCreatedAt.setText("Đăng ký lúc: " + SuKienDaDangKyActivity.formatDateForSearch(item.getCreatedAt()));

        // Gọi gửi email nếu trước 2 tiếng
        sendEmailIfBefore2Hours(item);

        // ✅ Ẩn nút HỦY nếu status = "joined"
        if ("joined".equalsIgnoreCase(item.getStatus())) {
            holder.btnUnregister.setVisibility(View.GONE); // ẩn nút
        } else {
            holder.btnUnregister.setVisibility(View.VISIBLE);
            holder.btnUnregister.setOnClickListener(v -> {
                unregisterEvent(item.getUserId(), item.getEventId(), position);
            });
        }

        holder.btnDelete.setVisibility(canSuaxoa ? View.VISIBLE : View.GONE);
        holder.btnAdd.setVisibility(canSuaxoa ? View.VISIBLE : View.GONE);
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

    // Hàm hủy đăng ký sự kiện
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
                    Toast.makeText(context, "Hủy đăng ký thành công", Toast.LENGTH_SHORT).show();
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
    // hàm gửi về gmail
    private void sendEmailIfBefore2Hours(SuKienDaDangKy registration) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date startDate = sdf.parse(registration.getStartTime());
            Date now = new Date();
            long diff = startDate.getTime() - now.getTime();

            // Chỉ gửi nếu trước 2 tiếng và chưa gửi
            if (diff > 0 && diff <= 2 * 60 * 60 * 1000 && !registration.isEmailSent()) {
                sendEmailToUser(
                        registration.getEmail(),
                        registration.getEventName(),
                        registration.getStartTime(),
                        registration.getLocation()
                );

                // Có thể set flag để tránh gửi lại
                registration.setEmailSent(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendEmailToUser(String email, String eventName, String startTime, String location) {
        String url = "https://qlsukien-1.onrender.com/api/sendReminderEmail";
        JSONObject body = new JSONObject();
        try {
            body.put("email", email);
            body.put("eventName", eventName);
            body.put("startTime", startTime);
            body.put("location", location);
        } catch (Exception e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, body,
                response -> Log.d("Email", "Gửi email thành công: " + email),
                error -> Log.e("Email", "Gửi email thất bại: " + email)
        );

        Volley.newRequestQueue(context).add(request);
    }




}
