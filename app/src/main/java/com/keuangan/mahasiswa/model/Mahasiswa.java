package com.keuangan.mahasiswa.model;

// Kelas Mahasiswa merupakan kelas turunan (subclass) dari kelas User
public class Mahasiswa extends User {
    private String nim;
    private double uangBulanan;
    private double saldo;

    public Mahasiswa(int id, String nama, String email, String nim, double uangBulanan, double saldo) {
        super(id, nama, email); // Memanggil konstruktor dari superclass User
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

    // Metode untuk menghitung saldo mahasiswa
    public double hitungSaldo() {
        return this.saldo;
    }

    // Overriding metode tampilkanInfo dari superclass User
    @Override
    public String tampilkanInfo() {
        return "Mahasiswa: " + getNama() + " | NIM: " + nim + " | Email: " + getEmail();
    }
}
