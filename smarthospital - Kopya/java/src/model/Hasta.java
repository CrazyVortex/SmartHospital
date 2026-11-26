package model;

public class Hasta {
    private int id;
    private String tcNo;
    private String ad;
    private String soyad;
    private String dogumTarihi;
    private String cinsiyet;
    private String telefon;
    private String kanGrubu;

    public Hasta() {
    }

    public Hasta(int id, String tcNo, String ad, String soyad, String dogumTarihi,
                 String cinsiyet, String telefon, String kanGrubu) {
        this.id = id;
        this.tcNo = tcNo;
        this.ad = ad;
        this.soyad = soyad;
        this.dogumTarihi = dogumTarihi;
        this.cinsiyet = cinsiyet;
        this.telefon = telefon;
        this.kanGrubu = kanGrubu;
    }

    public boolean kayitGecerliMi() {
        return zorunluAlanlarDoluMu() && tcGecerliMi() && telefonGecerliMi();
    }

    public boolean zorunluAlanlarDoluMu() {
        return tcNo != null && !tcNo.trim().isEmpty()
                && ad != null && !ad.trim().isEmpty()
                && soyad != null && !soyad.trim().isEmpty()
                && dogumTarihi != null && !dogumTarihi.trim().isEmpty()
                && cinsiyet != null && (cinsiyet.equalsIgnoreCase("K") || cinsiyet.equalsIgnoreCase("E"))
                && telefon != null && !telefon.trim().isEmpty();
    }

    public boolean tcGecerliMi() {
        return tcNo != null && tcNo.trim().matches("\\d{11}");
    }

    public boolean telefonGecerliMi() {
        return telefon != null && telefon.trim().matches("05\\d{9}");
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTcNo() { return tcNo; }
    public void setTcNo(String tcNo) { this.tcNo = tcNo; }

    public String getAd() { return ad; }
    public void setAd(String ad) { this.ad = ad; }

    public String getSoyad() { return soyad; }
    public void setSoyad(String soyad) { this.soyad = soyad; }

    public String getDogumTarihi() { return dogumTarihi; }
    public void setDogumTarihi(String dogumTarihi) { this.dogumTarihi = dogumTarihi; }

    public String getCinsiyet() { return cinsiyet; }
    public void setCinsiyet(String cinsiyet) { this.cinsiyet = cinsiyet; }

    public String getTelefon() { return telefon; }
    public void setTelefon(String telefon) { this.telefon = telefon; }

    public String getKanGrubu() { return kanGrubu; }
    public void setKanGrubu(String kanGrubu) { this.kanGrubu = kanGrubu; }
}
