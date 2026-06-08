package com.keuangan.mahasiswa.model;

/**
 * Konsep PBO: Encapsulation
 * Kelas Tabungan bertindak sebagai manajer saldo tabungan mahasiswa.
 * Menyediakan method validasi untuk menambah dan mengambil uang dari tabungan.
 */
public class Tabungan {
    // Konsep PBO: Encapsulation
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

    // Metode tambah tabungan
    public void tambahTabungan(double nominal) {
        if (nominal <= 0) {
            throw new IllegalArgumentException("Nominal setoran tabungan harus lebih besar dari nol!");
        }
        this.saldoTabungan += nominal;
    }

    // Metode ambil tabungan dengan validasi batas saldo tabungan
    public void ambilTabungan(double nominal) {
        if (nominal <= 0) {
            throw new IllegalArgumentException("Nominal penarikan tabungan harus lebih besar dari nol!");
        }
        if (nominal > this.saldoTabungan) {
            throw new IllegalArgumentException("Saldo tabungan tidak mencukupi untuk ditarik!");
        }
        this.saldoTabungan -= nominal;
    }

    // Metode cek saldo tabungan
    public double cekSaldoTabungan() {
        return this.saldoTabungan;
    }
}
