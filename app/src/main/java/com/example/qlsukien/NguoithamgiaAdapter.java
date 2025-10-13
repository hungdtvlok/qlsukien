package com.example.qlsukien;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NguoithamgiaAdapter extends RecyclerView.Adapter<NguoithamgiaAdapter.ViewHolder> {

    private Context context;
    private List<Nguoithamgia> listNguoi;      // danh sách đang hiển thị
    private List<Nguoithamgia> listNguoiFull;  // danh sách gốc
    private OnItemClickListener listener;

    // interface cho Sửa/Xóa
    public interface OnItemClickListener {
        void onEdit(Nguoithamgia item);
        void onDelete(Nguoithamgia item);
    }

    public NguoithamgiaAdapter(Context context, List<Nguoithamgia> listNguoi, OnItemClickListener listener) {
        this.context = context;
        this.listNguoi = new ArrayList<>(listNguoi);
        this.listNguoiFull = new ArrayList<>(listNguoi);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_nguoithamgia, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Nguoithamgia item = listNguoi.get(position);

        holder.tvFullName.setText("Họ tên: " + item.getFullName());
        holder.tvEmail.setText("Email: " + item.getEmail());
        holder.tvPhone.setText("SĐT: " + item.getPhone());
        holder.tvEventName.setText("Sự kiện: " + item.getEventName());
        holder.tvStartTime.setText("Bắt đầu: " + item.getStartTime());
        holder.tvEndTime.setText("Kết thúc: " + item.getEndTime());
        holder.tvNguoiDangKy.setText("Người tham gia: " + item.getRegisteredBy());
        holder.tvCreatedAt.setText("Đăng ký lúc: " + item.getCreatedAt());

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(item);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(item);
        });
    }

    @Override
    public int getItemCount() {
        return listNguoi.size();
    }

    // Cập nhật danh sách
    public void updateList(List<Nguoithamgia> newList) {
        this.listNguoi.clear();
        this.listNguoi.addAll(newList);
        this.listNguoiFull.clear();
        this.listNguoiFull.addAll(newList);
        notifyDataSetChanged();
    }

    // Tìm kiếm theo sự kiện, SĐT, hoặc tên
    public void filter(String text) {
        List<Nguoithamgia> filteredList = new ArrayList<>();

        // Chia input thành nhiều từ khóa (cách nhau bởi dấu cách)
        String[] keywords = text.toLowerCase().split("\\s+");

        for (Nguoithamgia item : listNguoiFull) {
            boolean matchAll = true;

            // Duyệt qua từng từ khóa -> mỗi từ khóa phải tìm thấy trong ít nhất 1 field
            for (String keyword : keywords) {
                if (!(item.getFullName().toLowerCase().contains(keyword) ||
                        item.getEmail().toLowerCase().contains(keyword) ||
                        item.getPhone().toLowerCase().contains(keyword) ||
                        item.getEventName().toLowerCase().contains(keyword) ||
                        item.getStartTime().toLowerCase().contains(keyword) ||
                        item.getEndTime().toLowerCase().contains(keyword) ||
                        item.getRegisteredBy().toLowerCase().contains(keyword) ||
                        item.getCreatedAt().toLowerCase().contains(keyword))) {
                    matchAll = false;
                    break;
                }
            }

            if (matchAll) {
                filteredList.add(item);
            }
        }

        listNguoi.clear();
        listNguoi.addAll(filteredList);
        notifyDataSetChanged();
    }




    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFullName, tvEmail, tvPhone, tvEventName,
                tvStartTime, tvEndTime, tvNguoiDangKy, tvCreatedAt;
        Button btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFullName = itemView.findViewById(R.id.tvFullName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvStartTime = itemView.findViewById(R.id.tvStartTime);
            tvEndTime = itemView.findViewById(R.id.tvEndTime);
            tvNguoiDangKy = itemView.findViewById(R.id.tvNguoiDangKy);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
