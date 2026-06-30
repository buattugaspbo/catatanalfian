package com.keuangan.mahasiswa.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.keuangan.mahasiswa.R;
import com.keuangan.mahasiswa.database.DatabaseHelper;
import com.keuangan.mahasiswa.model.Admin;
import com.keuangan.mahasiswa.model.User;
import com.keuangan.mahasiswa.utils.ValidasiInput;

// Activity untuk autentikasi login mahasiswa dan admin
public class LoginActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private TextInputEditText etEmail, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Periksa apakah sudah ada sesi login yang tersimpan
        SharedPreferences prefs = getSharedPreferences("keuangan_prefs", MODE_PRIVATE);
        int savedUserId = prefs.getInt("user_id", -1);
        String savedRole = prefs.getString("user_role", "");

        if (savedUserId != -1) {
            // Arahkan langsung ke dashboard yang sesuai tanpa login ulang
            redirectByRole(savedRole);
            return;
        }

        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelper(this);
        etEmail = findViewById(R.id.etLoginEmail);
        etPassword = findViewById(R.id.etLoginPassword);

        MaterialButton btnLogin = findViewById(R.id.btnLogin);
        TextView tvDaftarLink = findViewById(R.id.tvDaftarLink);

        btnLogin.setOnClickListener(v -> prosesLogin());
        tvDaftarLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void prosesLogin() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if (ValidasiInput.isEmpty(email) || ValidasiInput.isEmpty(password)) {
            Toast.makeText(this, "Email dan password wajib diisi!", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = dbHelper.loginUser(email, password);

        if (user == null) {
            Toast.makeText(this, "Email atau password salah!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Simpan sesi login ke SharedPreferences
        SharedPreferences prefs = getSharedPreferences("keuangan_prefs", MODE_PRIVATE);
        prefs.edit()
                .putInt("user_id", user.getId())
                .putString("user_role", user.getRole())
                .putString("user_nama", user.getNama())
                .apply();

        Toast.makeText(this, "Selamat datang, " + user.getNama() + "!", Toast.LENGTH_SHORT).show();
        redirectByRole(user.getRole());
    }

    // Mengarahkan ke halaman yang sesuai berdasarkan role pengguna (polimorfisme runtime)
    private void redirectByRole(String role) {
        Intent intent;
        if ("ADMIN".equalsIgnoreCase(role)) {
            intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
        } else {
            intent = new Intent(LoginActivity.this, DashboardActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
