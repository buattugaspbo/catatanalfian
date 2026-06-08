package com.keuangan.mahasiswa.model;

/**
 * Konsep PBO: Encapsulation
 * Kelas RencanaPengeluaran memetakan rencana anggaran bulanan per kategori.
 * Menyediakan method evaluasi sisa budget dan status over-budget.
 */
public class RencanaPengeluaran {
    // Konsep PBO: Encapsulation
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

    // Metode menghitung sisa rencana anggaran
    public double hitungSisaRencana(double totalTerpakai) {
        return this.nominalRencana - totalTerpakai;
    }

    // Metode mengecek apakah pengeluaran melebihi anggaran rencana
    public boolean cekMelebihiBudget(double totalTerpakai) {
        return totalTerpakai > this.nominalRencana;
    }
}
