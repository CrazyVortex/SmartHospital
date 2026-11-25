package servis;

public class RandevuDurumYoneticisi {

    public String durumEtiketiGetir(String durum) {
        if (durum == null) {
            return "Bilinmiyor";
        }

        switch (durum.trim().toLowerCase()) {
            case "bekliyor":
                return "Bekliyor";
            case "muayenede":
                return "Muayenede";
            case "tamamlandi":
                return "Tamamlandı";
            case "iptal":
                return "İptal";
            default:
                return "Geçersiz Durum";
        }
    }

    public boolean durumGecerliMi(String durum) {
        if (durum == null) {
            return false;
        }

        switch (durum.trim().toLowerCase()) {
            case "bekliyor":
            case "muayenede":
            case "tamamlandi":
            case "iptal":
                return true;
            default:
                return false;
        }
    }

    public boolean gecisIzniVarMi(String mevcutDurum, String yeniDurum) {
        if (!durumGecerliMi(mevcutDurum) || !durumGecerliMi(yeniDurum)) {
            return false;
        }

        String m = mevcutDurum.trim().toLowerCase();
        String y = yeniDurum.trim().toLowerCase();

        if (m.equals("tamamlandi") && !y.equals("tamamlandi")) {
            return false;
        }

        switch (y) {
            case "bekliyor":
            case "muayenede":
            case "tamamlandi":
            case "iptal":
                return true;
            default:
                return false;
        }
    }
}
