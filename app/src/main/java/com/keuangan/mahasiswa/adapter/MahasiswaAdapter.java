package com.keuangan.mahasiswa.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.keuangan.mahasiswa.R;
import com.keuangan.mahasiswa.model.Mahasiswa;

import java.util.List;

// Adapter RecyclerView untuk menampilkan daftar mahasiswa di halaman Admin Dashboard
public class MahasiswaAdapter extends RecyclerView.Adapter<MahasiswaAdapter.ViewHolder> {

    private final List<Mahasiswa> list;
    private final OnItemClickListener listener;

    // Interface untuk menangani klik pada item mahasiswa di daftar admin
    public interface OnItemClickListener {
        void onItemClick(Mahasiswa m);
    }

    public MahasiswaAdapter(List<Mahasiswa> list, OnItemClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mahasiswa, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Mahasiswa m = list.get(position);

        // Menentukan inisial avatar dari nama mahasiswa
        String nama = m.getNama();
        String inisial = "";
        if (nama != null && !nama.isEmpty()) {
            String[] parts = nama.split("\\s+");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < Math.min(parts.length, 2); i++) {
                if (!parts[i].isEmpty()) sb.append(parts[i].substring(0, 1).toUpperCase());
            }
            inisial = sb.toString();
        }

        holder.tvAvatar.setText(inisial);
        holder.tvNama.setText(m.getNama());
        holder.tvNim.setText("NIM: " + m.getNim());
        holder.tvEmail.setText(m.getEmail());

        // Listener klik untuk membuka menu aksi admin pada mahasiswa terpilih
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(m);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatar, tvNama, tvNim, tvEmail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.tvMahasiswaAvatar);
            tvNama = itemView.findViewById(R.id.tvMahasiswaNama);
            tvNim = itemView.findViewById(R.id.tvMahasiswaNim);
            tvEmail = itemView.findViewById(R.id.tvMahasiswaEmail);
        }
    }
}
