package com.keuangan.mahasiswa.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.keuangan.mahasiswa.R;
import com.keuangan.mahasiswa.adapter.MahasiswaAdapter;
import com.keuangan.mahasiswa.database.DatabaseHelper;
import com.keuangan.mahasiswa.model.Mahasiswa;

import java.util.ArrayList;
import java.util.List;

// Activity untuk halaman administrator yang dapat mengelola data mahasiswa
public class AdminDashboardActivity extends AppCompatActivity implements MahasiswaAdapter.OnItemClickListener {

    private DatabaseHelper dbHelper;
    private List<Mahasiswa> mahasiswaList;
    private MahasiswaAdapter adapter;
    private TextView tvJumlahMahasiswa, tvEmptyState;
    private RecyclerView rvMahasiswa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        dbHelper = new DatabaseHelper(this);

        tvJumlahMahasiswa = findViewById(R.id.tvJumlahMahasiswa);
        tvEmptyState = findViewById(R.id.tvAdminEmptyState);
        rvMahasiswa = findViewById(R.id.rvMahasiswa);

        MaterialButton btnLogout = findViewById(R.id.btnLogoutAdmin);
        btnLogout.setOnClickListener(v -> logout());

        setupRecyclerView();
        loadMahasiswaData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMahasiswaData();
    }

    private void setupRecyclerView() {
        mahasiswaList = new ArrayList<>();
        adapter = new MahasiswaAdapter(mahasiswaList, this);
        rvMahasiswa.setLayoutManager(new LinearLayoutManager(this));
        rvMahasiswa.setAdapter(adapter);
    }

    private void loadMahasiswaData() {
        mahasiswaList.clear();
        mahasiswaList.addAll(dbHelper.getAllMahasiswaForAdmin());
        adapter.notifyDataSetChanged();

        int jumlah = mahasiswaList.size();
        tvJumlahMahasiswa.setText(jumlah + " mahasiswa terdaftar");

        if (jumlah == 0) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvMahasiswa.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvMahasiswa.setVisibility(View.VISIBLE);
        }
    }

    // Callback ketika admin mengklik item mahasiswa di daftar
    @Override
    public void onItemClick(Mahasiswa m) {
        // Menampilkan dialog pilihan aksi untuk mahasiswa yang dipilih
        String[] pilihan = {
                "Reset Password",
                "Ubah Email",
                "Reset Data Keuangan",
                "Batal"
        };

        new AlertDialog.Builder(this)
                .setTitle(m.getNama() + " (" + m.getNim() + ")")
                .setItems(pilihan, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            showResetPasswordDialog(m);
                            break;
                        case 1:
                            showUbahEmailDialog(m);
                            break;
                        case 2:
                            showResetDataDialog(m);
                            break;
                    }
                })
                .show();
    }

    private void showResetPasswordDialog(Mahasiswa m) {
        EditText etPasswordBaru = new EditText(this);
        etPasswordBaru.setHint("Password baru (min. 6 karakter)");
        etPasswordBaru.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        etPasswordBaru.setPadding(48, 32, 48, 32);

        new AlertDialog.Builder(this)
                .setTitle("Reset Password — " + m.getNama())
                .setView(etPasswordBaru)
                .setPositiveButton("Simpan", (dialog, which) -> {
                    String pwBaru = etPasswordBaru.getText().toString().trim();
                    if (pwBaru.length() < 6) {
                        Toast.makeText(this, "Password minimal 6 karakter!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    boolean ok = dbHelper.adminUpdatePassword(m.getId(), pwBaru);
                    Toast.makeText(this, ok ? "Password berhasil diubah!" : "Gagal mengubah password.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void showUbahEmailDialog(Mahasiswa m) {
        EditText etEmailBaru = new EditText(this);
        etEmailBaru.setHint("Email baru");
        etEmailBaru.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        etEmailBaru.setPadding(48, 32, 48, 32);

        new AlertDialog.Builder(this)
                .setTitle("Ubah Email — " + m.getNama())
                .setView(etEmailBaru)
                .setPositiveButton("Simpan", (dialog, which) -> {
                    String emailBaru = etEmailBaru.getText().toString().trim();
                    if (emailBaru.isEmpty()) {
                        Toast.makeText(this, "Email tidak boleh kosong!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    boolean ok = dbHelper.adminUpdateEmail(m.getId(), emailBaru);
                    if (ok) {
                        Toast.makeText(this, "Email berhasil diubah!", Toast.LENGTH_SHORT).show();
                        loadMahasiswaData();
                    } else {
                        Toast.makeText(this, "Email sudah digunakan oleh akun lain!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void showResetDataDialog(Mahasiswa m) {
        new AlertDialog.Builder(this)
                .setTitle("Reset Data Keuangan")
                .setMessage("Apakah Anda yakin ingin menghapus seluruh data keuangan milik " +
                        m.getNama() + "?\n\nTindakan ini tidak dapat dibatalkan.")
                .setPositiveButton("Ya, Reset", (dialog, which) -> {
                    dbHelper.adminResetUserData(m.getId());
                    Toast.makeText(this, "Data keuangan " + m.getNama() + " berhasil direset!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void logout() {
        // Menghapus data sesi login dari SharedPreferences
        getSharedPreferences("keuangan_prefs", MODE_PRIVATE)
                .edit().clear().apply();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
