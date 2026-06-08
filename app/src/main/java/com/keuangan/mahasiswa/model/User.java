package com.keuangan.mahasiswa.model;

/**
 * Konsep PBO: Inheritance - Superclass
 * Kelas User mendefinisikan informasi dasar pengguna sistem.
 * Kelas ini bertindak sebagai kelas induk untuk kelas Mahasiswa.
 */
public class User {
    // Konsep PBO: Encapsulation (Atribut dideklarasikan private)
    private int id;
    private String nama;
    private String email;

    public User(int id, String nama, String email) {
        this.id = id;
        this.nama = nama;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Metode menampilkan info dasar
    public String tampilkanInfo() {
        return "Pengguna: " + nama + " (" + email + ")";
    }
}
