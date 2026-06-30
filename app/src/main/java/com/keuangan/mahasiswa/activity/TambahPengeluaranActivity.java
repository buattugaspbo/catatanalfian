package com.keuangan.mahasiswa.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.keuangan.mahasiswa.R;
import com.keuangan.mahasiswa.database.DatabaseHelper;
import com.keuangan.mahasiswa.model.Mahasiswa;
import com.keuangan.mahasiswa.model.Pengeluaran;
import com.keuangan.mahasiswa.model.Tabungan;
import com.keuangan.mahasiswa.utils.FormatRupiah;
import com.keuangan.mahasiswa.utils.ValidasiInput;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// Activity untuk menambahkan data pengeluaran baru
public class TambahPengeluaranActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private Mahasiswa mahasiswa;
    private Tabungan tabungan;
    private int userId;

    private EditText etKeterangan, etNominal, etAlasan;
    private Spinner spKategori, spTingkatKebutuhan;
    private TextView tvSaldoTersedia;

    private final String[] daftarKategori = {
            "Makan", "Kos", "Transportasi", "Internet", "Tugas Kuliah", "Hiburan", "Tabungan"
    };

    private final String[] daftarKebutuhan = {
            "Rendah", "Sedang", "Penting", "Darurat"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_pengeluaran);

        // Ambil userId dari SharedPreferences
        SharedPreferences prefs = getSharedPreferences("keuangan_prefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        dbHelper = new DatabaseHelper(this);
        mahasiswa = dbHelper.getMahasiswa(userId);
        tabungan = dbHelper.getTabungan(userId);

        initViews();
        setupSpinners();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnCatatPengeluaran).setOnClickListener(v -> catatPengeluaran());

        if (mahasiswa != null) {
            tvSaldoTersedia.setText(FormatRupiah.format(mahasiswa.getSaldo()));
        }
    }

    private void initViews() {
        etKeterangan = findViewById(R.id.etKeterangan);
        etNominal = findViewById(R.id.etNominal);
        etAlasan = findViewById(R.id.etAlasan);
        spKategori = findViewById(R.id.spKategori);
        spTingkatKebutuhan = findViewById(R.id.spTingkatKebutuhan);
        tvSaldoTersedia = findViewById(R.id.tvSaldoTersedia);
    }

    private void setupSpinners() {
        ArrayAdapter<String> katAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, daftarKategori
        );
        katAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spKategori.setAdapter(katAdapter);

        ArrayAdapter<String> kebAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, daftarKebutuhan
        );
        kebAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTingkatKebutuhan.setAdapter(kebAdapter);
    }

    private void catatPengeluaran() {
        String keterangan = etKeterangan.getText().toString().trim();
        String nominalStr = etNominal.getText().toString().trim();
        String kategori = spKategori.getSelectedItem().toString();
        String alasan = etAlasan.getText().toString().trim();
        String tingkatKebutuhan = spTingkatKebutuhan.getSelectedItem().toString();

        // Validasi input data pengeluaran harian
        if (ValidasiInput.isEmpty(keterangan) || ValidasiInput.isEmpty(nominalStr) || ValidasiInput.isEmpty(alasan)) {
            Toast.makeText(this, "Semua kolom input wajib diisi!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!ValidasiInput.isNumber(nominalStr)) {
            Toast.makeText(this, "Nominal pengeluaran harus berupa angka!", Toast.LENGTH_SHORT).show();
            return;
        }

        double nominal = Double.parseDouble(nominalStr);
        if (!ValidasiInput.isPositive(nominal)) {
            Toast.makeText(this, "Nominal pengeluaran harus lebih besar dari 0!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Menentukan tanggal transaksi hari ini
        String tanggal = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());

        // Inisialisasi objek Pengeluaran
        Pengeluaran pengeluaran;
        try {
            pengeluaran = new Pengeluaran(0, tanggal, nominal, keterangan, kategori, alasan, tingkatKebutuhan);
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        // Memproses transaksi pengeluaran untuk memperbarui saldo
        try {
            pengeluaran.prosesTransaksi(mahasiswa, tabungan);
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        // Menyimpan data pengeluaran dan pembaruan saldo ke database SQLite berdasarkan userId
        dbHelper.updateMahasiswa(mahasiswa);
        if ("Tabungan".equalsIgnoreCase(kategori)) {
            dbHelper.updateTabungan(tabungan, userId);
        }
        dbHelper.insertTransaksi(pengeluaran, userId);

        Toast.makeText(this, "Pengeluaran berhasil dicatat!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
