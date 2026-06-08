package com.keuangan.mahasiswa.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;
import com.keuangan.mahasiswa.R;
import com.keuangan.mahasiswa.database.DatabaseHelper;
import com.keuangan.mahasiswa.model.LaporanKeuangan;
import com.keuangan.mahasiswa.model.Mahasiswa;
import com.keuangan.mahasiswa.model.Tabungan;
import com.keuangan.mahasiswa.model.Transaksi;
import com.keuangan.mahasiswa.utils.FormatRupiah;

import java.util.List;

/**
 * Halaman utama Dashboard aplikasi.
 * Menampilkan ringkasan profil, saldo berjalan, uang bulanan, total pengeluaran,
 * tabungan, status keuangan mahasiswa, dan menu navigasi.
 */
public class DashboardActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private Mahasiswa mahasiswa;
    private Tabungan tabungan;

    private TextView tvAvatar, tvStudentName, tvStudentNim, tvFinanceStatus;
    private TextView tvSaldo, tvUangBulanan, tvTotalPengeluaran, tvTotalTabungan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        dbHelper = new DatabaseHelper(this);
        initViews();
        setupNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Selalu segarkan data ketika kembali ke dashboard
        loadDashboardData();
    }

    private void initViews() {
        tvAvatar = findViewById(R.id.tvAvatar);
        tvStudentName = findViewById(R.id.tvStudentName);
        tvStudentNim = findViewById(R.id.tvStudentNim);
        tvFinanceStatus = findViewById(R.id.tvFinanceStatus);
        
        tvSaldo = findViewById(R.id.tvSaldo);
        tvUangBulanan = findViewById(R.id.tvUangBulanan);
        tvTotalPengeluaran = findViewById(R.id.tvTotalPengeluaran);
        tvTotalTabungan = findViewById(R.id.tvTotalTabungan);
    }

    private void loadDashboardData() {
        // 1. Ambil data dari SQLite lokal
        mahasiswa = dbHelper.getMahasiswa();
        tabungan = dbHelper.getTabungan();
        List<Transaksi> transaksiList = dbHelper.getAllTransaksi();

        // 2. Tampilkan Profil & Inisial Avatar
        String nama = mahasiswa.getNama();
        tvStudentName.setText(nama);
        tvStudentNim.setText("NIM: " + mahasiswa.getNim());
        
        if (nama != null && !nama.isEmpty()) {
            String[] parts = nama.split("\\s+");
            StringBuilder initial = new StringBuilder();
            for (int i = 0; i < Math.min(parts.length, 2); i++) {
                if (!parts[i].isEmpty()) {
                    initial.append(parts[i].substring(0, 1).toUpperCase());
                }
            }
            tvAvatar.setText(initial.toString());
        }

        // 3. Hitung total pengeluaran secara polimorfik menggunakan model LaporanKeuangan
        LaporanKeuangan laporan = new LaporanKeuangan();
        double totalPengeluaran = laporan.hitungTotalPengeluaran(transaksiList);

        // 4. Update data saldo dan uang bulanan di UI
        tvSaldo.setText(FormatRupiah.format(mahasiswa.getSaldo()));
        tvUangBulanan.setText(FormatRupiah.format(mahasiswa.getUangBulanan()));
        tvTotalPengeluaran.setText(FormatRupiah.format(totalPengeluaran));
        tvTotalTabungan.setText(FormatRupiah.format(tabungan.getSaldoTabungan()));

        // 5. Tentukan Status Keuangan
        double uangBulanan = mahasiswa.getUangBulanan();
        if (uangBulanan == 0) {
            updateFinanceStatus("Aman", R.color.income);
        } else {
            double ratio = totalPengeluaran / uangBulanan;
            if (ratio < 0.5) {
                updateFinanceStatus("Aman", R.color.income);
            } else if (ratio >= 0.5 && ratio <= 0.8) {
                updateFinanceStatus("Waspada", R.color.accent);
            } else {
                updateFinanceStatus("Boros", R.color.expense);
            }
        }
    }

    private void updateFinanceStatus(String status, int colorResId) {
        tvFinanceStatus.setText(status);
        int color = ContextCompat.getColor(this, colorResId);
        tvFinanceStatus.setBackgroundColor(color);
        tvFinanceStatus.setTextColor(Color.WHITE);
        // Buat background rounded corner sederhana programmatically
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(color);
        gd.setCornerRadius(16);
        tvFinanceStatus.setBackground(gd);
    }

    private void setupNavigation() {
        // Navigasi ke Pengaturan Profil & Reset Data
        findViewById(R.id.cardProfile).setOnClickListener(v -> showProfileDialog());

        // Navigasi ke Halaman Uang Bulanan
        findViewById(R.id.menuUangBulanan).setOnClickListener(v -> 
            startActivity(new Intent(DashboardActivity.this, InputUangBulananActivity.class))
        );

        // Navigasi ke Halaman Rencana Pengeluaran
        findViewById(R.id.menuRencana).setOnClickListener(v -> 
            startActivity(new Intent(DashboardActivity.this, RencanaPengeluaranActivity.class))
        );

        // Navigasi ke Halaman Tambah Pengeluaran
        findViewById(R.id.menuCatatPengeluaran).setOnClickListener(v -> 
            startActivity(new Intent(DashboardActivity.this, TambahPengeluaranActivity.class))
        );

        // Navigasi ke Halaman Tabungan
        findViewById(R.id.menuTabungan).setOnClickListener(v -> 
            startActivity(new Intent(DashboardActivity.this, TabunganActivity.class))
        );

        // Navigasi ke Halaman Riwayat Transaksi
        findViewById(R.id.menuRiwayat).setOnClickListener(v -> 
            startActivity(new Intent(DashboardActivity.this, RiwayatActivity.class))
        );

        // Navigasi ke Halaman Laporan Keuangan
        findViewById(R.id.menuLaporan).setOnClickListener(v -> 
            startActivity(new Intent(DashboardActivity.this, LaporanActivity.class))
        );
    }

    private void showProfileDialog() {
        android.view.LayoutInflater inflater = android.view.LayoutInflater.from(this);
        android.view.View dialogView = inflater.inflate(R.layout.dialog_profile, null);

        android.widget.EditText etNama = dialogView.findViewById(R.id.etProfileNama);
        android.widget.EditText etNim = dialogView.findViewById(R.id.etProfileNim);
        android.widget.EditText etEmail = dialogView.findViewById(R.id.etProfileEmail);
        com.google.android.material.button.MaterialButton btnReset = dialogView.findViewById(R.id.btnResetData);

        // Load current data
        Mahasiswa m = dbHelper.getMahasiswa();
        etNama.setText(m.getNama());
        etNim.setText(m.getNim());
        etEmail.setText(m.getEmail());

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Simpan", (dialogInterface, i) -> {
                    String namaBaru = etNama.getText().toString().trim();
                    String nimBaru = etNim.getText().toString().trim();
                    String emailBaru = etEmail.getText().toString().trim();

                    if (namaBaru.isEmpty() || nimBaru.isEmpty() || emailBaru.isEmpty()) {
                        android.widget.Toast.makeText(this, "Semua field profil harus diisi!", android.widget.Toast.LENGTH_SHORT).show();
                        return;
                    }

                    m.setNama(namaBaru);
                    m.setNim(nimBaru);
                    m.setEmail(emailBaru);

                    dbHelper.updateMahasiswa(m);
                    android.widget.Toast.makeText(this, "Profil berhasil diperbarui!", android.widget.Toast.LENGTH_SHORT).show();
                    loadDashboardData();
                })
                .setNegativeButton("Batal", null)
                .create();

        btnReset.setOnClickListener(view -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Reset Semua Data")
                    .setMessage("Apakah Anda yakin ingin menghapus semua rencana anggaran, tabungan, dan riwayat transaksi? Tindakan ini tidak bisa dibatalkan.")
                    .setPositiveButton("Ya, Hapus Semua", (dialogInterface, i) -> {
                        dbHelper.resetDatabase();
                        dialog.dismiss();
                        android.widget.Toast.makeText(this, "Semua data berhasil direset!", android.widget.Toast.LENGTH_SHORT).show();
                        loadDashboardData();
                    })
                    .setNegativeButton("Batal", null)
                    .show();
        });

        dialog.show();
    }
}
