package com.keuangan.mahasiswa.model;

// Kelas Kategori untuk mendefinisikan batas anggaran maksimal per kategori pengeluaran
public class Kategori {
    private int id;
    private String namaKategori;
    private double batasAnggaran;

    public Kategori(int id, String namaKategori, double batasAnggaran) {
        this.id = id;
        this.namaKategori = namaKategori;
        this.batasAnggaran = batasAnggaran;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNamaKategori() {
        return namaKategori;
    }

    public void setNamaKategori(String namaKategori) {
        this.namaKategori = namaKategori;
    }

    public double getBatasAnggaran() {
        return batasAnggaran;
    }

    public void setBatasAnggaran(double batasAnggaran) {
        this.batasAnggaran = batasAnggaran;
    }
}
