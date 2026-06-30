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
import com.keuangan.mahasiswa.model.Pemasukan;
import com.keuangan.mahasiswa.model.Pengeluaran;
import com.keuangan.mahasiswa.model.Tabungan;
import com.keuangan.mahasiswa.utils.FormatRupiah;
import com.keuangan.mahasiswa.utils.ValidasiInput;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// Activity untuk mengelola proses penyetoran dan penarikan uang tabungan
public class TabunganActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private Mahasiswa mahasiswa;
    private Tabungan tabungan;
    private int userId;

    private TextView tvSaldoTabungan, tvSaldoDompet;
    private EditText etNominalTambah, etNominalAmbil, etAlasanAmbil;
    private Spinner spTingkatDarurat;

    private final String[] daftarDarurat = {
            "Rendah", "Sedang", "Penting", "Darurat"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabungan);

        // Ambil userId dari SharedPreferences
        SharedPreferences prefs = getSharedPreferences("keuangan_prefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        dbHelper = new DatabaseHelper(this);

        initViews();
        setupSpinner();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnTambahTabungan).setOnClickListener(v -> tambahTabungan());
        findViewById(R.id.btnAmbilTabungan).setOnClickListener(v -> ambilTabungan());

        refreshData();
    }

    private void initViews() {
        tvSaldoTabungan = findViewById(R.id.tvSaldoTabungan);
        tvSaldoDompet = findViewById(R.id.tvSaldoDompet);
        etNominalTambah = findViewById(R.id.etNominalTambahTabungan);
        etNominalAmbil = findViewById(R.id.etNominalAmbilTabungan);
        etAlasanAmbil = findViewById(R.id.etAlasanAmbilTabungan);
        spTingkatDarurat = findViewById(R.id.spTingkatDaruratTabungan);
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, daftarDarurat
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTingkatDarurat.setAdapter(adapter);
    }

    private void refreshData() {
        mahasiswa = dbHelper.getMahasiswa(userId);
        tabungan = dbHelper.getTabungan(userId);

        if (mahasiswa != null && tabungan != null) {
            tvSaldoTabungan.setText(FormatRupiah.format(tabungan.getSaldoTabungan()));
            tvSaldoDompet.setText("Saldo Dompet Utama: " + FormatRupiah.format(mahasiswa.getSaldo()));
        }
    }

    private void tambahTabungan() {
        String nominalStr = etNominalTambah.getText().toString().trim();

        if (ValidasiInput.isEmpty(nominalStr)) {
            Toast.makeText(this, "Nominal setoran tidak boleh kosong!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!ValidasiInput.isNumber(nominalStr)) {
            Toast.makeText(this, "Nominal setoran harus angka!", Toast.LENGTH_SHORT).show();
            return;
        }

        double nominal = Double.parseDouble(nominalStr);
        if (!ValidasiInput.isPositive(nominal)) {
            Toast.makeText(this, "Nominal setoran harus lebih dari 0!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (nominal > mahasiswa.getSaldo()) {
            Toast.makeText(this, "Saldo utama tidak mencukupi untuk ditabung!", Toast.LENGTH_SHORT).show();
            return;
        }

        String tanggal = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());

        // Penyetoran tabungan dicatat sebagai transaksi Pengeluaran dengan kategori Tabungan
        Pengeluaran pengeluaran = new Pengeluaran(0, tanggal, nominal, "Setoran Tabungan", "Tabungan", "Menabung untuk masa depan", "Penting");

        try {
            // Memproses transaksi penyetoran tabungan
            pengeluaran.prosesTransaksi(mahasiswa, tabungan);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        // Menyimpan pembaruan data ke database SQLite berdasarkan userId
        dbHelper.updateMahasiswa(mahasiswa);
        dbHelper.updateTabungan(tabungan, userId);
        dbHelper.insertTransaksi(pengeluaran, userId);

        etNominalTambah.setText("");
        Toast.makeText(this, "Berhasil menyetor ke tabungan!", Toast.LENGTH_SHORT).show();
        refreshData();
    }

    private void ambilTabungan() {
        String nominalStr = etNominalAmbil.getText().toString().trim();
        String alasan = etAlasanAmbil.getText().toString().trim();

        if (ValidasiInput.isEmpty(nominalStr) || ValidasiInput.isEmpty(alasan)) {
            Toast.makeText(this, "Nominal dan Alasan penarikan wajib diisi!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!ValidasiInput.isNumber(nominalStr)) {
            Toast.makeText(this, "Nominal penarikan harus angka!", Toast.LENGTH_SHORT).show();
            return;
        }

        double nominal = Double.parseDouble(nominalStr);
        if (!ValidasiInput.isPositive(nominal)) {
            Toast.makeText(this, "Nominal penarikan harus lebih dari 0!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (nominal > tabungan.getSaldoTabungan()) {
            Toast.makeText(this, "Nominal melebihi saldo tabungan Anda!", Toast.LENGTH_SHORT).show();
            return;
        }

        String tanggal = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());

        // Penarikan tabungan dicatat sebagai transaksi Pemasukan dengan sumber Ambil Tabungan
        Pemasukan pemasukan = new Pemasukan(0, tanggal, nominal, "Tarik Tabungan: " + alasan, "Ambil Tabungan");

        try {
            // Memproses transaksi penarikan tabungan
            pemasukan.prosesTransaksi(mahasiswa, tabungan);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        // Menyimpan pembaruan data ke database SQLite berdasarkan userId
        dbHelper.updateMahasiswa(mahasiswa);
        dbHelper.updateTabungan(tabungan, userId);
        dbHelper.insertTransaksi(pemasukan, userId);

        etNominalAmbil.setText("");
        etAlasanAmbil.setText("");
        Toast.makeText(this, "Berhasil menarik uang dari tabungan!", Toast.LENGTH_SHORT).show();
        refreshData();
    }
}
