package com.keuangan.mahasiswa.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Kelas LaporanKeuangan untuk memproses daftar transaksi dan menghasilkan ringkasan finansial
public class LaporanKeuangan {

    // Menghitung akumulasi total pengeluaran dari daftar transaksi
    public double hitungTotalPengeluaran(List<Transaksi> list) {
        double total = 0;
        for (Transaksi t : list) {
            if (t instanceof Pengeluaran) {
                total += t.getNominal();
            }
        }
        return total;
    }

    // Menghitung akumulasi total pemasukan dari daftar transaksi
    public double hitungTotalPemasukan(List<Transaksi> list) {
        double total = 0;
        for (Transaksi t : list) {
            if (t instanceof Pemasukan) {
                total += t.getNominal();
            }
        }
        return total;
    }

    public double hitungTotalTabungan(double saldoTabungan) {
        return saldoTabungan;
    }

    // Menganalisis perbandingan anggaran rencana dengan riwayat transaksi pengeluaran
    public String buatKesimpulan(List<RencanaPengeluaran> rencanaList, List<Transaksi> list) {
        StringBuilder kesimpulan = new StringBuilder();
        
        Map<String, Double> pengeluaranPerKategori = new HashMap<>();
        int ambilTabunganCount = 0;

        for (Transaksi t : list) {
            if (t instanceof Pengeluaran) {
                Pengeluaran p = (Pengeluaran) t;
                String kat = p.getKategori();
                double nominal = p.getNominal();
                
                // Tidak menghitung tabungan sebagai pengeluaran konsumsi harian
                if (!"Tabungan".equalsIgnoreCase(kat)) {
                    if (pengeluaranPerKategori.containsKey(kat)) {
                        pengeluaranPerKategori.put(kat, pengeluaranPerKategori.get(kat) + nominal);
                    } else {
                        pengeluaranPerKategori.put(kat, nominal);
                    }
                }
            } else if (t instanceof Pemasukan) {
                Pemasukan pem = (Pemasukan) t;
                if ("Ambil Tabungan".equalsIgnoreCase(pem.getSumberPemasukan())) {
                    ambilTabunganCount++;
                }
            }
        }

        // Membandingkan pengeluaran aktual dengan rencana pengeluaran
        for (RencanaPengeluaran rp : rencanaList) {
            String kat = rp.getKategori();
            double nominalRencana = rp.getNominalRencana();
            double terpakai = pengeluaranPerKategori.containsKey(kat) ? pengeluaranPerKategori.get(kat) : 0;

            if (rp.cekMelebihiBudget(terpakai)) {
                kesimpulan.append("• Pengeluaran Kategori \"").append(kat)
                        .append("\" sudah melebihi rencana. (Rencana: Rp ")
                        .append(String.format("%,.0f", nominalRencana))
                        .append(", Terpakai: Rp ")
                        .append(String.format("%,.0f", terpakai))
                        .append("). Kurangi pengeluaran pada kategori tersebut.\n");
            }
        }

        // Mendeteksi jika frekuensi penarikan tabungan terlalu tinggi
        if (ambilTabunganCount >= 3) {
            kesimpulan.append("• Tabungan terlalu sering diambil (ditarik ").append(ambilTabunganCount)
                    .append(" kali). Saldo tabungan perlu dijaga.\n");
        }

        if (kesimpulan.length() == 0) {
            kesimpulan.append("Pengeluaran masih aman karena belum melebihi rencana.");
        }

        return kesimpulan.toString();
    }
}
