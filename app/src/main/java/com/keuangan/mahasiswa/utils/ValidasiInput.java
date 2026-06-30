package com.keuangan.mahasiswa.utils;

// Kelas utilitas untuk membantu validasi input pada form aplikasi
public class ValidasiInput {

    // Memeriksa apakah input teks kosong atau hanya berisi spasi
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    // Memeriksa apakah input teks dapat diubah menjadi tipe data angka
    public static boolean isNumber(String str) {
        if (isEmpty(str)) return false;
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Memeriksa apakah nilai angka lebih besar dari nol
    public static boolean isPositive(double val) {
        return val > 0;
    }
}
