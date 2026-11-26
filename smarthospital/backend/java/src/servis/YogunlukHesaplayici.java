package servis;

import java.util.List;

public class YogunlukHesaplayici {

    public static final int VARSAYILAN_ORTALAMA_MUAYENE_DK = 12;

    public static final String NORMAL = "NORMAL";
    public static final String YOGUN = "YOGUN";
    public static final String KRITIK = "KRITIK";

    /** tahminiDakika = bekleyenSayisi * ortalamaMuayeneDk */
    public int tahminiBeklemeDk(int bekleyenSayisi, int ortalamaMuayeneDk) {
        if (bekleyenSayisi < 0 || ortalamaMuayeneDk < 0) {
            throw new IllegalArgumentException("Değerler negatif olamaz.");
        }
        return bekleyenSayisi * ortalamaMuayeneDk;
    }

    public int tahminiBeklemeDk(int bekleyenSayisi) {
        return tahminiBeklemeDk(bekleyenSayisi, VARSAYILAN_ORTALAMA_MUAYENE_DK);
    }

    /** yogunlukOrani = bekleyen / günlük kapasite * 100 */
    public double yogunlukOrani(int bekleyenSayisi, int gunlukKapasite) {
        if (gunlukKapasite <= 0) {
            throw new IllegalArgumentException("Günlük kapasite 0'dan büyük olmalıdır.");
        }
        return (bekleyenSayisi * 100.0) / gunlukKapasite;
    }

    /** Orana göre seviye belirler */
    public String yogunlukSeviyesi(double oran) {
        if (oran <= 50) {
            return NORMAL;
        } else if (oran <= 80) {
            return YOGUN;
        } else {
            return KRITIK;
        }
    }

    /** Döngü örnek tüm polikliniklerin bekleyen sayılarını toplar. */
    public int toplamBekleyen(List<Integer> poliklinikBekleyenleri) {
        int toplam = 0;
        for (int sayi : poliklinikBekleyenleri) {
            toplam += sayi;
        }
        return toplam;
    }
    public void analizYazdir(String[] adlar, int[] bekleyenler, int[] kapasiteler) {
        for (int i = 0; i < adlar.length; i++) {
            double oran = yogunlukOrani(bekleyenler[i], kapasiteler[i]);
            String seviye = yogunlukSeviyesi(oran);
            int dk = tahminiBeklemeDk(bekleyenler[i]);

            System.out.println(adlar[i] + " | Bekleyen: " + bekleyenler[i]
                    + " | Oran: %" + Math.round(oran) + " | " + seviye + " | ~" + dk + " dk");

            if (seviye.equals(KRITIK)) {
                System.out.println("   UYARI: " + adlar[i] + " kritik yoğunlukta!");
            }
        }
    }

    // Hızlı test
    public static void main(String[] args) {
        YogunlukHesaplayici h = new YogunlukHesaplayici();
        String[] adlar = {"Dahiliye", "Kardiyoloji", "KBB"};
        int[] bekleyenler = {4, 6, 9};
        int[] kapasiteler = {20, 10, 10};
        h.analizYazdir(adlar, bekleyenler, kapasiteler);
    }
}