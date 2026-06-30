package com.keuangan.mahasiswa.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.keuangan.mahasiswa.R;
import com.keuangan.mahasiswa.database.DatabaseHelper;
import com.keuangan.mahasiswa.utils.ValidasiInput;

// Activity untuk registrasi akun mahasiswa baru
public class RegisterActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private TextInputEditText etNama, etNim, etEmail, etPassword, etKonfirmasi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new DatabaseHelper(this);

        etNama = findViewById(R.id.etRegisterNama);
        etNim = findViewById(R.id.etRegisterNim);
        etEmail = findViewById(R.id.etRegisterEmail);
        etPassword = findViewById(R.id.etRegisterPassword);
        etKonfirmasi = findViewById(R.id.etRegisterKonfirmasi);

        MaterialButton btnDaftar = findViewById(R.id.btnDaftar);
        TextView tvLoginLink = findViewById(R.id.tvLoginLink);

        btnDaftar.setOnClickListener(v -> prosesDaftar());
        tvLoginLink.setOnClickListener(v -> finish());
    }

    private void prosesDaftar() {
        String nama = etNama.getText() != null ? etNama.getText().toString().trim() : "";
        String nim = etNim.getText() != null ? etNim.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        String konfirmasi = etKonfirmasi.getText() != null ? etKonfirmasi.getText().toString().trim() : "";

        // Validasi semua field wajib diisi
        if (ValidasiInput.isEmpty(nama) || ValidasiInput.isEmpty(nim) ||
                ValidasiInput.isEmpty(email) || ValidasiInput.isEmpty(password) ||
                ValidasiInput.isEmpty(konfirmasi)) {
            Toast.makeText(this, "Semua field wajib diisi!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validasi kesesuaian password dan konfirmasi password
        if (!password.equals(konfirmasi)) {
            Toast.makeText(this, "Password dan konfirmasi password tidak cocok!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validasi panjang password minimal 6 karakter
        if (password.length() < 6) {
            Toast.makeText(this, "Password minimal 6 karakter!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Proses pendaftaran ke database
        boolean berhasil = dbHelper.registerMahasiswa(nama, nim, email, password);

        if (berhasil) {
            Toast.makeText(this, "Registrasi berhasil! Silakan login.", Toast.LENGTH_LONG).show();
            finish(); // Kembali ke halaman login
        } else {
            Toast.makeText(this, "Email sudah terdaftar! Gunakan email lain.", Toast.LENGTH_SHORT).show();
        }
    }
}
