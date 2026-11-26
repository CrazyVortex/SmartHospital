package servis;

import model.Doktor;
import model.Poliklinik;
import model.Randevu;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RandevuKontrolServisi {
    public static final String DURUM_AKTIF = "Aktif";
    public static final String DURUM_IPTAL = "İptal";

    private final List<Poliklinik> poliklinikler;
    private final List<Doktor> doktorlar;
    private final List<Randevu> randevular;
    private final Map<Integer, List<LocalTime>> doktorCalismaSaatleri;

    public RandevuKontrolServisi(List<Poliklinik> poliklinikler,
                                 List<Doktor> doktorlar,
                                 List<Randevu> randevular,
                                 Map<Integer, List<LocalTime>> doktorCalismaSaatleri) {
        this.poliklinikler = new ArrayList<>(Objects.requireNonNull(
                poliklinikler, "Poliklinik listesi null olamaz."));
        this.doktorlar = new ArrayList<>(Objects.requireNonNull(
                doktorlar, "Doktor listesi null olamaz."));
        this.randevular = new ArrayList<>();
        for (Randevu randevu : Objects.requireNonNull(
                randevular, "Randevu listesi null olamaz.")) {
            this.randevular.add(kopyalaRandevu(Objects.requireNonNull(
                    randevu, "Randevu kaydi null olamaz.")));
        }

        this.doktorCalismaSaatleri = new HashMap<>();
        for (Map.Entry<Integer, List<LocalTime>> entry : Objects.requireNonNull(
                doktorCalismaSaatleri, "Doktor calisma saatleri null olamaz.").entrySet()) {
            Integer doktorId = Objects.requireNonNull(
                    entry.getKey(), "Calisma saatinin doktor ID degeri null olamaz.");
            List<LocalTime> saatler = new ArrayList<>(Objects.requireNonNull(
                    entry.getValue(), "Doktorun calisma saatleri null olamaz."));
            if (saatler.contains(null)) {
                throw new IllegalArgumentException("Calisma saatleri null deger iceremez.");
            }
            Collections.sort(saatler);
            this.doktorCalismaSaatleri.put(doktorId, saatler);
        }
    }

    public synchronized List<LocalTime> uygunSaatleriGetir(int poliklinikId,
                                                            int doktorId,
                                                            LocalDate tarih) {
        dogrulaTarih(tarih);
        dogrulaDoktorVePoliklinik(poliklinikId, doktorId);

        List<LocalTime> calismaSaatleri = doktorCalismaSaatleri.get(doktorId);
        if (calismaSaatleri == null || calismaSaatleri.isEmpty()) {
            throw new IllegalArgumentException("Doktor icin calisma saati tanimlanmamis.");
        }

        List<LocalTime> uygunSaatler = new ArrayList<>();
        for (LocalTime saat : calismaSaatleri) {
            if (!aktifRandevuVarMi(doktorId, tarih, saat)) {
                uygunSaatler.add(saat);
            }
        }
        return Collections.unmodifiableList(uygunSaatler);
    }

    public synchronized Randevu randevuOlustur(int randevuId,
                                                int hastaId,
                                                int poliklinikId,
                                                int doktorId,
                                                LocalDate tarih,
                                                LocalTime saat) {
        if (randevuId <= 0) {
            throw new IllegalArgumentException("Randevu ID pozitif olmalidir.");
        }
        if (hastaId <= 0) {
            throw new IllegalArgumentException("Hasta ID pozitif olmalidir.");
        }
        dogrulaTarih(tarih);
        if (saat == null) {
            throw new IllegalArgumentException("Randevu saati zorunludur.");
        }

        dogrulaDoktorVePoliklinik(poliklinikId, doktorId);
        List<LocalTime> calismaSaatleri = doktorCalismaSaatleri.get(doktorId);
        if (calismaSaatleri == null || !calismaSaatleri.contains(saat)) {
            throw new IllegalArgumentException("Secilen saat doktorun calisma saatleri icinde degil.");
        }
        if (aktifRandevuVarMi(doktorId, tarih, saat)) {
            throw new IllegalStateException("Doktor bu tarih ve saatte zaten aktif bir randevuya sahip.");
        }
        for (Randevu mevcutRandevu : randevular) {
            if (mevcutRandevu.getId() == randevuId) {
                throw new IllegalArgumentException("Bu randevu ID degeri zaten kullanilmis.");
            }
        }

        Randevu yeniRandevu = new Randevu(
                randevuId, hastaId, doktorId, tarih, saat, DURUM_AKTIF);
        randevular.add(yeniRandevu);
        return kopyalaRandevu(yeniRandevu);
    }

    public synchronized void randevuyuIptalEt(int randevuId) {
        for (Randevu randevu : randevular) {
            if (randevu.getId() == randevuId) {
                randevu.setDurum(DURUM_IPTAL);
                return;
            }
        }
        throw new IllegalArgumentException("Iptal edilecek randevu bulunamadi.");
    }

    public synchronized List<Randevu> randevulariGetir() {
        List<Randevu> randevuKopyalari = new ArrayList<>();
        for (Randevu randevu : randevular) {
            randevuKopyalari.add(kopyalaRandevu(randevu));
        }
        return Collections.unmodifiableList(randevuKopyalari);
    }

    public static RandevuKontrolServisi ornekServisOlustur() {
        Poliklinik dahiliye = new Poliklinik(
                1, "Dahiliye", "A Blok, 1. kat", 30);
        Poliklinik kardiyoloji = new Poliklinik(
                2, "Kardiyoloji", "B Blok, 2. kat", 24);

        Doktor dahiliyeDoktoru = new Doktor(
                101, "Ayse", "Yilmaz", "5550000101", "Dahiliye", dahiliye);
        Doktor kardiyolojiDoktoru = new Doktor(
                102, "Mehmet", "Demir", "5550000102", "Kardiyoloji", kardiyoloji);

        Map<Integer, List<LocalTime>> calismaSaatleri = new HashMap<>();
        calismaSaatleri.put(101, ornekSaatleri());
        calismaSaatleri.put(102, ornekSaatleri());

        LocalDate yarin = LocalDate.now().plusDays(1);
        List<Randevu> randevular = new ArrayList<>();
        randevular.add(new Randevu(
                1, 501, 101, yarin, LocalTime.of(9, 30), DURUM_AKTIF));
        randevular.add(new Randevu(
                2, 502, 101, yarin, LocalTime.of(10, 0), DURUM_IPTAL));
        randevular.add(new Randevu(
                3, 503, 102, yarin, LocalTime.of(9, 0), DURUM_AKTIF));
        randevular.add(new Randevu(
                4, 504, 102, yarin, LocalTime.of(9, 30), DURUM_IPTAL));

        List<Poliklinik> poliklinikler = new ArrayList<>();
        poliklinikler.add(dahiliye);
        poliklinikler.add(kardiyoloji);

        List<Doktor> doktorlar = new ArrayList<>();
        doktorlar.add(dahiliyeDoktoru);
        doktorlar.add(kardiyolojiDoktoru);

        return new RandevuKontrolServisi(
                poliklinikler, doktorlar, randevular, calismaSaatleri);
    }

    private void dogrulaDoktorVePoliklinik(int poliklinikId, int doktorId) {
        if (poliklinikId <= 0) {
            throw new IllegalArgumentException("Poliklinik secimi zorunludur.");
        }
        if (doktorId <= 0) {
            throw new IllegalArgumentException("Doktor secimi zorunludur.");
        }

        boolean poliklinikBulundu = false;
        for (Poliklinik poliklinik : poliklinikler) {
            if (poliklinik.getId() == poliklinikId) {
                poliklinikBulundu = true;
                break;
            }
        }
        if (!poliklinikBulundu) {
            throw new IllegalArgumentException("Secilen poliklinik bulunamadi.");
        }

        Doktor secilenDoktor = null;
        for (Doktor doktor : doktorlar) {
            if (doktor.getId() == doktorId) {
                secilenDoktor = doktor;
                break;
            }
        }
        if (secilenDoktor == null) {
            throw new IllegalArgumentException("Secilen doktor bulunamadi.");
        }
        if (secilenDoktor.getPoliklinik() == null
                || secilenDoktor.getPoliklinik().getId() != poliklinikId) {
            throw new IllegalArgumentException(
                    "Secilen doktor bu poliklinige bagli degil.");
        }
    }

    private boolean aktifRandevuVarMi(int doktorId, LocalDate tarih, LocalTime saat) {
        for (Randevu randevu : randevular) {
            if (randevu.getDoktorId() == doktorId
                    && tarih.equals(randevu.getTarih())
                    && saat.equals(randevu.getSaat())
                    && !iptalEdilmisMi(randevu.getDurum())) {
                return true;
            }
        }
        return false;
    }

    private boolean iptalEdilmisMi(String durum) {
        if (durum == null) {
            return false;
        }
        String temizDurum = durum.trim();
        return DURUM_IPTAL.equalsIgnoreCase(temizDurum)
                || "İptal edildi".equalsIgnoreCase(temizDurum)
                || "Iptal".equalsIgnoreCase(temizDurum)
                || "Iptal edildi".equalsIgnoreCase(temizDurum)
                || "cancelled".equalsIgnoreCase(temizDurum)
                || "canceled".equalsIgnoreCase(temizDurum);
    }

    private void dogrulaTarih(LocalDate tarih) {
        if (tarih == null) {
            throw new IllegalArgumentException("Randevu tarihi zorunludur.");
        }
        if (tarih.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Gecmis bir tarihe randevu verilemez.");
        }
    }

    private static List<LocalTime> ornekSaatleri() {
        List<LocalTime> saatler = new ArrayList<>();
        saatler.add(LocalTime.of(9, 0));
        saatler.add(LocalTime.of(9, 30));
        saatler.add(LocalTime.of(10, 0));
        saatler.add(LocalTime.of(10, 30));
        saatler.add(LocalTime.of(11, 0));
        saatler.add(LocalTime.of(13, 0));
        saatler.add(LocalTime.of(13, 30));
        saatler.add(LocalTime.of(14, 0));
        saatler.add(LocalTime.of(14, 30));
        saatler.add(LocalTime.of(15, 0));
        return saatler;
    }

    private static Randevu kopyalaRandevu(Randevu randevu) {
        return new Randevu(
                randevu.getId(),
                randevu.getHastaId(),
                randevu.getDoktorId(),
                randevu.getTarih(),
                randevu.getSaat(),
                randevu.getDurum());
    }
}
