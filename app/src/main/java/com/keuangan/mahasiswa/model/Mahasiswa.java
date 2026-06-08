package com.keuangan.mahasiswa.model;

/**
 * Konsep PBO: Inheritance - Subclass
 * Kelas Mahasiswa mewarisi (extends) dari kelas User.
 * Kelas ini menambahkan properti spesifik mahasiswa seperti NIM, uangBulanan, dan saldo berjalan.
 */
public class Mahasiswa extends User {
    // Konsep PBO: Encapsulation
    private String nim;
    private double uangBulanan;
    private double saldo;

    public Mahasiswa(int id, String nama, String email, String nim, double uangBulanan, double saldo) {
        super(id, nama, email); // Memanggil konstruktor superclass User
        this.nim = nim;
        this.uangBulanan = uangBulanan;
        this.saldo = saldo;
    }

    public String getNim() {
        return nim;
    }

    public void setNim(String nim) {
        this.nim = nim;
    }

    public double getUangBulanan() {
        return uangBulanan;
    }

    public void setUangBulanan(double uangBulanan) {
        if (uangBulanan < 0) {
            throw new IllegalArgumentException("Uang bulanan tidak boleh negatif!");
        }
        this.uangBulanan = uangBulanan;
    }

    public double getSaldo() {
        return saldo;
    }

    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }

    // Metode khusus menghitung saldo (misal jika ada penyesuaian khusus di masa depan)
    public double hitungSaldo() {
        return this.saldo;
    }

    // Konsep PBO: Polymorphism - Overriding Method
    @Override
    public String tampilkanInfo() {
        return "Mahasiswa: " + getNama() + " | NIM: " + nim + " | Email: " + getEmail();
    }
}
