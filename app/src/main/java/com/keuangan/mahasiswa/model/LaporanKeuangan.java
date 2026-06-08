package com.keuangan.mahasiswa.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Konsep PBO: Polymorphism (Penggunaan instanceof & Dynamic Casting)
 * Kelas LaporanKeuangan bertugas memproses daftar transaksi umum (superclass Transaksi) 
 * secara dinamis untuk menghasilkan kalkulasi pemasukan, pengeluaran, dan kesimpulan finansial.
 */
public class LaporanKeuangan {

    // Menghitung total pengeluaran dari list transaksi polimorfik
    public double hitungTotalPengeluaran(List<Transaksi> list) {
        double total = 0;
        for (Transaksi t : list) {
            // Mengecek apakah objek riil bertipe Pengeluaran (Polymorphism)
            if (t instanceof Pengeluaran) {
                total += t.getNominal();
            }
        }
        return total;
    }

    // Menghitung total pemasukan dari list transaksi polimorfik
    public double hitungTotalPemasukan(List<Transaksi> list) {
        double total = 0;
        for (Transaksi t : list) {
            // Mengecek apakah objek riil bertipe Pemasukan (Polymorphism)
            if (t instanceof Pemasukan) {
                total += t.getNominal();
            }
        }
        return total;
    }

    public double hitungTotalTabungan(double saldoTabungan) {
        return saldoTabungan;
    }

    // Membuat kesimpulan finansial dengan menganalisis rencana budget vs riwayat pengeluaran
    public String buatKesimpulan(List<RencanaPengeluaran> rencanaList, List<Transaksi> list) {
        StringBuilder kesimpulan = new StringBuilder();
        
        // Petakan akumulasi pengeluaran per kategori
        Map<String, Double> pengeluaranPerKategori = new HashMap<>();
        int ambilTabunganCount = 0;

        for (Transaksi t : list) {
            if (t instanceof Pengeluaran) {
                Pengeluaran p = (Pengeluaran) t;
                String kat = p.getKategori();
                double nominal = p.getNominal();
                
                // Jangan masukkan tabungan sebagai pengeluaran konsumtif harian
                if (!"Tabungan".equalsIgnoreCase(kat)) {
                    if (pengeluaranPerKategori.containsKey(kat)) {
                        pengeluaranPerKategori.put(kat, pengeluaranPerKategori.get(kat) + nominal);
                    } else {
                        pengeluaranPerKategori.put(kat, nominal);
                    }
                }
            } else if (t instanceof Pemasukan) {
                Pemasukan pem = (Pemasukan) t;
                // Hitung seberapa sering mengambil tabungan
                if ("Ambil Tabungan".equalsIgnoreCase(pem.getSumberPemasukan())) {
                    ambilTabunganCount++;
                }
            }
        }

        // Bandingkan pengeluaran riil dengan rencana alokasi anggaran
        boolean overBudget = false;
        for (RencanaPengeluaran rp : rencanaList) {
            String kat = rp.getKategori();
            double nominalRencana = rp.getNominalRencana();
            double terpakai = pengeluaranPerKategori.containsKey(kat) ? pengeluaranPerKategori.get(kat) : 0;

            if (rp.cekMelebihiBudget(terpakai)) {
                overBudget = true;
                kesimpulan.append("• Pengeluaran Kategori \"").append(kat)
                        .append("\" sudah melebihi rencana. (Rencana: Rp ")
                        .append(String.format("%,.0f", nominalRencana))
                        .append(", Terpakai: Rp ")
                        .append(String.format("%,.0f", terpakai))
                        .append("). Kurangi pengeluaran pada kategori tersebut.\n");
            }
        }

        // Deteksi jika tabungan terlalu sering ditarik
        if (ambilTabunganCount >= 3) {
            kesimpulan.append("• Tabungan terlalu sering diambil (ditarik ").append(ambilTabunganCount)
                    .append(" kali). Saldo tabungan perlu dijaga.\n");
        }

        // Teks penutup kesimpulan
        if (kesimpulan.length() == 0) {
            kesimpulan.append("Pengeluaran masih aman karena belum melebihi rencana.");
        }

        return kesimpulan.toString();
    }
}
