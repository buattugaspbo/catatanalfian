package com.keuangan.mahasiswa.model;

// Kelas RencanaPengeluaran untuk memetakan alokasi rencana anggaran bulanan per kategori belanja
public class RencanaPengeluaran {
    private String kategori;
    private double nominalRencana;

    public RencanaPengeluaran(String kategori, double nominalRencana) {
        this.kategori = kategori;
        setNominalRencana(nominalRencana);
    }

    public String getKategori() {
        return kategori;
    }

    public void setKategori(String kategori) {
        this.kategori = kategori;
    }

    public double getNominalRencana() {
        return nominalRencana;
    }

    public void setNominalRencana(double nominalRencana) {
        if (nominalRencana < 0) {
            throw new IllegalArgumentException("Rencana anggaran tidak boleh bernilai negatif!");
        }
        this.nominalRencana = nominalRencana;
    }

    // Metode untuk menghitung sisa dari rencana anggaran yang dialokasikan
    public double hitungSisaRencana(double totalTerpakai) {
        return this.nominalRencana - totalTerpakai;
    }

    // Metode untuk memeriksa apakah total pengeluaran melebihi batas rencana anggaran
    public boolean cekMelebihiBudget(double totalTerpakai) {
        return totalTerpakai > this.nominalRencana;
    }
}
