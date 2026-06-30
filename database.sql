-- =========================================================================
-- SQL Script untuk Pembuatan Database "keuangan_mahasiswa" (MySQL / MariaDB)
-- Dibuat berdasarkan kelas Java DatabaseHelper di aplikasi Pencatatan Keuangan Mahasiswa
-- =========================================================================

-- 1. Membuat Database (jika belum ada)
CREATE DATABASE IF NOT EXISTS keuangan_mahasiswa;
USE keuangan_mahasiswa;

-- =========================================================================
-- 2. Membuat Tabel: mahasiswa
-- Menyimpan data profil mahasiswa (saat ini diasumsikan single-user untuk aplikasi lokal)
-- =========================================================================
CREATE TABLE IF NOT EXISTS mahasiswa (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nama VARCHAR(100) NOT NULL,
    nim VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    uang_bulanan DECIMAL(15, 2) DEFAULT 0.00,
    saldo DECIMAL(15, 2) DEFAULT 0.00
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================================
-- 3. Membuat Tabel: tabungan
-- Menyimpan data total tabungan mahasiswa
-- =========================================================================
CREATE TABLE IF NOT EXISTS tabungan (
    id INT AUTO_INCREMENT PRIMARY KEY,
    saldo_tabungan DECIMAL(15, 2) DEFAULT 0.00
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================================
-- 4. Membuat Tabel: rencana_pengeluaran
-- Menyimpan alokasi rencana anggaran pengeluaran per kategori
-- =========================================================================
CREATE TABLE IF NOT EXISTS rencana_pengeluaran (
    id INT AUTO_INCREMENT PRIMARY KEY,
    kategori VARCHAR(50) UNIQUE NOT NULL,
    nominal_rencana DECIMAL(15, 2) DEFAULT 0.00
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================================
-- 5. Membuat Tabel: transaksi
-- Menyimpan data riwayat transaksi (Pemasukan dan Pengeluaran)
-- Menggunakan satu tabel (Single Table Inheritance pattern)
-- =========================================================================
CREATE TABLE IF NOT EXISTS transaksi (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tanggal VARCHAR(50) NOT NULL,              -- Menyimpan tanggal transaksi (format string: dd-MM-yyyy atau yyyy-MM-dd)
    nominal DECIMAL(15, 2) NOT NULL,           -- Jumlah uang transaksi
    keterangan TEXT,                           -- Catatan singkat transaksi
    type VARCHAR(20) NOT NULL,                 -- Jenis transaksi: 'PEMASUKAN' atau 'PENGELUARAN'
    sumber_pemasukan VARCHAR(100) DEFAULT NULL, -- Hanya diisi jika type = 'PEMASUKAN' (contoh: 'Orang Tua', 'Beasiswa')
    kategori VARCHAR(50) DEFAULT NULL,         -- Hanya diisi jika type = 'PENGELUARAN' (contoh: 'Makanan', 'Kuliah')
    alasan TEXT DEFAULT NULL,                  -- Hanya diisi jika type = 'PENGELUARAN' (alasan membeli sesuatu)
    tingkat_kebutuhan VARCHAR(50) DEFAULT NULL -- Hanya diisi jika type = 'PENGELUARAN' (contoh: 'Kebutuhan', 'Keinginan')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================================
-- 6. Insert Data Default Awal (Opsional - Sesuai dengan inisialisasi di kode Java)
-- =========================================================================
INSERT INTO mahasiswa (id, nama, nim, email, uang_bulanan, saldo) 
VALUES (1, 'Alfian Ferdiansyah', '162024XXX', 'alfian@student.ac.id', 0.00, 0.00)
ON DUPLICATE KEY UPDATE id=id;

INSERT INTO tabungan (id, saldo_tabungan) 
VALUES (1, 0.00)
ON DUPLICATE KEY UPDATE id=id;
