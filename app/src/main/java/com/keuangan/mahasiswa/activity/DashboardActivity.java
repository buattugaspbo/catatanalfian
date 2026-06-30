package com.keuangan.mahasiswa.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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

// Activity untuk halaman dashboard utama aplikasi
public class DashboardActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private Mahasiswa mahasiswa;
    private Tabungan tabungan;
    private int userId;

    private TextView tvAvatar, tvStudentName, tvStudentNim, tvFinanceStatus;
    private TextView tvSaldo, tvUangBulanan, tvTotalPengeluaran, tvTotalTabungan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Ambil userId dari SharedPreferences
        SharedPreferences prefs = getSharedPreferences("keuangan_prefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        if (userId == -1) {
            // Jika tidak ada session, kembali ke Login
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        dbHelper = new DatabaseHelper(this);
        initViews();
        setupNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Memperbarui data dashboard setiap kali activity dilanjutkan (onResume)
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
        // Mengambil data mahasiswa, tabungan, dan daftar transaksi dari database berdasarkan userId
        mahasiswa = dbHelper.getMahasiswa(userId);
        if (mahasiswa == null) {
            Toast.makeText(this, "Gagal memuat data pengguna!", Toast.LENGTH_SHORT).show();
            logout();
            return;
        }

        tabungan = dbHelper.getTabungan(userId);
        List<Transaksi> transaksiList = dbHelper.getAllTransaksi(userId);

        // Menampilkan profil mahasiswa dan menentukan inisial avatar
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

        // Menghitung akumulasi total pengeluaran
        LaporanKeuangan laporan = new LaporanKeuangan();
        double totalPengeluaran = laporan.hitungTotalPengeluaran(transaksiList);

        // Menampilkan informasi keuangan mahasiswa ke UI dengan format pemisah ribuan
        tvSaldo.setText(FormatRupiah.format(mahasiswa.getSaldo()));
        tvUangBulanan.setText(FormatRupiah.format(mahasiswa.getUangBulanan()));
        tvTotalPengeluaran.setText(FormatRupiah.format(totalPengeluaran));
        tvTotalTabungan.setText(FormatRupiah.format(tabungan.getSaldoTabungan()));

        // Menentukan status keuangan berdasarkan rasio total pengeluaran terhadap uang bulanan
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
        
        // Membuat bentuk background rounded corner secara programmatis
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(color);
        gd.setCornerRadius(16);
        tvFinanceStatus.setBackground(gd);
    }

    private void setupNavigation() {
        // Mengatur penanganan klik untuk navigasi ke berbagai menu activity
        findViewById(R.id.cardProfile).setOnClickListener(v -> showProfileDialog());

        findViewById(R.id.menuUangBulanan).setOnClickListener(v -> 
            startActivity(new Intent(DashboardActivity.this, InputUangBulananActivity.class))
        );

        findViewById(R.id.menuRencana).setOnClickListener(v -> 
            startActivity(new Intent(DashboardActivity.this, RencanaPengeluaranActivity.class))
        );

        findViewById(R.id.menuCatatPengeluaran).setOnClickListener(v -> 
            startActivity(new Intent(DashboardActivity.this, TambahPengeluaranActivity.class))
        );

        findViewById(R.id.menuTabungan).setOnClickListener(v -> 
            startActivity(new Intent(DashboardActivity.this, TabunganActivity.class))
        );

        findViewById(R.id.menuRiwayat).setOnClickListener(v -> 
            startActivity(new Intent(DashboardActivity.this, RiwayatActivity.class))
        );

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
        com.google.android.material.button.MaterialButton btnLogout = dialogView.findViewById(R.id.btnLogoutUser);

        // Memuat data mahasiswa saat ini ke form edit profil
        Mahasiswa m = dbHelper.getMahasiswa(userId);
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
                        Toast.makeText(this, "Semua field profil harus diisi!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    m.setNama(namaBaru);
                    m.setNim(nimBaru);
                    m.setEmail(emailBaru);

                    dbHelper.updateMahasiswa(m);
                    Toast.makeText(this, "Profil berhasil diperbarui!", Toast.LENGTH_SHORT).show();
                    loadDashboardData();
                })
                .setNegativeButton("Batal", null)
                .create();

        btnReset.setOnClickListener(view -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Reset Semua Data")
                    .setMessage("Apakah Anda yakin ingin menghapus semua rencana anggaran, tabungan, dan riwayat transaksi? Tindakan ini tidak bisa dibatalkan.")
                    .setPositiveButton("Ya, Hapus Semua", (dialogInterface, i) -> {
                        dbHelper.resetDatabase(userId);
                        dialog.dismiss();
                        Toast.makeText(this, "Semua data berhasil direset!", Toast.LENGTH_SHORT).show();
                        loadDashboardData();
                    })
                    .setNegativeButton("Batal", null)
                    .show();
        });

        btnLogout.setOnClickListener(view -> {
            dialog.dismiss();
            logout();
        });

        dialog.show();
    }

    private void logout() {
        // Hapus session dan kembali ke LoginActivity
        SharedPreferences prefs = getSharedPreferences("keuangan_prefs", MODE_PRIVATE);
        prefs.edit().clear().apply();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
