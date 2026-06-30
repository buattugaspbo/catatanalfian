package com.keuangan.mahasiswa.model;

// Kelas User sebagai kelas induk (superclass) untuk menyimpan informasi dasar pengguna sistem
public class User {
    private int id;
    private String nama;
    private String email;
    private String password;
    private String role;

    // Konstruktor dasar tanpa password dan role (untuk backward compatibility)
    public User(int id, String nama, String email) {
        this.id = id;
        this.nama = nama;
        this.email = email;
        this.password = "";
        this.role = "MAHASISWA";
    }

    // Konstruktor lengkap dengan password dan role
    public User(int id, String nama, String email, String password, String role) {
        this.id = id;
        this.nama = nama;
        this.email = email;
        this.password = password;
        this.role = role;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // Metode untuk menampilkan informasi dasar pengguna
    public String tampilkanInfo() {
        return "Pengguna: " + nama + " (" + email + ")";
    }
}
