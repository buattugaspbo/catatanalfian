package com.keuangan.mahasiswa.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.keuangan.mahasiswa.R;
import com.keuangan.mahasiswa.model.RencanaPengeluaran;
import com.keuangan.mahasiswa.utils.FormatRupiah;

import java.util.List;

// Adapter untuk menampilkan daftar rencana anggaran dalam RecyclerView
public class RencanaAdapter extends RecyclerView.Adapter<RencanaAdapter.ViewHolder> {

    private final List<RencanaPengeluaran> list;
    private final OnItemLongClickListener listener;

    // Interface untuk menangani aksi long click pada item rencana
    public interface OnItemLongClickListener {
        void onItemLongClick(RencanaPengeluaran rp);
    }

    public RencanaAdapter(List<RencanaPengeluaran> list, OnItemLongClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rencana, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RencanaPengeluaran rp = list.get(position);
        holder.tvKategori.setText(rp.getKategori());
        holder.tvNominal.setText(FormatRupiah.format(rp.getNominalRencana()));

        // Listener untuk mendeteksi long click pada item list
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (listener != null) {
                    listener.onItemLongClick(rp);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvKategori, tvNominal;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvKategori = itemView.findViewById(R.id.tvRencanaKategori);
            tvNominal = itemView.findViewById(R.id.tvRencanaNominal);
        }
    }
}
