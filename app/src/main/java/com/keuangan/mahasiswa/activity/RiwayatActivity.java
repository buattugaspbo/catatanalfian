package com.keuangan.mahasiswa.activity;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.keuangan.mahasiswa.R;
import com.keuangan.mahasiswa.adapter.TransaksiAdapter;
import com.keuangan.mahasiswa.database.DatabaseHelper;
import com.keuangan.mahasiswa.model.Mahasiswa;
import com.keuangan.mahasiswa.model.Pemasukan;
import com.keuangan.mahasiswa.model.Pengeluaran;
import com.keuangan.mahasiswa.model.Tabungan;
import com.keuangan.mahasiswa.model.Transaksi;

import java.util.ArrayList;
import java.util.List;

// Activity untuk menampilkan seluruh riwayat transaksi pemasukan dan pengeluaran
public class RiwayatActivity extends AppCompatActivity implements TransaksiAdapter.OnItemLongClickListener {

    private DatabaseHelper dbHelper;
    private List<Transaksi> riwayatList;
    private TransaksiAdapter adapter;
    private int userId;

    private TextView tvEmptyState;
    private RecyclerView rvTransaksi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riwayat);

        // Ambil userId dari SharedPreferences
        SharedPreferences prefs = getSharedPreferences("keuangan_prefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        dbHelper = new DatabaseHelper(this);

        tvEmptyState = findViewById(R.id.tvEmptyState);
        rvTransaksi = findViewById(R.id.rvTransaksi);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        setupRecyclerView();
        loadRiwayatData();
    }

    private void setupRecyclerView() {
        riwayatList = new ArrayList<>();
        adapter = new TransaksiAdapter(riwayatList, this);
        rvTransaksi.setLayoutManager(new LinearLayoutManager(this));
        rvTransaksi.setAdapter(adapter);
    }

    private void loadRiwayatData() {
        riwayatList.clear();
        riwayatList.addAll(dbHelper.getAllTransaksi(userId));
        adapter.notifyDataSetChanged();

        if (riwayatList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvTransaksi.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvTransaksi.setVisibility(View.VISIBLE);
        }
    }

    // Callback ketika item transaksi ditekan lama untuk dihapus
    @Override
    public void onItemLongClick(Transaksi t) {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Transaksi")
                .setMessage("Apakah Anda yakin ingin menghapus transaksi ini? Saldo dan Tabungan Anda akan disesuaikan kembali.")
                .setPositiveButton("Hapus", (dialog, which) -> revertDanHapusTransaksi(t))
                .setNegativeButton("Batal", null)
                .show();
    }

    private void revertDanHapusTransaksi(Transaksi t) {
        Mahasiswa m = dbHelper.getMahasiswa(userId);
        Tabungan tab = dbHelper.getTabungan(userId);

        if (m == null || tab == null) {
            Toast.makeText(this, "Gagal memproses penghapusan!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mengembalikan saldo utama dan saldo tabungan ke kondisi sebelum transaksi dilakukan
        if (t instanceof Pemasukan) {
            Pemasukan pem = (Pemasukan) t;
            m.setSaldo(m.getSaldo() - t.getNominal());
            
            if ("Ambil Tabungan".equalsIgnoreCase(pem.getSumberPemasukan())) {
                tab.setSaldoTabungan(tab.getSaldoTabungan() + t.getNominal());
            } else if ("Uang Bulanan".equalsIgnoreCase(pem.getSumberPemasukan())) {
                m.setUangBulanan(Math.max(0, m.getUangBulanan() - t.getNominal()));
            }
        } else if (t instanceof Pengeluaran) {
            Pengeluaran peng = (Pengeluaran) t;
            m.setSaldo(m.getSaldo() + t.getNominal());
            
            if ("Tabungan".equalsIgnoreCase(peng.getKategori())) {
                tab.setSaldoTabungan(Math.max(0, tab.getSaldoTabungan() - t.getNominal()));
            }
        }

        // Menyimpan perubahan ke database SQLite berdasarkan userId dan menghapus data transaksi
        dbHelper.updateMahasiswa(m);
        dbHelper.updateTabungan(tab, userId);
        dbHelper.deleteTransaksi(t.getId());

        Toast.makeText(this, "Transaksi dihapus dan saldo disesuaikan!", Toast.LENGTH_SHORT).show();
        loadRiwayatData();
    }
}
