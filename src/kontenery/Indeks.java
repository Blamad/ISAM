package kontenery;

public class Indeks {

	private Integer klucz,
					adresStrony;
	
	public Indeks(Integer klucz, Integer numerStrony)
	{
		this.klucz = klucz;
		this.adresStrony = numerStrony;
	}

	public Integer getKlucz() {
		return klucz;
	}

	public void setKlucz(Integer klucz) {
		this.klucz = klucz;
	}

	public Integer getAdresStrony() {
		return adresStrony;
	}

	public void setAdresStrony(Integer adresStrony) {
		this.adresStrony = adresStrony;
	}
	
}
