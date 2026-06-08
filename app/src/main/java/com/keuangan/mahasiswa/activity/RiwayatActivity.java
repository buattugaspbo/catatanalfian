package com.keuangan.mahasiswa.activity;

import android.app.AlertDialog;
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

/**
 * Logika halaman RiwayatActivity.
 * Menampilkan daftar transaksi riwayat.
 * Mendukung opsi hapus transaksi via Long Click dengan me-revert
 * saldo dompet utama dan tabungan secara aman.
 */
public class RiwayatActivity extends AppCompatActivity implements TransaksiAdapter.OnItemLongClickListener {

    private DatabaseHelper dbHelper;
    private List<Transaksi> riwayatList;
    private TransaksiAdapter adapter;

    private TextView tvEmptyState;
    private RecyclerView rvTransaksi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riwayat);

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
        riwayatList.addAll(dbHelper.getAllTransaksi());
        adapter.notifyDataSetChanged();

        if (riwayatList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvTransaksi.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvTransaksi.setVisibility(View.VISIBLE);
        }
    }

    // Callback long click untuk menghapus transaksi
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
        Mahasiswa m = dbHelper.getMahasiswa();
        Tabungan tab = dbHelper.getTabungan();

        // OOP Revert: Membalikkan efek transaksi pada Saldo dan Tabungan secara dinamis
        if (t instanceof Pemasukan) {
            Pemasukan pem = (Pemasukan) t;
            // Mengurangi kembali saldo berjalan utama
            m.setSaldo(m.getSaldo() - t.getNominal());
            
            // Revert tabungan jika asalnya menarik tabungan
            if ("Ambil Tabungan".equalsIgnoreCase(pem.getSumberPemasukan())) {
                tab.setSaldoTabungan(tab.getSaldoTabungan() + t.getNominal());
            } else if ("Uang Bulanan".equalsIgnoreCase(pem.getSumberPemasukan())) {
                m.setUangBulanan(Math.max(0, m.getUangBulanan() - t.getNominal()));
            }
        } else if (t instanceof Pengeluaran) {
            Pengeluaran peng = (Pengeluaran) t;
            // Menambahkan kembali saldo berjalan utama
            m.setSaldo(m.getSaldo() + t.getNominal());
            
            // Revert tabungan jika tujuannya menyetor tabungan
            if ("Tabungan".equalsIgnoreCase(peng.getKategori())) {
                tab.setSaldoTabungan(Math.max(0, tab.getSaldoTabungan() - t.getNominal()));
            }
        }

        // Update ke database lokal SQLite
        dbHelper.updateMahasiswa(m);
        dbHelper.updateTabungan(tab);
        dbHelper.deleteTransaksi(t.getId());

        Toast.makeText(this, "Transaksi dihapus dan saldo disesuaikan!", Toast.LENGTH_SHORT).show();
        loadRiwayatData();
    }
}
