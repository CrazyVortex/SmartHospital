package model;

public class Personel extends Kisi {
    private String rol;

    public Personel() {
    }

    public Personel(int id, String ad, String soyad, String telefon, String rol) {
        super(id, ad, soyad, telefon);
        this.rol = rol;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}
