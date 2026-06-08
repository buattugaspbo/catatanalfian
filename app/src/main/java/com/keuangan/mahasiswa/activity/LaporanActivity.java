package com.keuangan.mahasiswa.activity;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.keuangan.mahasiswa.R;
import com.keuangan.mahasiswa.database.DatabaseHelper;
import com.keuangan.mahasiswa.model.LaporanKeuangan;
import com.keuangan.mahasiswa.model.Mahasiswa;
import com.keuangan.mahasiswa.model.Pengeluaran;
import com.keuangan.mahasiswa.model.RencanaPengeluaran;
import com.keuangan.mahasiswa.model.Tabungan;
import com.keuangan.mahasiswa.model.Transaksi;
import com.keuangan.mahasiswa.utils.FormatRupiah;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Logika halaman LaporanActivity.
 * Menghitung rincian pengeluaran per kategori secara dinamis,
 * menentukan tingkat kedaruratan dominan, dan menyajikan kesimpulan keuangan otomatis.
 */
public class LaporanActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private Mahasiswa mahasiswa;
    private Tabungan tabungan;
    private List<Transaksi> transaksiList;
    private List<RencanaPengeluaran> rencanaList;

    private TextView tvUangBulanan, tvTotalPengeluaran, tvTotalTabungan, tvSaldoAkhir;
    private TextView tvTingkatKebutuhanDominan, tvKesimpulan;
    private LinearLayout llKategoriLaporan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laporan);

        dbHelper = new DatabaseHelper(this);
        mahasiswa = dbHelper.getMahasiswa();
        tabungan = dbHelper.getTabungan();
        transaksiList = dbHelper.getAllTransaksi();
        rencanaList = dbHelper.getAllRencana();

        initViews();
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        tampilkanLaporanRingkas();
        tampilkanPengeluaranPerKategori();
        hitungTingkatKebutuhanDominan();
        tampilkanKesimpulanFinansial();
    }

    private void initViews() {
        tvUangBulanan = findViewById(R.id.tvLaporanUangBulanan);
        tvTotalPengeluaran = findViewById(R.id.tvLaporanTotalPengeluaran);
        tvTotalTabungan = findViewById(R.id.tvLaporanTotalTabungan);
        tvSaldoAkhir = findViewById(R.id.tvLaporanSaldoAkhir);
        tvTingkatKebutuhanDominan = findViewById(R.id.tvTingkatKebutuhanDominan);
        tvKesimpulan = findViewById(R.id.tvLaporanKesimpulan);
        llKategoriLaporan = findViewById(R.id.llKategoriLaporan);
    }

    private void tampilkanLaporanRingkas() {
        LaporanKeuangan laporan = new LaporanKeuangan();
        double totalPengeluaran = laporan.hitungTotalPengeluaran(transaksiList);
        double totalPemasukan = laporan.hitungTotalPemasukan(transaksiList);

        tvUangBulanan.setText(FormatRupiah.format(mahasiswa.getUangBulanan()));
        tvTotalPengeluaran.setText(FormatRupiah.format(totalPengeluaran));
        tvTotalTabungan.setText(FormatRupiah.format(laporan.hitungTotalTabungan(tabungan.getSaldoTabungan())));
        tvSaldoAkhir.setText(FormatRupiah.format(mahasiswa.getSaldo()));
    }

    private void tampilkanPengeluaranPerKategori() {
        llKategoriLaporan.removeAllViews();

        // 1. Akumulasikan pengeluaran per kategori
        Map<String, Double> kategoriSpend = new HashMap<>();
        for (Transaksi t : transaksiList) {
            if (t instanceof Pengeluaran) {
                Pengeluaran p = (Pengeluaran) t;
                String kat = p.getKategori();
                kategoriSpend.put(kat, kategoriSpend.getOrDefault(kat, 0.0) + p.getNominal());
            }
        }

        // 2. Tampilkan pengeluaran per kategori secara dinamis di UI
        if (kategoriSpend.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("Belum ada data pengeluaran.");
            emptyText.setTextSize(13);
            emptyText.setTextColor(getResources().getColor(R.color.text_secondary));
            llKategoriLaporan.addView(emptyText);
            return;
        }

        for (Map.Entry<String, Double> entry : kategoriSpend.entrySet()) {
            TextView tvRow = new TextView(this);
            tvRow.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            tvRow.setPadding(0, 8, 0, 8);
            tvRow.setTextSize(13);
            tvRow.setTextColor(getResources().getColor(R.color.text_primary));
            
            // Format text: Makan: Rp 150.000
            String rowText = "• " + entry.getKey() + ": " + FormatRupiah.format(entry.getValue());
            tvRow.setText(rowText);
            
            llKategoriLaporan.addView(tvRow);
        }
    }

    private void hitungTingkatKebutuhanDominan() {
        Map<String, Integer> counts = new HashMap<>();
        for (Transaksi t : transaksiList) {
            if (t instanceof Pengeluaran) {
                Pengeluaran p = (Pengeluaran) t;
                String tingkat = p.getTingkatKebutuhan();
                if (tingkat != null && !tingkat.isEmpty()) {
                    counts.put(tingkat, counts.getOrDefault(tingkat, 0) + 1);
                }
            }
        }

        String dominan = "-";
        int max = 0;
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                dominan = entry.getKey();
            }
        }

        if (max > 0) {
            tvTingkatKebutuhanDominan.setText("Tingkat Kebutuhan Terbanyak: " + dominan + " (digunakan sebanyak " + max + " kali)");
        } else {
            tvTingkatKebutuhanDominan.setText("Tingkat Kebutuhan Terbanyak: Belum ada transaksi pengeluaran.");
        }
    }

    private void tampilkanKesimpulanFinansial() {
        LaporanKeuangan laporan = new LaporanKeuangan();
        String kesimpulanText = laporan.buatKesimpulan(rencanaList, transaksiList);
        tvKesimpulan.setText(kesimpulanText);
    }
}
