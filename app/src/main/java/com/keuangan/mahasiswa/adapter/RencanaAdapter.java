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

/**
 * Adapter untuk merender daftar rencana anggaran belanja mahasiswa dalam RecyclerView.
 */
public class RencanaAdapter extends RecyclerView.Adapter<RencanaAdapter.ViewHolder> {

    private final List<RencanaPengeluaran> list;

    public RencanaAdapter(List<RencanaPengeluaran> list) {
        this.list = list;
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
