package model;

public class Doktor extends Kisi {
    private String uzmanlik;
    private Poliklinik poliklinik;

    public Doktor() {
    }

    public Doktor(int id, String ad, String soyad, String telefon,
                  String uzmanlik, Poliklinik poliklinik) {
        super(id, ad, soyad, telefon);
        this.uzmanlik = uzmanlik;
        this.poliklinik = poliklinik;
    }

    public String getUzmanlik() {
        return uzmanlik;
    }

    public void setUzmanlik(String uzmanlik) {
        this.uzmanlik = uzmanlik;
    }

    public Poliklinik getPoliklinik() {
        return poliklinik;
    }

    public void setPoliklinik(Poliklinik poliklinik) {
        this.poliklinik = poliklinik;
    }
}
