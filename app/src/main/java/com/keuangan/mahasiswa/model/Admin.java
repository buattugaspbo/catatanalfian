package com.keuangan.mahasiswa.model;

// Kelas Admin merupakan subclass dari User yang merepresentasikan pengguna dengan hak akses administrator
public class Admin extends User {

    public Admin(int id, String nama, String email, String password) {
        super(id, nama, email, password, "ADMIN");
    }

    // Metode untuk memverifikasi apakah objek ini memiliki hak akses admin
    public boolean punyaAksesAdmin() {
        return true;
    }

    // Overriding metode tampilkanInfo dari superclass User
    @Override
    public String tampilkanInfo() {
        return "Administrator Sistem | Email: " + getEmail();
    }
}
