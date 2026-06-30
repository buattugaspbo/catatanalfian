package com.keuangan.mahasiswa.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.keuangan.mahasiswa.R;
import com.keuangan.mahasiswa.adapter.RencanaAdapter;
import com.keuangan.mahasiswa.database.DatabaseHelper;
import com.keuangan.mahasiswa.model.Mahasiswa;
import com.keuangan.mahasiswa.model.RencanaPengeluaran;
import com.keuangan.mahasiswa.utils.FormatRupiah;
import com.keuangan.mahasiswa.utils.ValidasiInput;

import java.util.ArrayList;
import java.util.List;

// Activity untuk mengelola rencana pengeluaran bulanan per kategori
public class RencanaPengeluaranActivity extends AppCompatActivity implements RencanaAdapter.OnItemLongClickListener {

    private DatabaseHelper dbHelper;
    private Mahasiswa mahasiswa;
    private List<RencanaPengeluaran> rencanaList;
    private RencanaAdapter adapter;
    private int userId;

    private Spinner spKategori;
    private EditText etNominal;
    private TextView tvUangBulananSummary, tvTotalRencanaSummary, tvBudgetWarning;
    private RecyclerView rvRencana;

    private final String[] daftarKategori = {
            "Kos", "Makan", "Transportasi", "Internet", "Tugas Kuliah", "Hiburan", "Tabungan"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rencana_pengeluaran);

        // Ambil userId dari SharedPreferences
        SharedPreferences prefs = getSharedPreferences("keuangan_prefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        dbHelper = new DatabaseHelper(this);
        mahasiswa = dbHelper.getMahasiswa(userId);

        initViews();
        setupSpinner();
        setupRecyclerView();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnTambahRencana).setOnClickListener(v -> tambahRencana());

        loadRencanaData();
    }

    private void initViews() {
        spKategori = findViewById(R.id.spKategoriRencana);
        etNominal = findViewById(R.id.etNominalRencana);
        tvUangBulananSummary = findViewById(R.id.tvUangBulananSummary);
        tvTotalRencanaSummary = findViewById(R.id.tvTotalRencanaSummary);
        tvBudgetWarning = findViewById(R.id.tvBudgetWarning);
        rvRencana = findViewById(R.id.rvRencana);
    }

    private void setupSpinner() {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, daftarKategori
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spKategori.setAdapter(spinnerAdapter);
    }

    private void setupRecyclerView() {
        rencanaList = new ArrayList<>();
        adapter = new RencanaAdapter(rencanaList, this);
        rvRencana.setLayoutManager(new LinearLayoutManager(this));
        rvRencana.setAdapter(adapter);
    }

    private void loadRencanaData() {
        if (mahasiswa == null) return;
        
        rencanaList.clear();
        rencanaList.addAll(dbHelper.getAllRencana(userId));
        adapter.notifyDataSetChanged();

        // Menghitung total seluruh rencana anggaran
        double totalRencana = 0;
        for (RencanaPengeluaran r : rencanaList) {
            totalRencana += r.getNominalRencana();
        }

        // Memperbarui informasi di UI
        tvUangBulananSummary.setText("Uang Bulanan: " + FormatRupiah.format(mahasiswa.getUangBulanan()));
        tvTotalRencanaSummary.setText("Total Rencana: " + FormatRupiah.format(totalRencana));

        // Menampilkan peringatan jika rencana melebihi uang bulanan
        if (totalRencana > mahasiswa.getUangBulanan()) {
            tvBudgetWarning.setText("Peringatan: Total rencana anggaran (" + FormatRupiah.format(totalRencana) + ") melebihi total uang bulanan Anda!");
            tvBudgetWarning.setVisibility(View.VISIBLE);
        } else {
            tvBudgetWarning.setVisibility(View.GONE);
        }
    }

    private void tambahRencana() {
        String kategori = spKategori.getSelectedItem().toString();
        String nominalStr = etNominal.getText().toString().trim();

        // Validasi input form
        if (ValidasiInput.isEmpty(nominalStr)) {
            Toast.makeText(this, "Nominal rencana tidak boleh kosong!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!ValidasiInput.isNumber(nominalStr)) {
            Toast.makeText(this, "Nominal rencana harus angka!", Toast.LENGTH_SHORT).show();
            return;
        }

        double nominal = Double.parseDouble(nominalStr);
        if (!ValidasiInput.isPositive(nominal)) {
            Toast.makeText(this, "Nominal harus lebih dari 0!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Inisialisasi objek RencanaPengeluaran
        RencanaPengeluaran rencana = new RencanaPengeluaran(kategori, nominal);

        // Menyimpan data rencana anggaran ke database SQLite berdasarkan userId
        dbHelper.addOrUpdateRencana(rencana, userId);
        etNominal.setText("");

        Toast.makeText(this, "Rencana anggaran berhasil disimpan", Toast.LENGTH_SHORT).show();
        loadRencanaData();
    }

    // Callback long click untuk menghapus rencana anggaran
    @Override
    public void onItemLongClick(RencanaPengeluaran rp) {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Rencana Anggaran")
                .setMessage("Apakah Anda yakin ingin menghapus rencana anggaran kategori \"" + rp.getKategori() + "\"?")
                .setPositiveButton("Hapus", (dialog, which) -> deleteRencana(rp))
                .setNegativeButton("Batal", null)
                .show();
    }

    private void deleteRencana(RencanaPengeluaran rp) {
        boolean success = dbHelper.deleteRencana(rp.getKategori(), userId);
        if (success) {
            Toast.makeText(this, "Rencana anggaran berhasil dihapus!", Toast.LENGTH_SHORT).show();
            loadRencanaData();
        } else {
            Toast.makeText(this, "Gagal menghapus rencana anggaran!", Toast.LENGTH_SHORT).show();
        }
    }
}
