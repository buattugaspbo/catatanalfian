package com.keuangan.mahasiswa.utils;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utilitas pembantu untuk mengubah nilai numerik double menjadi
 * representasi format mata uang Rupiah Indonesia.
 */
public class FormatRupiah {
    
    public static String format(double nominal) {
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
        // Menghilangkan nilai desimal sen ",00" untuk penyederhanaan UI
        return formatRupiah.format(nominal).replace(",00", "");
    }
}
