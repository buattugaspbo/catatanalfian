package com.keuangan.mahasiswa.utils;

import java.text.NumberFormat;
import java.util.Locale;

// Kelas utilitas untuk mengubah nilai numerik double menjadi format mata uang Rupiah
public class FormatRupiah {

    // Format dengan simbol Rp dan titik ribuan: Rp1.500.000
    public static String format(double nominal) {
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
        formatRupiah.setMaximumFractionDigits(0);
        formatRupiah.setMinimumFractionDigits(0);
        return formatRupiah.format(nominal);
    }

    // Format angka dengan titik ribuan saja, tanpa simbol Rp: 1.500.000
    public static String formatAngka(double nominal) {
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatAngka = NumberFormat.getNumberInstance(localeID);
        formatAngka.setMaximumFractionDigits(0);
        formatAngka.setMinimumFractionDigits(0);
        return formatAngka.format(nominal);
    }
}
