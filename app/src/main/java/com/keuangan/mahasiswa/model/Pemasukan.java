package com.keuangan.mahasiswa.model;

/**
 * Konsep PBO: Inheritance & Polymorphism
 * Kelas Pemasukan mewarisi dari Transaksi.
 * Meng-override prosesTransaksi() untuk menambah saldo mahasiswa dan menangani penarikan tabungan.
 */
public class Pemasukan extends Transaksi {
    // Konsep PBO: Encapsulation
    private String sumberPemasukan;

    public Pemasukan(int id, String tanggal, double nominal, String keterangan, String sumberPemasukan) {
        super(id, tanggal, nominal, keterangan);
        this.sumberPemasukan = sumberPemasukan;
    }

    public String getSumberPemasukan() {
        return sumberPemasukan;
    }

    public void setSumberPemasukan(String sumberPemasukan) {
        this.sumberPemasukan = sumberPemasukan;
    }

    // Konsep PBO: Polymorphism - Overriding Method
    @Override
    public void prosesTransaksi(Mahasiswa m, Tabungan t) {
        // Logika bisnis: Saldo utama bertambah
        m.setSaldo(m.getSaldo() + getNominal());

        // Jika sumber pemasukan berasal dari pengambilan tabungan, kurangi saldo tabungan
        if ("Ambil Tabungan".equalsIgnoreCase(sumberPemasukan) && t != null) {
            t.ambilTabungan(getNominal());
        }
    }

    @Override
    public String getType() {
        return "PEMASUKAN";
    }

    @Override
    public String getDetailInfo() {
        return "Sumber: " + sumberPemasukan;
    }
}
