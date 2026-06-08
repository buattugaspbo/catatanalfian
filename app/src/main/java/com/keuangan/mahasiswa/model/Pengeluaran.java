package com.keuangan.mahasiswa.model;

/**
 * Konsep PBO: Inheritance & Polymorphism
 * Kelas Pengeluaran mewarisi dari Transaksi.
 * Meng-override prosesTransaksi() untuk mengurangi saldo utama dan mendepositkan uang ke tabungan.
 * Menerapkan enkapsulasi untuk validasi bahwa alasan pengeluaran tidak boleh kosong.
 */
public class Pengeluaran extends Transaksi {
    // Konsep PBO: Encapsulation
    private String kategori;
    private String alasan;
    private String tingkatKebutuhan; // Rendah, Sedang, Penting, Darurat

    public Pengeluaran(int id, String tanggal, double nominal, String keterangan, String kategori, String alasan, String tingkatKebutuhan) {
        super(id, tanggal, nominal, keterangan);
        
        // Validasi: Alasan pengeluaran tidak boleh kosong
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

    // Konsep PBO: Polymorphism - Overriding Method
    @Override
    public void prosesTransaksi(Mahasiswa m, Tabungan t) {
        // Validasi bisnis: Pengeluaran tidak boleh melebihi saldo berjalan mahasiswa
        if (getNominal() > m.getSaldo()) {
            throw new IllegalArgumentException("Saldo dompet tidak mencukupi!");
        }

        // Mengurangi saldo dompet
        m.setSaldo(m.getSaldo() - getNominal());

        // Jika pengeluaran bertipe investasi/tabungan, tambahkan ke saldo tabungan
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
