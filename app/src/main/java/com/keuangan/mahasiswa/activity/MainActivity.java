package com.keuangan.mahasiswa.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.keuangan.mahasiswa.R;
import com.keuangan.mahasiswa.database.DatabaseHelper;

/**
 * Halaman Splash / Main Menu aplikasi.
 * Bertindak sebagai penyambut pertama sekaligus melakukan inisiasi data profil awal 
 * mahasiswa di SQLite secara otomatis jika database masih baru.
 */
public class MainActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        
        // Memicu getMahasiswa() untuk mendeteksi / menginisiasi profil mahasiswa default
        dbHelper.getMahasiswa();
        dbHelper.getTabungan(); // Inisiasi tabungan awal

        MaterialButton btnEnter = findViewById(R.id.btnEnter);
        btnEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                startActivity(intent);
                finish(); // Menutup Splash agar tidak bisa back ke halaman ini lagi
            }
        });
    }
}
