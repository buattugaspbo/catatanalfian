package com.keuangan.mahasiswa.model;

// Kelas Pengeluaran mewarisi seluruh atribut dan metode dari kelas Transaksi
public class Pengeluaran extends Transaksi {
    private String kategori;
    private String alasan;
    private String tingkatKebutuhan;

    public Pengeluaran(int id, String tanggal, double nominal, String keterangan, String kategori, String alasan, String tingkatKebutuhan) {
        super(id, tanggal, nominal, keterangan);
        
        // Validasi input parameter alasan agar tidak kosong
        if (alasan == null || alasan.trim().isEmpty()) {
            throw new IllegalArgumentException("Alasan pengeluaran tidak boleh kosong!");
        }
        this.kategori = kategori;
        this.alasan = alasan;
        this.tingkatKebutuhan = tingkatKebutuhan;
    }

    public String getKategori() {
        return kategori;
    }

    public void setKategori(String kategori) {
        this.kategori = kategori;
    }

    public String getAlasan() {
        return alasan;
    }

    public void setAlasan(String alasan) {
        if (alasan == null || alasan.trim().isEmpty()) {
            throw new IllegalArgumentException("Alasan pengeluaran tidak boleh kosong!");
        }
        this.alasan = alasan;
    }

    public String getTingkatKebutuhan() {
        return tingkatKebutuhan;
    }

    public void setTingkatKebutuhan(String tingkatKebutuhan) {
        this.tingkatKebutuhan = tingkatKebutuhan;
    }

    // Implementasi metode prosesTransaksi untuk transaksi pengeluaran
    @Override
    public void prosesTransaksi(Mahasiswa m, Tabungan t) {
        // Validasi agar pengeluaran tidak melebihi saldo saat ini
        if (getNominal() > m.getSaldo()) {
            throw new IllegalArgumentException("Saldo dompet tidak mencukupi!");
        }

        // Mengurangi saldo utama mahasiswa
        m.setSaldo(m.getSaldo() - getNominal());

        // Menambahkan nominal ke saldo tabungan jika kategori pengeluaran adalah Tabungan
        if ("Tabungan".equalsIgnoreCase(kategori) && t != null) {
            t.tambahTabungan(getNominal());
        }
    }

    @Override
    public String getType() {
        return "PENGELUARAN";
    }

    @Override
    public String getDetailInfo() {
        return "Kategori: " + kategori + " | Kebutuhan: " + tingkatKebutuhan + " | Alasan: " + alasan;
    }
}
