package com.keuangan.mahasiswa.model;

// Kelas abstrak Transaksi sebagai induk dari transaksi pemasukan dan pengeluaran
public abstract class Transaksi {
    private int id;
    private String tanggal;
    private double nominal;
    private String keterangan;

    public Transaksi(int id, String tanggal, double nominal, String keterangan) {
        this.id = id;
        this.tanggal = tanggal;
        setNominal(nominal); // Validasi input menggunakan setter
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
        // Validasi agar nominal tidak bernilai negatif atau nol
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

    // Metode abstrak untuk memproses transaksi yang akan diimplementasikan oleh subclass
    public abstract void prosesTransaksi(Mahasiswa m, Tabungan t);
    
    public abstract String getType();
    public abstract String getDetailInfo();

    // Metode untuk menampilkan detail data transaksi
    public String tampilkanDetail() {
        return "[" + tanggal + "] " + getType() + ": " + keterangan + " - Nominal: " + nominal;
    }
}
