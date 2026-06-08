package com.keuangan.mahasiswa.model;

/**
 * Konsep PBO: Abstraction (Abstraksi)
 * Kelas Transaksi dideklarasikan sebagai abstract class.
 * Kelas ini memiliki abstract method prosesTransaksi() yang akan di-override
 * oleh subclass Pemasukan dan Pengeluaran sesuai logika bisnis masing-masing.
 */
public abstract class Transaksi {
    // Konsep PBO: Encapsulation
    private int id;
    private String tanggal;
    private double nominal;
    private String keterangan;

    public Transaksi(int id, String tanggal, double nominal, String keterangan) {
        this.id = id;
        this.tanggal = tanggal;
        setNominal(nominal); // Validasi input melalui setter enkapsulasi
        this.keterangan = keterangan;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTanggal() {
        return tanggal;
    }

    public void setTanggal(String tanggal) {
        this.tanggal = tanggal;
    }

    public double getNominal() {
        return nominal;
    }

    public void setNominal(double nominal) {
        // Validasi nominal tidak boleh negatif atau nol
        if (nominal <= 0) {
            throw new IllegalArgumentException("Nominal harus berupa angka lebih dari 0!");
        }
        this.nominal = nominal;
    }

    public String getKeterangan() {
        return keterangan;
    }

    public void setKeterangan(String keterangan) {
        this.keterangan = keterangan;
    }

    // Konsep PBO: Abstraction - Method Abstrak
    // Logika pemrosesan transaksi yang di-override secara spesifik di subclass
    public abstract void prosesTransaksi(Mahasiswa m, Tabungan t);
    
    // Method Pendukung Polimorfisme Tampilan
    public abstract String getType();
    public abstract String getDetailInfo();

    // Menampilkan detail teks transaksi
    public String tampilkanDetail() {
        return "[" + tanggal + "] " + getType() + ": " + keterangan + " - Nominal: " + nominal;
    }
}
