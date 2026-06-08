package com.keuangan.mahasiswa.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.keuangan.mahasiswa.model.LaporanKeuangan;
import com.keuangan.mahasiswa.model.Mahasiswa;
import com.keuangan.mahasiswa.model.Pemasukan;
import com.keuangan.mahasiswa.model.Pengeluaran;
import com.keuangan.mahasiswa.model.RencanaPengeluaran;
import com.keuangan.mahasiswa.model.Tabungan;
import com.keuangan.mahasiswa.model.Transaksi;

import java.util.ArrayList;
import java.util.List;

/**
 * Kelas DatabaseHelper mengelola semua interaksi database SQLite lokal.
 * Menerapkan enkapsulasi kueri database sehingga logika query dipisah dari kelas Activity.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "keuangan_mahasiswa.db";
    private static final int DATABASE_VERSION = 1;

    // Nama-nama Tabel
    private static final String TABLE_MAHASISWA = "mahasiswa";
    private static final String TABLE_TABUNGAN = "tabungan";
    private static final String TABLE_RENCANA = "rencana_pengeluaran";
    private static final String TABLE_TRANSAKSI = "transaksi";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 1. Skema Tabel Mahasiswa
        db.execSQL("CREATE TABLE " + TABLE_MAHASISWA + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nama TEXT, " +
                "nim TEXT, " +
                "email TEXT, " +
                "uang_bulanan REAL, " +
                "saldo REAL);");

        // 2. Skema Tabel Tabungan
        db.execSQL("CREATE TABLE " + TABLE_TABUNGAN + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "saldo_tabungan REAL);");

        // 3. Skema Tabel Rencana Pengeluaran
        db.execSQL("CREATE TABLE " + TABLE_RENCANA + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "kategori TEXT UNIQUE, " +
                "nominal_rencana REAL);");

        // 4. Skema Tabel Transaksi (Polimorfis)
        db.execSQL("CREATE TABLE " + TABLE_TRANSAKSI + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "tanggal TEXT, " +
                "nominal REAL, " +
                "keterangan TEXT, " +
                "type TEXT, " + // PEMASUKAN / PENGELUARAN
                "sumber_pemasukan TEXT, " + // Spesifik Pemasukan
                "kategori TEXT, " + // Spesifik Pengeluaran
                "alasan TEXT, " + // Spesifik Pengeluaran
                "tingkat_kebutuhan TEXT);"); // Spesifik Pengeluaran
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MAHASISWA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TABUNGAN);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RENCANA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSAKSI);
        onCreate(db);
    }

    // =========================================================================
    // LOGIKA CRUD UNTUK OBJEK MAHASISWA
    // =========================================================================

    public Mahasiswa getMahasiswa() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_MAHASISWA, null, null, null, null, null, null);
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
        } else {
            // Jika kosong, buat mahasiswa default (Alfian Ferdiansyah)
            m = new Mahasiswa(1, "Alfian Ferdiansyah", "alfian@student.ac.id", "162024XXX", 0, 0);
            saveMahasiswa(m);
        }
        return m;
    }

    private void saveMahasiswa(Mahasiswa m) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", m.getId());
        values.put("nama", m.getNama());
        values.put("nim", m.getNim());
        values.put("email", m.getEmail());
        values.put("uang_bulanan", m.getUangBulanan());
        values.put("saldo", m.getSaldo());
        db.insert(TABLE_MAHASISWA, null, values);
    }

    public void updateMahasiswa(Mahasiswa m) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("uang_bulanan", m.getUangBulanan());
        values.put("saldo", m.getSaldo());
        db.update(TABLE_MAHASISWA, values, "id = ?", new String[]{String.valueOf(m.getId())});
    }

    // =========================================================================
    // LOGIKA CRUD UNTUK OBJEK TABUNGAN
    // =========================================================================

    public Tabungan getTabungan() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TABUNGAN, null, null, null, null, null, null);
        Tabungan t = null;

        if (cursor != null && cursor.moveToFirst()) {
            double saldoTabungan = cursor.getDouble(cursor.getColumnIndexOrThrow("saldo_tabungan"));
            t = new Tabungan(saldoTabungan);
            cursor.close();
        } else {
            t = new Tabungan(0);
            saveTabungan(t);
        }
        return t;
    }

    private void saveTabungan(Tabungan t) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", 1);
        values.put("saldo_tabungan", t.getSaldoTabungan());
        db.insert(TABLE_TABUNGAN, null, values);
    }

    public void updateTabungan(Tabungan t) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("saldo_tabungan", t.getSaldoTabungan());
        db.update(TABLE_TABUNGAN, values, "id = 1", null);
    }

    // =========================================================================
    // LOGIKA CRUD UNTUK OBJEK RENCANA PENGELUARAN
    // =========================================================================

    public List<RencanaPengeluaran> getAllRencana() {
        List<RencanaPengeluaran> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_RENCANA, null, null, null, null, null, null);

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

    public void addOrUpdateRencana(RencanaPengeluaran r) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("kategori", r.getKategori());
        values.put("nominal_rencana", r.getNominalRencana());

        int affected = db.update(TABLE_RENCANA, values, "kategori = ?", new String[]{r.getKategori()});
        if (affected == 0) {
            db.insert(TABLE_RENCANA, null, values);
        }
    }

    // =========================================================================
    // LOGIKA CRUD UNTUK OBJEK TRANSAKSI (POLIMORFISME PERSISTENCE)
    // =========================================================================

    public boolean insertTransaksi(Transaksi t) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("tanggal", t.getTanggal());
        values.put("nominal", t.getNominal());
        values.put("keterangan", t.getKeterangan());
        values.put("type", t.getType());

        // Menyimpan field spesifik berdasarkan sub-tipe polimorfis
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

    public List<Transaksi> getAllTransaksi() {
        List<Transaksi> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TRANSAKSI, null, null, null, null, null, "id DESC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String tanggal = cursor.getString(cursor.getColumnIndexOrThrow("tanggal"));
                double nominal = cursor.getDouble(cursor.getColumnIndexOrThrow("nominal"));
                String keterangan = cursor.getString(cursor.getColumnIndexOrThrow("keterangan"));
                String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));

                // Instansiasi objek konkret berdasarkan kolom 'type' (Polymorphism)
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

    public Transaksi getTransaksiById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TRANSAKSI, null, "id = ?", new String[]{String.valueOf(id)}, null, null, null);
        Transaksi t = null;

        if (cursor != null && cursor.moveToFirst()) {
            String tanggal = cursor.getString(cursor.getColumnIndexOrThrow("tanggal"));
            double nominal = cursor.getDouble(cursor.getColumnIndexOrThrow("nominal"));
            String keterangan = cursor.getString(cursor.getColumnIndexOrThrow("keterangan"));
            String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));

            if ("PEMASUKAN".equalsIgnoreCase(type)) {
                String sumber = cursor.getString(cursor.getColumnIndexOrThrow("sumber_pemasukan"));
                t = new Pemasukan(id, tanggal, nominal, keterangan, sumber);
            } else {
                String kategori = cursor.getString(cursor.getColumnIndexOrThrow("kategori"));
                String alasan = cursor.getString(cursor.getColumnIndexOrThrow("alasan"));
                String tingkat = cursor.getString(cursor.getColumnIndexOrThrow("tingkat_kebutuhan"));
                t = new Pengeluaran(id, tanggal, nominal, keterangan, kategori, alasan, tingkat);
            }
            cursor.close();
        }
        return t;
    }

    public boolean deleteTransaksi(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int affected = db.delete(TABLE_TRANSAKSI, "id = ?", new String[]{String.valueOf(id)});
        return affected > 0;
    }
}
