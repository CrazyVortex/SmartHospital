package model;

public class Poliklinik {
    private int id;
    private String poliklinikAdi;
    private String konum;
    private int gunlukKapasite;

    public Poliklinik() {
    }

    public Poliklinik(int id, String poliklinikAdi, String konum, int gunlukKapasite) {
        this.id = id;
        this.poliklinikAdi = poliklinikAdi;
        this.konum = konum;
        this.gunlukKapasite = gunlukKapasite;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPoliklinikAdi() {
        return poliklinikAdi;
    }

    public void setPoliklinikAdi(String poliklinikAdi) {
        this.poliklinikAdi = poliklinikAdi;
    }

    public String getKonum() {
        return konum;
    }

    public void setKonum(String konum) {
        this.konum = konum;
    }

    public int getGunlukKapasite() {
        return gunlukKapasite;
    }

    public void setGunlukKapasite(int gunlukKapasite) {
        this.gunlukKapasite = gunlukKapasite;
    }
}
