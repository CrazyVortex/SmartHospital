package model;

public class Muayene {
    private int id; // private dışarıdan doğrudan değiştirilemez
    private int randevuId;
    private String sikayet;
    private String tani;
    private String recete;
    private String olusturmaTarihi;

    public Muayene() {
    }

    public Muayene(int id, int randevuId, String sikayet, String tani,  // kurucu (constructor) biri boş nesne oluşturuyor diğerı bılgı dolduruyr
                   String recete, String olusturmaTarihi) {
        this.id = id; // bu nesneye gelen ıdyi ata
        this.randevuId = randevuId;
        this.sikayet = sikayet;
        this.tani = tani;
        this.recete = recete;
        this.olusturmaTarihi = olusturmaTarihi;
    }

    public int getId() { return id; } // getter degeri okumak ıcin
    public void setId(int id) { this.id = id; } //setter değeri değiştirmek özel alana erişim

    public int getRandevuId() { return randevuId; }
    public void setRandevuId(int randevuId) { this.randevuId = randevuId; }

    public String getSikayet() { return sikayet; }
    public void setSikayet(String sikayet) { this.sikayet = sikayet; }

    public String getTani() { return tani; }
    public void setTani(String tani) { this.tani = tani; }

    public String getRecete() { return recete; }
    public void setRecete(String recete) { this.recete = recete; }

    public String getOlusturmaTarihi() { return olusturmaTarihi; }
    public void setOlusturmaTarihi(String olusturmaTarihi) { this.olusturmaTarihi = olusturmaTarihi; }

    /** Zorunlu alanlar (şikayet ve tanı) dolu mu? 5. günde doğrulama için kullanılacak. */
    public boolean gecerliMi() {  // sikayet ve tanı dolumu
        return sikayet != null && !sikayet.trim().isEmpty() // trim baştaki ve sondaki boşlukları siler
                && tani != null && !tani.trim().isEmpty();
    }

    public String muayeneBilgi() {
        return "Muayene: " + id + " Randevu: " + randevuId + " Tanı: " + tani;
    }
}
