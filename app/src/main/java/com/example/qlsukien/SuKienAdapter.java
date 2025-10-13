package com.example.qlsukien;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class SuKienAdapter extends RecyclerView.Adapter<SuKienAdapter.SuKienViewHolder> {

    private Context context;
    private List<SuKien> suKienList;
    private SuKienAdapterListener listener;

    private boolean canEdit = false;     // ẩn/hiện nút sửa, xóa
    private boolean canRegister = false; // ẩn/hiện nút đăng ký

    public interface SuKienAdapterListener {
        void onEdit(SuKien suKien);
        void onDelete(SuKien suKien);
        void onRegister(SuKien suKien);  // thêm callback cho nút Đăng ký
    }

    public SuKienAdapter(Context context, List<SuKien> suKienList,
                         SuKienAdapterListener listener,
                         boolean canEdit, boolean canRegister) {
        this.context = context;
        this.suKienList = suKienList;
        this.listener = listener;
        this.canEdit = canEdit;
        this.canRegister = canRegister;
    }

    @Override
    public SuKienViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_sukien, parent, false);
        return new SuKienViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SuKienViewHolder holder, int position) {
        SuKien sk = suKienList.get(position);

        holder.txtName.setText("Tên sự kiện: " + sk.getName());
        holder.txtStartDate.setText("Ngày bắt đầu: " + formatDate(sk.getStartTime()));
        holder.txtEndDate.setText("Ngày kết thúc: " + formatDate(sk.getEndTime()));
        holder.txtLocation.setText("Địa điểm: " + sk.getLocation());
        holder.txtDesc.setText("Mô tả: " + sk.getDescription());

        // Ẩn/hiện các nút theo quyền
        holder.btnEdit.setVisibility(canEdit ? View.VISIBLE : View.GONE);
        holder.btnDelete.setVisibility(canEdit ? View.VISIBLE : View.GONE);
        holder.btnDk.setVisibility(canRegister ? View.VISIBLE : View.GONE);

        // Xử lý click các nút
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(sk));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(sk));
        holder.btnDk.setOnClickListener(v -> listener.onRegister(sk));
    }

    private String formatDate(String isoDate) {
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            Date date = isoFormat.parse(isoDate);

            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            return outputFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return isoDate;
        }
    }

    @Override
    public int getItemCount() {
        return suKienList.size();
    }

    static class SuKienViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtStartDate, txtEndDate, txtLocation, txtDesc;
        Button btnEdit, btnDelete, btnDk;

        public SuKienViewHolder(View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);
            txtStartDate = itemView.findViewById(R.id.txtStartDate);
            txtEndDate = itemView.findViewById(R.id.txtEndDate);
            txtLocation = itemView.findViewById(R.id.txtLocation);
            txtDesc = itemView.findViewById(R.id.txtDesc);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnDk = itemView.findViewById(R.id.btnDk); // ánh xạ nút Đăng ký
        }
    }
}
