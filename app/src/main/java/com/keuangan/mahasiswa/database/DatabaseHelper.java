package com.keuangan.mahasiswa.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.keuangan.mahasiswa.model.Admin;
import com.keuangan.mahasiswa.model.Mahasiswa;
import com.keuangan.mahasiswa.model.Pemasukan;
import com.keuangan.mahasiswa.model.Pengeluaran;
import com.keuangan.mahasiswa.model.RencanaPengeluaran;
import com.keuangan.mahasiswa.model.Tabungan;
import com.keuangan.mahasiswa.model.Transaksi;
import com.keuangan.mahasiswa.model.User;

import java.util.ArrayList;
import java.util.List;

// Kelas DatabaseHelper untuk mengelola interaksi dengan database SQLite lokal.
// Kelas ini memisahkan logika query database dari kelas Activity.
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "keuangan_mahasiswa.db";
    private static final int DATABASE_VERSION = 2;

    // Nama-nama tabel
    private static final String TABLE_MAHASISWA = "mahasiswa";
    private static final String TABLE_TABUNGAN = "tabungan";
    private static final String TABLE_RENCANA = "rencana_pengeluaran";
    private static final String TABLE_TRANSAKSI = "transaksi";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Skema Tabel Mahasiswa (sekaligus tabel User untuk semua role)
        db.execSQL("CREATE TABLE " + TABLE_MAHASISWA + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nama TEXT, " +
                "nim TEXT, " +
                "email TEXT UNIQUE, " +
                "password TEXT, " +
                "uang_bulanan REAL DEFAULT 0, " +
                "saldo REAL DEFAULT 0, " +
                "role TEXT DEFAULT 'MAHASISWA');");

        // Skema Tabel Tabungan (terikat per user)
        db.execSQL("CREATE TABLE " + TABLE_TABUNGAN + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER UNIQUE, " +
                "saldo_tabungan REAL DEFAULT 0);");

        // Skema Tabel Rencana Pengeluaran (terikat per user)
        db.execSQL("CREATE TABLE " + TABLE_RENCANA + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "kategori TEXT, " +
                "nominal_rencana REAL, " +
                "UNIQUE(user_id, kategori));");

        // Skema Tabel Transaksi (terikat per user)
        db.execSQL("CREATE TABLE " + TABLE_TRANSAKSI + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "tanggal TEXT, " +
                "nominal REAL, " +
                "keterangan TEXT, " +
                "type TEXT, " +
                "sumber_pemasukan TEXT, " +
                "kategori TEXT, " +
                "alasan TEXT, " +
                "tingkat_kebutuhan TEXT);");

        // Menyisipkan akun admin default saat database pertama kali dibuat
        db.execSQL("INSERT INTO " + TABLE_MAHASISWA +
                " (nama, nim, email, password, uang_bulanan, saldo, role) VALUES " +
                "('Administrator', 'ADMIN-001', 'admin@keuangan.com', 'admin123', 0, 0, 'ADMIN')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Menghapus semua tabel lama dan membuat ulang dengan skema baru
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MAHASISWA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TABUNGAN);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RENCANA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSAKSI);
        onCreate(db);
    }

    // =========================================================================
    // METODE AUTENTIKASI (LOGIN & REGISTER)
    // =========================================================================

    // Mencoba login berdasarkan email dan password, mengembalikan objek User atau null
    public User loginUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_MAHASISWA, null,
                "email = ? AND password = ?",
                new String[]{email, password}, null, null, null);

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            String nama = cursor.getString(cursor.getColumnIndexOrThrow("nama"));
            String nim = cursor.getString(cursor.getColumnIndexOrThrow("nim"));
            String emailDb = cursor.getString(cursor.getColumnIndexOrThrow("email"));
            String passwordDb = cursor.getString(cursor.getColumnIndexOrThrow("password"));
            double uangBulanan = cursor.getDouble(cursor.getColumnIndexOrThrow("uang_bulanan"));
            double saldo = cursor.getDouble(cursor.getColumnIndexOrThrow("saldo"));
            String role = cursor.getString(cursor.getColumnIndexOrThrow("role"));

            // Polimorfisme: kembalikan tipe objek sesuai role pengguna
            if ("ADMIN".equalsIgnoreCase(role)) {
                user = new Admin(id, nama, emailDb, passwordDb);
            } else {
                user = new Mahasiswa(id, nama, emailDb, nim, uangBulanan, saldo);
                user.setPassword(passwordDb);
            }
            cursor.close();
        }
        return user;
    }

    // Mendaftarkan mahasiswa baru, mengembalikan false jika email sudah terdaftar
    public boolean registerMahasiswa(String nama, String nim, String email, String password) {
        if (isEmailTerdaftar(email)) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nama", nama);
        values.put("nim", nim);
        values.put("email", email);
        values.put("password", password);
        values.put("uang_bulanan", 0.0);
        values.put("saldo", 0.0);
        values.put("role", "MAHASISWA");

        long result = db.insert(TABLE_MAHASISWA, null, values);
        if (result != -1) {
            // Inisialisasi baris tabungan kosong untuk user baru
            ContentValues tabValues = new ContentValues();
            tabValues.put("user_id", (int) result);
            tabValues.put("saldo_tabungan", 0.0);
            db.insert(TABLE_TABUNGAN, null, tabValues);
        }
        return result != -1;
    }

    // Memeriksa apakah email sudah terdaftar di database
    public boolean isEmailTerdaftar(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_MAHASISWA, new String[]{"id"},
                "email = ?", new String[]{email}, null, null, null);
        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) cursor.close();
        return exists;
    }

    // =========================================================================
    // METODE UNTUK MENGELOLA DATA MAHASISWA
    // =========================================================================

    public Mahasiswa getMahasiswa(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_MAHASISWA, null,
                "id = ?", new String[]{String.valueOf(userId)}, null, null, null);
        Mahasiswa m = null;

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            String nama = cursor.getString(cursor.getColumnIndexOrThrow("nama"));
            String nim = cursor.getString(cursor.getColumnIndexOrThrow("nim"));
            String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
            double uangBulanan = cursor.getDouble(cursor.getColumnIndexOrThrow("uang_bulanan"));
            double saldo = cursor.getDouble(cursor.getColumnIndexOrThrow("saldo"));
            m = new Mahasiswa(id, nama, email, nim, uangBulanan, saldo);
            cursor.close();
        }
        return m;
    }

    public void updateMahasiswa(Mahasiswa m) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nama", m.getNama());
        values.put("nim", m.getNim());
        values.put("email", m.getEmail());
        values.put("uang_bulanan", m.getUangBulanan());
        values.put("saldo", m.getSaldo());
        db.update(TABLE_MAHASISWA, values, "id = ?", new String[]{String.valueOf(m.getId())});
    }

    // Mereset data keuangan milik user tertentu (dipakai oleh admin)
    public void resetDatabase(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_TRANSAKSI + " WHERE user_id = " + userId);
        db.execSQL("DELETE FROM " + TABLE_RENCANA + " WHERE user_id = " + userId);

        ContentValues tabValues = new ContentValues();
        tabValues.put("saldo_tabungan", 0.0);
        db.update(TABLE_TABUNGAN, tabValues, "user_id = ?", new String[]{String.valueOf(userId)});

        ContentValues mValues = new ContentValues();
        mValues.put("uang_bulanan", 0.0);
        mValues.put("saldo", 0.0);
        db.update(TABLE_MAHASISWA, mValues, "id = ?", new String[]{String.valueOf(userId)});
    }

    // =========================================================================
    // METODE KHUSUS ADMIN
    // =========================================================================

    // Mengambil semua data mahasiswa (bukan admin) untuk ditampilkan oleh admin
    public List<Mahasiswa> getAllMahasiswaForAdmin() {
        List<Mahasiswa> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_MAHASISWA, null,
                "role = ?", new String[]{"MAHASISWA"}, null, null, "nama ASC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String nama = cursor.getString(cursor.getColumnIndexOrThrow("nama"));
                String nim = cursor.getString(cursor.getColumnIndexOrThrow("nim"));
                String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
                double uangBulanan = cursor.getDouble(cursor.getColumnIndexOrThrow("uang_bulanan"));
                double saldo = cursor.getDouble(cursor.getColumnIndexOrThrow("saldo"));
                list.add(new Mahasiswa(id, nama, email, nim, uangBulanan, saldo));
            }
            cursor.close();
        }
        return list;
    }

    // Memperbarui password mahasiswa berdasarkan ID (aksi admin)
    public boolean adminUpdatePassword(int userId, String passwordBaru) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("password", passwordBaru);
        int affected = db.update(TABLE_MAHASISWA, values, "id = ?", new String[]{String.valueOf(userId)});
        return affected > 0;
    }

    // Memperbarui email mahasiswa berdasarkan ID (aksi admin)
    public boolean adminUpdateEmail(int userId, String emailBaru) {
        if (isEmailTerdaftar(emailBaru)) return false;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("email", emailBaru);
        int affected = db.update(TABLE_MAHASISWA, values, "id = ?", new String[]{String.valueOf(userId)});
        return affected > 0;
    }

    // Mereset seluruh data keuangan mahasiswa (aksi admin darurat)
    public void adminResetUserData(int userId) {
        resetDatabase(userId);
    }

    // =========================================================================
    // METODE UNTUK MENGELOLA DATA TABUNGAN
    // =========================================================================

    public Tabungan getTabungan(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TABUNGAN, null,
                "user_id = ?", new String[]{String.valueOf(userId)}, null, null, null);
        Tabungan t;

        if (cursor != null && cursor.moveToFirst()) {
            double saldoTabungan = cursor.getDouble(cursor.getColumnIndexOrThrow("saldo_tabungan"));
            t = new Tabungan(saldoTabungan);
            cursor.close();
        } else {
            // Inisialisasi tabungan jika belum ada baris untuk user ini
            t = new Tabungan(0);
            SQLiteDatabase dbWrite = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("user_id", userId);
            values.put("saldo_tabungan", 0.0);
            dbWrite.insert(TABLE_TABUNGAN, null, values);
        }
        return t;
    }

    public void updateTabungan(Tabungan t, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("saldo_tabungan", t.getSaldoTabungan());
        db.update(TABLE_TABUNGAN, values, "user_id = ?", new String[]{String.valueOf(userId)});
    }

    // =========================================================================
    // METODE UNTUK MENGELOLA RENCANA PENGELUARAN
    // =========================================================================

    public List<RencanaPengeluaran> getAllRencana(int userId) {
        List<RencanaPengeluaran> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_RENCANA, null,
                "user_id = ?", new String[]{String.valueOf(userId)}, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String kategori = cursor.getString(cursor.getColumnIndexOrThrow("kategori"));
                double nominal = cursor.getDouble(cursor.getColumnIndexOrThrow("nominal_rencana"));
                list.add(new RencanaPengeluaran(kategori, nominal));
            }
            cursor.close();
        }
        return list;
    }

    public void addOrUpdateRencana(RencanaPengeluaran r, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("kategori", r.getKategori());
        values.put("nominal_rencana", r.getNominalRencana());

        int affected = db.update(TABLE_RENCANA, values,
                "user_id = ? AND kategori = ?",
                new String[]{String.valueOf(userId), r.getKategori()});
        if (affected == 0) {
            db.insert(TABLE_RENCANA, null, values);
        }
    }

    // Menghapus rencana pengeluaran berdasarkan kategori dan user
    public boolean deleteRencana(String kategori, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int affected = db.delete(TABLE_RENCANA,
                "kategori = ? AND user_id = ?",
                new String[]{kategori, String.valueOf(userId)});
        return affected > 0;
    }

    // =========================================================================
    // METODE UNTUK MENGELOLA DATA TRANSAKSI
    // =========================================================================

    public boolean insertTransaksi(Transaksi t, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("tanggal", t.getTanggal());
        values.put("nominal", t.getNominal());
        values.put("keterangan", t.getKeterangan());
        values.put("type", t.getType());

        // Menyimpan nilai field spesifik berdasarkan sub-tipe objek Transaksi
        if (t instanceof Pemasukan) {
            values.put("sumber_pemasukan", ((Pemasukan) t).getSumberPemasukan());
        } else if (t instanceof Pengeluaran) {
            values.put("kategori", ((Pengeluaran) t).getKategori());
            values.put("alasan", ((Pengeluaran) t).getAlasan());
            values.put("tingkat_kebutuhan", ((Pengeluaran) t).getTingkatKebutuhan());
        }

        long res = db.insert(TABLE_TRANSAKSI, null, values);
        return res != -1;
    }

    public List<Transaksi> getAllTransaksi(int userId) {
        List<Transaksi> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TRANSAKSI, null,
                "user_id = ?", new String[]{String.valueOf(userId)},
                null, null, "id DESC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String tanggal = cursor.getString(cursor.getColumnIndexOrThrow("tanggal"));
                double nominal = cursor.getDouble(cursor.getColumnIndexOrThrow("nominal"));
                String keterangan = cursor.getString(cursor.getColumnIndexOrThrow("keterangan"));
                String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));

                if ("PEMASUKAN".equalsIgnoreCase(type)) {
                    String sumber = cursor.getString(cursor.getColumnIndexOrThrow("sumber_pemasukan"));
                    list.add(new Pemasukan(id, tanggal, nominal, keterangan, sumber));
                } else {
                    String kategori = cursor.getString(cursor.getColumnIndexOrThrow("kategori"));
                    String alasan = cursor.getString(cursor.getColumnIndexOrThrow("alasan"));
                    String tingkat = cursor.getString(cursor.getColumnIndexOrThrow("tingkat_kebutuhan"));
                    list.add(new Pengeluaran(id, tanggal, nominal, keterangan, kategori, alasan, tingkat));
                }
            }
            cursor.close();
        }
        return list;
    }

    public boolean deleteTransaksi(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int affected = db.delete(TABLE_TRANSAKSI, "id = ?", new String[]{String.valueOf(id)});
        return affected > 0;
    }
}
