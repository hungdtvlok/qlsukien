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

public class SuKienDaDangKy2Adapter extends RecyclerView.Adapter<SuKienDaDangKy2Adapter.ViewHolder> {

    private Context context;
    private List<SuKienDaDangKy> list;

    public SuKienDaDangKy2Adapter(Context context, List<SuKienDaDangKy> list) {
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


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFullName, tvEmail, tvPhone, tvEventName, tvStartTime, tvEndTime, tvCreatedAt;
        Button btnUnregister;

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
        }
    }





}
