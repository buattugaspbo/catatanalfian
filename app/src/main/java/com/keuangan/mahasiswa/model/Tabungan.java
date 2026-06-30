package com.keuangan.mahasiswa.model;

// Kelas Tabungan untuk menyimpan dan mengelola data saldo tabungan mahasiswa
public class Tabungan {
    private double saldoTabungan;

    public Tabungan(double saldoTabungan) {
        this.saldoTabungan = saldoTabungan;
    }

    public double getSaldoTabungan() {
        return saldoTabungan;
    }

    public void setSaldoTabungan(double saldoTabungan) {
        if (saldoTabungan < 0) {
            throw new IllegalArgumentException("Saldo tabungan tidak boleh kurang dari nol!");
        }
        this.saldoTabungan = saldoTabungan;
    }

    // Metode untuk menambah saldo tabungan
    public void tambahTabungan(double nominal) {
        if (nominal <= 0) {
            throw new IllegalArgumentException("Nominal setoran tabungan harus lebih besar dari nol!");
        }
        this.saldoTabungan += nominal;
    }

    // Metode untuk menarik uang dari tabungan dengan validasi batas saldo tabungan
    public void ambilTabungan(double nominal) {
        if (nominal <= 0) {
            throw new IllegalArgumentException("Nominal penarikan tabungan harus lebih besar dari nol!");
        }
        if (nominal > this.saldoTabungan) {
            throw new IllegalArgumentException("Saldo tabungan tidak mencukupi untuk ditarik!");
        }
        this.saldoTabungan -= nominal;
    }

    // Metode untuk mengecek jumlah saldo tabungan saat ini
    public double cekSaldoTabungan() {
        return this.saldoTabungan;
    }
}
