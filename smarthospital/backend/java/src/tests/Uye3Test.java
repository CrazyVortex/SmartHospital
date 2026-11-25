import model.Muayene;
import servis.ApiBaglantiServisi;

import java.io.IOException;

public class Uye3Test {
    public static void main(String[] args) {
        ApiBaglantiServisi servis = new ApiBaglantiServisi();
        try {
            System.out.println("--- DASHBOARD ---");
            System.out.println(servis.dashboardOzetGetir());

            System.out.println("--- YOGUNLUK ---");
            System.out.println(servis.yogunlukAnaliziGetir());

            Muayene m = new Muayene();
            m.setRandevuId(1);          // bekleyen bir randevunun id'sini yaz
            m.setSikayet("Öksürük");
            m.setTani("Grip");
            m.setRecete("Dinlenme");

            System.out.println("--- MUAYENE KAYDET ---");
            System.out.println(servis.muayeneKaydet(m));
        } catch (IOException | InterruptedException e) {
            System.out.println("Bağlantı hatası: " + e.getMessage());
        }
    }
}
