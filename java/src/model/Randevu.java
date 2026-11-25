package model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Randevu {
    private int id;
    private int hastaId;
    private int doktorId;
    private LocalDate tarih;
    private LocalTime saat;
    private String durum;

    public Randevu() {
    }

    public Randevu(int id, int hastaId, int doktorId, LocalDate tarih,
                   LocalTime saat, String durum) {
        this.id = id;
        this.hastaId = hastaId;
        this.doktorId = doktorId;
        this.tarih = tarih;
        this.saat = saat;
        this.durum = durum;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getHastaId() {
        return hastaId;
    }

    public void setHastaId(int hastaId) {
        this.hastaId = hastaId;
    }

    public int getDoktorId() {
        return doktorId;
    }

    public void setDoktorId(int doktorId) {
        this.doktorId = doktorId;
    }

    public LocalDate getTarih() {
        return tarih;
    }

    public void setTarih(LocalDate tarih) {
        this.tarih = tarih;
    }

    public LocalTime getSaat() {
        return saat;
    }

    public void setSaat(LocalTime saat) {
        this.saat = saat;
    }

    public String getDurum() {
        return durum;
    }

    public void setDurum(String durum) {
        this.durum = durum;
    }
}
