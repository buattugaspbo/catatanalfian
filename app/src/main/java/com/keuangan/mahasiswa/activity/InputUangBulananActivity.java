package com.keuangan.mahasiswa.activity;

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

/**
 * Logika halaman InputUangBulananActivity.
 * Memproses penyimpanan nominal uang bulanan ke database lokal
 * dan mencatatnya sebagai transaksi pemasukan polimorfik secara otomatis.
 */
public class InputUangBulananActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private EditText etNominal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_uang_bulanan);

        dbHelper = new DatabaseHelper(this);
        etNominal = findViewById(R.id.etNominalUangBulanan);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnSimpanUangBulanan).setOnClickListener(v -> simpanUangBulanan());
    }

    private void simpanUangBulanan() {
        String nominalStr = etNominal.getText().toString().trim();

        // 1. Validasi input menggunakan kelas utilitas
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

        // 2. Load objek Mahasiswa dan Tabungan dari DB
        Mahasiswa m = dbHelper.getMahasiswa();
        Tabungan t = dbHelper.getTabungan();

        // 3. Tentukan tanggal hari ini
        String tanggal = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());

        // 4. Instansiasi Pemasukan secara polimorfis
        // Menggunakan konstruktor dengan enkapsulasi data
        Pemasukan pemasukan = new Pemasukan(0, tanggal, nominal, "Penerimaan Uang Bulanan", "Uang Bulanan");

        // 5. Jalankan logika prosesTransaksi secara OOP
        pemasukan.prosesTransaksi(m, t);

        // 6. Update Uang Bulanan mahasiswa ke nominal baru
        m.setUangBulanan(nominal);

        // 7. Simpan perubahan ke database SQLite lokal
        dbHelper.updateMahasiswa(m);
        dbHelper.insertTransaksi(pemasukan);

        Toast.makeText(this, "Uang bulanan berhasil disimpan!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
