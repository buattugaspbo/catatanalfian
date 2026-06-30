package com.keuangan.mahasiswa.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.keuangan.mahasiswa.R;
import com.keuangan.mahasiswa.database.DatabaseHelper;
import com.keuangan.mahasiswa.model.Mahasiswa;
import com.keuangan.mahasiswa.model.Pemasukan;
import com.keuangan.mahasiswa.model.Tabungan;
import com.keuangan.mahasiswa.utils.ValidasiInput;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// Activity untuk memasukkan data uang bulanan mahasiswa
public class InputUangBulananActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private EditText etNominal;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_uang_bulanan);

        // Ambil userId dari SharedPreferences
        SharedPreferences prefs = getSharedPreferences("keuangan_prefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        dbHelper = new DatabaseHelper(this);
        etNominal = findViewById(R.id.etNominalUangBulanan);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnSimpanUangBulanan).setOnClickListener(v -> simpanUangBulanan());
    }

    private void simpanUangBulanan() {
        String nominalStr = etNominal.getText().toString().trim();

        // Validasi input form nominal uang bulanan
        if (ValidasiInput.isEmpty(nominalStr)) {
            Toast.makeText(this, "Nominal tidak boleh kosong!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!ValidasiInput.isNumber(nominalStr)) {
            Toast.makeText(this, "Nominal harus berupa angka!", Toast.LENGTH_SHORT).show();
            return;
        }

        double nominal = Double.parseDouble(nominalStr);
        if (!ValidasiInput.isPositive(nominal)) {
            Toast.makeText(this, "Nominal harus lebih dari 0!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mengambil data mahasiswa dan tabungan saat ini dari database berdasarkan userId
        Mahasiswa m = dbHelper.getMahasiswa(userId);
        Tabungan t = dbHelper.getTabungan(userId);

        if (m == null || t == null) {
            Toast.makeText(this, "Gagal mendapatkan data user!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mendapatkan tanggal hari ini
        String tanggal = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());

        // Inisialisasi objek Pemasukan
        Pemasukan pemasukan = new Pemasukan(0, tanggal, nominal, "Penerimaan Uang Bulanan", "Uang Bulanan");

        // Memproses penyesuaian saldo dan uang bulanan
        pemasukan.prosesTransaksi(m, t);
        m.setUangBulanan(nominal);

        // Menyimpan perubahan ke database SQLite
        dbHelper.updateMahasiswa(m);
        dbHelper.insertTransaksi(pemasukan, userId);

        Toast.makeText(this, "Uang bulanan berhasil disimpan!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
