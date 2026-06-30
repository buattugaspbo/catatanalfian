package com.keuangan.mahasiswa.activity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.keuangan.mahasiswa.R;
import com.keuangan.mahasiswa.database.DatabaseHelper;
import com.keuangan.mahasiswa.model.LaporanKeuangan;
import com.keuangan.mahasiswa.model.Mahasiswa;
import com.keuangan.mahasiswa.model.Pemasukan;
import com.keuangan.mahasiswa.model.Pengeluaran;
import com.keuangan.mahasiswa.model.RencanaPengeluaran;
import com.keuangan.mahasiswa.model.Tabungan;
import com.keuangan.mahasiswa.model.Transaksi;
import com.keuangan.mahasiswa.utils.FormatRupiah;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Activity untuk menyajikan laporan ringkasan keuangan mahasiswa dengan grafik dan filter
public class LaporanActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private Mahasiswa mahasiswa;
    private Tabungan tabungan;
    private List<Transaksi> semuaTransaksiList;
    private List<Transaksi> filteredTransaksiList;
    private List<RencanaPengeluaran> rencanaList;
    private int userId;

    private TextView tvUangBulanan, tvTotalPengeluaran, tvTotalTabungan, tvSaldoAkhir;
    private TextView tvTingkatKebutuhanDominan, tvKesimpulan, tvLineChartPeriode;
    private LinearLayout llKategoriLaporan;

    private Spinner spTipe, spBulan, spTahun;
    private LineChart lineChart;
    private PieChart pieChart;

    private final String[] listTipeFilter = {"Semua", "Per Bulan"};
    private final String[] listBulan = {
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laporan);

        // Ambil userId dari SharedPreferences
        SharedPreferences prefs = getSharedPreferences("keuangan_prefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        dbHelper = new DatabaseHelper(this);
        mahasiswa = dbHelper.getMahasiswa(userId);
        tabungan = dbHelper.getTabungan(userId);
        semuaTransaksiList = dbHelper.getAllTransaksi(userId);
        rencanaList = dbHelper.getAllRencana(userId);

        filteredTransaksiList = new ArrayList<>(semuaTransaksiList);

        initViews();
        setupSpinners();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnTerapkanFilter).setOnClickListener(v -> terapkanFilter());

        updateTampilan();
    }

    private void initViews() {
        tvUangBulanan = findViewById(R.id.tvLaporanUangBulanan);
        tvTotalPengeluaran = findViewById(R.id.tvLaporanTotalPengeluaran);
        tvTotalTabungan = findViewById(R.id.tvLaporanTotalTabungan);
        tvSaldoAkhir = findViewById(R.id.tvLaporanSaldoAkhir);
        tvTingkatKebutuhanDominan = findViewById(R.id.tvTingkatKebutuhanDominan);
        tvKesimpulan = findViewById(R.id.tvLaporanKesimpulan);
        llKategoriLaporan = findViewById(R.id.llKategoriLaporan);
        tvLineChartPeriode = findViewById(R.id.tvLineChartPeriode);

        spTipe = findViewById(R.id.spFilterTipe);
        spBulan = findViewById(R.id.spFilterBulan);
        spTahun = findViewById(R.id.spFilterTahun);
        lineChart = findViewById(R.id.lineChart);
        pieChart = findViewById(R.id.pieChart);
    }

    private void setupSpinners() {
        // Spinner Tipe Filter
        ArrayAdapter<String> tipeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listTipeFilter);
        tipeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTipe.setAdapter(tipeAdapter);

        // Spinner Bulan
        ArrayAdapter<String> bulanAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listBulan);
        bulanAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spBulan.setAdapter(bulanAdapter);

        // Spinner Tahun (diambil dinamis dari data transaksi yang ada)
        Set<String> tahunSet = new HashSet<>();
        for (Transaksi t : semuaTransaksiList) {
            int thn = getYearFromDateString(t.getTanggal());
            if (thn != -1) {
                tahunSet.add(String.valueOf(thn));
            }
        }
        // Tambahkan tahun saat ini sebagai default jika list kosong
        if (tahunSet.isEmpty()) {
            tahunSet.add(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
        }
        List<String> listTahun = new ArrayList<>(tahunSet);
        Collections.sort(listTahun, Collections.reverseOrder());

        ArrayAdapter<String> tahunAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listTahun);
        tahunAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTahun.setAdapter(tahunAdapter);

        // Atur interaktivitas
        spTipe.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) { // Semua
                    spBulan.setEnabled(false);
                } else { // Per Bulan
                    spBulan.setEnabled(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Set default bulan saat ini
        int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
        spBulan.setSelection(currentMonth);
    }

    private void terapkanFilter() {
        String tipe = spTipe.getSelectedItem().toString();
        String tahunStr = spTahun.getSelectedItem() != null ? spTahun.getSelectedItem().toString() : "";
        int targetTahun = tahunStr.isEmpty() ? -1 : Integer.parseInt(tahunStr);

        filteredTransaksiList.clear();

        if ("Semua".equals(tipe)) {
            for (Transaksi t : semuaTransaksiList) {
                int thn = getYearFromDateString(t.getTanggal());
                if (thn == targetTahun || targetTahun == -1) {
                    filteredTransaksiList.add(t);
                }
            }
            tvLineChartPeriode.setText("Tren Keuangan Tahun " + tahunStr);
        } else {
            int targetBulan = spBulan.getSelectedItemPosition() + 1; // 1-indexed
            for (Transaksi t : semuaTransaksiList) {
                int bln = getMonthFromDateString(t.getTanggal());
                int thn = getYearFromDateString(t.getTanggal());
                if (bln == targetBulan && thn == targetTahun) {
                    filteredTransaksiList.add(t);
                }
            }
            tvLineChartPeriode.setText("Tren Keuangan Bulan " + listBulan[targetBulan - 1] + " " + tahunStr);
        }

        updateTampilan();
        Toast.makeText(this, "Filter berhasil diterapkan!", Toast.LENGTH_SHORT).show();
    }

    private void updateTampilan() {
        tampilkanLaporanRingkas();
        tampilkanPengeluaranPerKategori();
        hitungTingkatKebutuhanDominan();
        tampilkanKesimpulanFinansial();
        buatLineChart();
        buatPieChart();
    }

    private void tampilkanLaporanRingkas() {
        LaporanKeuangan laporan = new LaporanKeuangan();
        double totalPengeluaran = laporan.hitungTotalPengeluaran(filteredTransaksiList);
        double totalPemasukan = laporan.hitungTotalPemasukan(filteredTransaksiList);

        if (mahasiswa != null && tabungan != null) {
            tvUangBulanan.setText(FormatRupiah.format(mahasiswa.getUangBulanan()));
            tvTotalPengeluaran.setText(FormatRupiah.format(totalPengeluaran));
            tvTotalTabungan.setText(FormatRupiah.format(tabungan.getSaldoTabungan()));
            tvSaldoAkhir.setText(FormatRupiah.format(mahasiswa.getSaldo()));
        }
    }

    private void tampilkanPengeluaranPerKategori() {
        llKategoriLaporan.removeAllViews();

        Map<String, Double> kategoriSpend = new HashMap<>();
        for (Transaksi t : filteredTransaksiList) {
            if (t instanceof Pengeluaran) {
                Pengeluaran p = (Pengeluaran) t;
                String kat = p.getKategori();
                kategoriSpend.put(kat, kategoriSpend.getOrDefault(kat, 0.0) + p.getNominal());
            }
        }

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
            
            // Format nominal menggunakan titik
            String rowText = "• " + entry.getKey() + ": " + FormatRupiah.format(entry.getValue());
            tvRow.setText(rowText);
            
            llKategoriLaporan.addView(tvRow);
        }
    }

    private void hitungTingkatKebutuhanDominan() {
        Map<String, Integer> counts = new HashMap<>();
        for (Transaksi t : filteredTransaksiList) {
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
        String kesimpulanText = laporan.buatKesimpulan(rencanaList, filteredTransaksiList);
        tvKesimpulan.setText(kesimpulanText);
    }

    // =========================================================================
    // PEMBUATAN GRAFIK (LINE CHART & PIE CHART DONAT)
    // =========================================================================

    private void buatLineChart() {
        List<Entry> entriesPemasukan = new ArrayList<>();
        List<Entry> entriesPengeluaran = new ArrayList<>();
        final List<String> labels = new ArrayList<>();

        String tipe = spTipe.getSelectedItem().toString();

        if ("Per Bulan".equals(tipe)) {
            // X-axis: Hari dalam sebulan (1 s/d 31)
            double[] harianPemasukan = new double[32];
            double[] harianPengeluaran = new double[32];

            for (Transaksi t : filteredTransaksiList) {
                int day = getDayFromDateString(t.getTanggal());
                if (day >= 1 && day <= 31) {
                    if (t instanceof Pemasukan) {
                        harianPemasukan[day] += t.getNominal();
                    } else {
                        harianPengeluaran[day] += t.getNominal();
                    }
                }
            }

            for (int i = 1; i <= 31; i++) {
                entriesPemasukan.add(new Entry(i - 1, (float) harianPemasukan[i]));
                entriesPengeluaran.add(new Entry(i - 1, (float) harianPengeluaran[i]));
                labels.add(String.valueOf(i));
            }
        } else {
            // X-axis: 12 Bulan (Jan s/d Des)
            double[] bulananPemasukan = new double[13];
            double[] bulananPengeluaran = new double[13];

            for (Transaksi t : filteredTransaksiList) {
                int month = getMonthFromDateString(t.getTanggal());
                if (month >= 1 && month <= 12) {
                    if (t instanceof Pemasukan) {
                        bulananPemasukan[month] += t.getNominal();
                    } else {
                        bulananPengeluaran[month] += t.getNominal();
                    }
                }
            }

            String[] namaBulanSingkat = {"Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Ags", "Sep", "Okt", "Nov", "Des"};
            for (int i = 1; i <= 12; i++) {
                entriesPemasukan.add(new Entry(i - 1, (float) bulananPemasukan[i]));
                entriesPengeluaran.add(new Entry(i - 1, (float) bulananPengeluaran[i]));
                labels.add(namaBulanSingkat[i - 1]);
            }
        }

        // Setup Data Set Line Pemasukan (Hijau)
        LineDataSet setPem = new LineDataSet(entriesPemasukan, "Pemasukan");
        setPem.setColor(Color.rgb(76, 175, 80));
        setPem.setCircleColor(Color.rgb(76, 175, 80));
        setPem.setLineWidth(2.5f);
        setPem.setCircleRadius(3.5f);
        setPem.setDrawCircleHole(false);
        setPem.setDrawValues(false);

        // Setup Data Set Line Pengeluaran (Merah)
        LineDataSet setPeng = new LineDataSet(entriesPengeluaran, "Pengeluaran");
        setPeng.setColor(Color.rgb(244, 67, 54));
        setPeng.setCircleColor(Color.rgb(244, 67, 54));
        setPeng.setLineWidth(2.5f);
        setPeng.setCircleRadius(3.5f);
        setPeng.setDrawCircleHole(false);
        setPeng.setDrawValues(false);

        LineData lineData = new LineData(setPem, setPeng);
        lineChart.setData(lineData);

        // Styling LineChart
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setTextColor(Color.DKGRAY);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(Color.DKGRAY);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelCount(Math.min(labels.size(), 10));

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setTextColor(Color.DKGRAY);
        lineChart.getAxisRight().setEnabled(false);

        lineChart.animateX(800);
        lineChart.invalidate();
    }

    private void buatPieChart() {
        List<PieEntry> entries = new ArrayList<>();
        Map<String, Double> kategoriSpend = new HashMap<>();

        for (Transaksi t : filteredTransaksiList) {
            if (t instanceof Pengeluaran) {
                Pengeluaran p = (Pengeluaran) t;
                String kat = p.getKategori();
                kategoriSpend.put(kat, kategoriSpend.getOrDefault(kat, 0.0) + p.getNominal());
            }
        }

        for (Map.Entry<String, Double> entry : kategoriSpend.entrySet()) {
            if (entry.getValue() > 0) {
                entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
            }
        }

        if (entries.isEmpty()) {
            pieChart.clear();
            pieChart.setNoDataText("Belum ada data pengeluaran untuk ditampilkan di grafik donat.");
            pieChart.invalidate();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        
        // Custom color palette
        int[] palette = {
                Color.rgb(33, 150, 243),  // Blue
                Color.rgb(233, 30, 99),   // Pink
                Color.rgb(255, 152, 0),   // Orange
                Color.rgb(156, 39, 176),  // Purple
                Color.rgb(0, 150, 136),   // Teal
                Color.rgb(255, 235, 59),  // Yellow
                Color.rgb(139, 195, 74)   // Light Green
        };
        dataSet.setColors(palette);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);

        // Styling PieChart (Donut)
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(50f);
        pieChart.setTransparentCircleRadius(55f);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setCenterText("Pengeluaran");
        pieChart.setCenterTextSize(14f);
        pieChart.getLegend().setEnabled(true);
        pieChart.getLegend().setTextColor(Color.DKGRAY);
        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.setEntryLabelTextSize(10f);

        pieChart.animateY(800);
        pieChart.invalidate();
    }

    // =========================================================================
    // DATE HELPER METHODS
    // =========================================================================

    private int getMonthFromDateString(String dateStr) {
        if (dateStr == null) return -1;
        String lower = dateStr.toLowerCase();
        if (lower.contains("jan")) return 1;
        if (lower.contains("feb")) return 2;
        if (lower.contains("mar")) return 3;
        if (lower.contains("apr")) return 4;
        if (lower.contains("mei") || lower.contains("may")) return 5;
        if (lower.contains("jun")) return 6;
        if (lower.contains("jul")) return 7;
        if (lower.contains("ags") || lower.contains("aug")) return 8;
        if (lower.contains("sep")) return 9;
        if (lower.contains("okt") || lower.contains("oct")) return 10;
        if (lower.contains("nov")) return 11;
        if (lower.contains("des") || lower.contains("dec")) return 12;
        return -1;
    }

    private int getYearFromDateString(String dateStr) {
        if (dateStr == null) return -1;
        String[] parts = dateStr.split(" ");
        if (parts.length >= 3) {
            try {
                return Integer.parseInt(parts[2].replaceAll("[^0-9]", ""));
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }

    private int getDayFromDateString(String dateStr) {
        if (dateStr == null) return -1;
        String[] parts = dateStr.split(" ");
        if (parts.length >= 1) {
            try {
                return Integer.parseInt(parts[0].replaceAll("[^0-9]", ""));
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }
}
