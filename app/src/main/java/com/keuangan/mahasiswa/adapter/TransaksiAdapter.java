package com.keuangan.mahasiswa.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.keuangan.mahasiswa.R;
import com.keuangan.mahasiswa.model.Pemasukan;
import com.keuangan.mahasiswa.model.Pengeluaran;
import com.keuangan.mahasiswa.model.Transaksi;
import com.keuangan.mahasiswa.utils.FormatRupiah;

import java.util.List;

/**
 * Konsep PBO: Polymorphism
 * TransaksiAdapter menampung list bertipe superclass Transaksi.
 * Saat bind data, JVM secara dinamis memanggil method override t.getDetailInfo()
 * dan memilah visual warna/tanda nominal tergantung subclass riilnya.
 */
public class TransaksiAdapter extends RecyclerView.Adapter<TransaksiAdapter.ViewHolder> {

    private final List<Transaksi> list;
    private final OnItemLongClickListener listener;

    public interface OnItemLongClickListener {
        void onItemLongClick(Transaksi t);
    }

    public TransaksiAdapter(List<Transaksi> list, OnItemLongClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaksi, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaksi t = list.get(position);
        Context ctx = holder.itemView.getContext();

        holder.tvDescription.setText(t.getKeterangan());
        holder.tvDate.setText(t.getTanggal());
        
        // PBO: Polymorphism - Dynamic Method Dispatch
        holder.tvDetailInfo.setText(t.getDetailInfo());
        
        String prefix = "";
        int colorRes = R.color.text_primary;
        
        if (t instanceof Pemasukan) {
            prefix = "+ ";
            colorRes = R.color.income;
        } else if (t instanceof Pengeluaran) {
            prefix = "- ";
            colorRes = R.color.expense;
        }
        
        int color = ContextCompat.getColor(ctx, colorRes);
        holder.tvAmount.setText(prefix + FormatRupiah.format(t.getNominal()));
        holder.tvAmount.setTextColor(color);
        holder.viewIndicator.setBackgroundTintList(ColorStateList.valueOf(color));

        // Click listener tahan lama untuk opsi Hapus
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (listener != null) {
                    listener.onItemLongClick(t);
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
        View viewIndicator;
        TextView tvDescription, tvDetailInfo, tvDate, tvAmount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            viewIndicator = itemView.findViewById(R.id.viewTypeIndicator);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvDetailInfo = itemView.findViewById(R.id.tvDetailInfo);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvAmount = itemView.findViewById(R.id.tvAmount);
        }
    }
}
