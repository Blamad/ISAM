package kontenery;


/**
 * Klasa reprezentuje pojedynczy rekord w strukturze ISAM w kolejnoœci: klucz -> dane -> wskaŸnik overflow
 * @author Eryk
 *
 */
public class Rekord {
	
	public static final Integer KLUCZ_REKORD_PUSTY = -1;
	public static final Integer KLUCZ_REKORD_STRAZNIK = 0;
	public static final Integer WARTOSC_REKORD_DO_USUNIECIA = -2;
	public static final Integer BRAK_WSKAZNIKA = -1;
	
	private Integer klucz,
					dane,
					wskaznik;
	
	private StronaDanych stronaZawierajaca;
	
	public Rekord(Integer klucz, Integer dane, Integer wskaznik)
	{
		this.klucz = klucz;
		this.dane = dane;
		this.wskaznik = wskaznik;
		
	}

	public Boolean czyRekordPusty()
	{
		if(klucz == Rekord.KLUCZ_REKORD_PUSTY)
			return true;
		return false;
	}
	
	public Boolean czyRekordDoZapisu()
	{
		if(dane >= 0 && klucz > Rekord.KLUCZ_REKORD_STRAZNIK)
			return true;
		return false;
	}
	
	public static Rekord zwrocPustyRekord()
	{
		return new Rekord(Rekord.KLUCZ_REKORD_PUSTY, -1, BRAK_WSKAZNIKA);
	}
	
	public static Rekord zwrocRekordStraznika()
	{
		return new Rekord(0, -1, BRAK_WSKAZNIKA);
	}
	
	public Integer getKlucz() {
		return klucz;
	}

	public void setKlucz(Integer klucz) {
		this.klucz = klucz;
	}

	public Integer getDane() {
		return dane;
	}

	public void setDane(Integer dane) {
		this.dane = dane;
	}

	public Integer getWskaznik() {
		return wskaznik;
	}

	public void setWskaznik(Integer wskaznik) {
		this.wskaznik = wskaznik;
	}

	public StronaDanych getStronaZawierajaca() {
		return stronaZawierajaca;
	}

	public void setStronaZawierajaca(StronaDanych stronaZawierajaca) {
		this.stronaZawierajaca = stronaZawierajaca;
	}

}