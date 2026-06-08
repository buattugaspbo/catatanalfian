package com.keuangan.mahasiswa.utils;

/**
 * Utilitas pembantu untuk validasi isian input formulir.
 * Mencegah error tipe data format angka dan isian kosong.
 */
public class ValidasiInput {

    // Mengecek apakah string kosong
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    // Mengecek apakah string berupa angka valid
    public static boolean isNumber(String str) {
        if (isEmpty(str)) return false;
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Mengecek apakah nilai double lebih dari nol
    public static boolean isPositive(double val) {
        return val > 0;
    }
}
