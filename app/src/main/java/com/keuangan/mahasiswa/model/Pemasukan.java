package com.keuangan.mahasiswa.model;

// Kelas Pemasukan mewarisi seluruh atribut dan metode dari kelas Transaksi
public class Pemasukan extends Transaksi {
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

    // Implementasi metode prosesTransaksi untuk transaksi pemasukan
    @Override
    public void prosesTransaksi(Mahasiswa m, Tabungan t) {
        // Menambah saldo dompet mahasiswa
        m.setSaldo(m.getSaldo() + getNominal());

        // Mengurangi saldo tabungan apabila sumber dana ditarik dari tabungan
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
