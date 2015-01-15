package kontenery;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import main.ISAM;

/**
 * Opiekun struktury danych.
 * @author Eryk
 *
 */
public class StronaDanych {
	
	public static final Integer ILOSC_REKORDOW = 31;
	public static final Integer ROZMIAR_STRONY = 3*4*ILOSC_REKORDOW; //3(ilosc danych w rekordzie) * 4 * ilosc rekordow
	
	private Integer adresStrony;
	
	private List<Rekord> listaRekordow;
	
	private RandomAccessFile plik;
	private FileChannel kanal;
	private ByteBuffer bufor;
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	//																								  //
	//									UTWORZENIE NOWEJ STRONY										  //
	//																								  //
	////////////////////////////////////////////////////////////////////////////////////////////////////
		
	/**
	 * Po wywo�aniu tego konstruktora nale�y wywo�a� metod� utworzenia nowej strony
	 * Spowoduje to zainicjalizowanie wst�pnej struktury i stra�nika oraz przydzielenie stronie odpowiedniego adresu.
	 */
	
	public StronaDanych()
	{
		this.listaRekordow = new ArrayList();
	}
	
	/**
	 * wywo�anie tej metody tworzy now� stron�, BEZ ZAPISU DO PLIKU!
	 * @param adres
	 */
	public void utworzNowaStrone(Integer adres)
	{
		this.adresStrony = adres;
		
		this.listaRekordow.add(Rekord.zwrocRekordStraznika());
		
		for(Integer i = 1; i < ILOSC_REKORDOW; i++)
			this.listaRekordow.add(Rekord.zwrocPustyRekord());
		
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	//																								  //
	//								POBRANIE ISTNIEJ�CEJ STRONY										  //
	//																								  //
	////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Strona inicjowana numerem indeksu, sama si� pobiera, przeprowadza w sobie wyszukiwanie elementu po jakim� kluczu,
	 * zajmuje si� dodawaniem, edycj� i usuwaniem element�w w sobie. 
	 * @param adres
	 */
	public StronaDanych(Integer adres)
	{
		this.adresStrony = adres;
		this.listaRekordow = new ArrayList();
		pobierzStrone();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	//																								  //
	//										SZUKANIE PO KLUCZU										  //
	//																								  //
	////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public Rekord szukajKluczaWStronie(Integer klucz)
	{
		for(Integer i = listaRekordow.size() - 1; i >= 0; i--)
		{
			Rekord rekord = listaRekordow.get(i);
			if(rekord.getKlucz() == Rekord.KLUCZ_REKORD_PUSTY)
				continue;
			
			if(rekord.getKlucz().equals(klucz) && !rekord.getDane().equals(Rekord.WARTOSC_REKORD_DO_USUNIECIA))
			{
				rekord.setStronaZawierajaca(this);
				return rekord;
			}
			
			if(rekord.getKlucz() < klucz) 
				if(rekord.getWskaznik() != Rekord.BRAK_WSKAZNIKA) //pierwsze zej�cie poni�ej i nie jest r�wne, tutaj mo�e by� m�j klucz
				{
					StronaDanych stronaOverflow = new StronaDanych(rekord.getWskaznik());
					Rekord rekordOverflow = stronaOverflow.zwrocPierwszyRekord();
					Rekord tmp = null;
					while(rekordOverflow.getKlucz() < klucz
							&& !rekordOverflow.getWskaznik().equals(Rekord.BRAK_WSKAZNIKA))
					{
						tmp = stronaOverflow.wyszukajRekordOAdresie(rekordOverflow.getWskaznik());
						if(tmp == null)
						{
							stronaOverflow = new StronaDanych(rekordOverflow.getWskaznik());
							rekordOverflow = stronaOverflow.zwrocPierwszyRekord();
						}
						else
							rekordOverflow = tmp;
					}
					
					if(rekordOverflow.getKlucz().equals(klucz) && !rekordOverflow.getDane().equals(Rekord.WARTOSC_REKORD_DO_USUNIECIA))
					{
						rekordOverflow.setStronaZawierajaca(stronaOverflow);
						return rekordOverflow;
					}
				}
				else
					break;
			}
	return null;
	}
	
	/**
	 * Zwraca rekord o danym adresie ktory jest obecny w tej stronie lub null, je�eli rekord nie jest na tej stronie.
	 * @param adresRekordu
	 * @return
	 */
	public Rekord wyszukajRekordOAdresie(Integer adresRekordu)
	{
		if(adresRekordu >= adresStrony + ROZMIAR_STRONY || adresRekordu < adresStrony)
			return null;
		
		Integer adresWzgledemPoczatkuStrony = adresRekordu - adresStrony;
		Integer indeks = adresWzgledemPoczatkuStrony /(4*3);
		
		return listaRekordow.get(indeks);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	//																								  //
	//										DODAWANIE REKORDU										  //
	//																								  //
	////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public Boolean dodajRekord(Rekord rekord)
	{
		//Wyszukiwanie miejsca do wstawienia.
		Integer indeks = listaRekordow.size() - 1;
		for(; indeks > 0; indeks--)
		{
			Rekord tmp = listaRekordow.get(indeks);
			
			if(tmp.czyRekordPusty()) //Omijam rekordy puste
 				continue;
			
			if(tmp.getKlucz() == rekord.getKlucz())
			{	
				if(tmp.getDane() != Rekord.WARTOSC_REKORD_DO_USUNIECIA)
					return false;
				else
				{
					tmp.setDane(rekord.getDane());
					this.zapiszStrone();
					return true;
				}
			}
			
			if(tmp.getKlucz() < rekord.getKlucz()) //Trafienie, musz� wstawi� co� za obiektem o tym kluczu
				break;
		}
		indeks++;//po petli indeks spada o jedno miejsce za duzo.
		
		//Wstawienie rekordu w znalezione miejsce.
		if(indeks < listaRekordow.size() && listaRekordow.get(indeks).czyRekordPusty())
		{
			listaRekordow.set(indeks, rekord);
			zapiszStrone();
			return true;
		}
		else //Wpisywanie w overflow
		{
			//Musz� pami�ta� aktualny rekord kt�ry sprawdzam (w przypadku ko�ca �a�cucha) i poprzedni, w przypadku gdy znajd� miejsce pomi�dzy poprzednim i bierz�cym 
			Rekord poprzedniSprawdzanyRekord = null;
			StronaDanych poprzedniaStrona = null;
			Rekord bierzacySprawdzanyRekord = listaRekordow.get(indeks - 1);
			StronaDanych bierzacaStrona = this;
			
			//Petla kreci sie dop�ki nie znajdzie rekordu bez wska�nika (koniec �a�cucha) lub rekordu o wi�kszym lub r�wnym kluczu
			while(bierzacySprawdzanyRekord.getWskaznik() != Rekord.BRAK_WSKAZNIKA && bierzacySprawdzanyRekord.getKlucz() < rekord.getKlucz())
			{
				poprzedniSprawdzanyRekord = bierzacySprawdzanyRekord;
				bierzacySprawdzanyRekord = bierzacaStrona.wyszukajRekordOAdresie(poprzedniSprawdzanyRekord.getWskaznik());
				if(bierzacySprawdzanyRekord == null)
				{
					poprzedniaStrona = bierzacaStrona;
					bierzacaStrona = new StronaDanych(poprzedniSprawdzanyRekord.getWskaznik());
					bierzacySprawdzanyRekord = bierzacaStrona.zwrocPierwszyRekord();
				}
			}
			//Wyj�cie z p�tli oznacza trafienie lub koniec wycieczki
			//Albo trafi�em na bierz�cy element z r�wnym kluczem
			if(bierzacySprawdzanyRekord.getKlucz() == rekord.getKlucz())
				return false;
			else //Albo trafi�em na koniec �a�cucha z warto�ci� mniejsz� od mojego klucza
				if(bierzacySprawdzanyRekord.getWskaznik() == Rekord.BRAK_WSKAZNIKA && bierzacySprawdzanyRekord.getKlucz() < rekord.getKlucz())
				{
					bierzacySprawdzanyRekord.setWskaznik(zapiszRekordNaKoncuPliku(rekord));
					bierzacaStrona.zapiszStrone();
				}
				else //Albo trafi�em w �rodek, i musz� wczepi� m�j rekord mi�dzy te dw�jk�.
				{
					rekord.setWskaznik(poprzedniSprawdzanyRekord.getWskaznik());
					poprzedniSprawdzanyRekord.setWskaznik(zapiszRekordNaKoncuPliku(rekord));
					poprzedniaStrona.zapiszStrone();
				}
		}
		return true;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	//																								  //
	//											INNY STUFF											  //
	//																								  //
	////////////////////////////////////////////////////////////////////////////////////////////////////
	
	private Integer zapiszRekordNaKoncuPliku(Rekord rekord)
	{
		otworzPlik();
		ISAM.iloscZapisow++;
		Integer pozycjaRekordu = null;
		try {
			pozycjaRekordu = new Long(plik.length()).intValue();
			kanal.position(plik.length());
		} catch (IOException e) {
			e.printStackTrace();
		}

		bufor.putInt(rekord.getKlucz());
		bufor.putInt(rekord.getDane());
		bufor.putInt(rekord.getWskaznik());
		try
		{
			bufor.flip();
			kanal.write(bufor);
			bufor.clear();
			ISAM.rozmiarPlikuDanych = new Long(plik.length()).intValue();
		}
		catch(IOException e)
		{
			bufor.clear();
			e.printStackTrace();
		}
		zamknijPlik();
		
		return pozycjaRekordu;
	}
	
	private void pobierzStrone()
	{
		otworzPlik();
		try {
			if(kanal.read(bufor) > 0)
			{
				ISAM.iloscOdczytow++;
				bufor.flip();
				listaRekordow.clear();
				Integer klucz, dana, wskaznik;
				
				for (int i = 0; i < bufor.limit(); i+=12)
	            {
	            	klucz = bufor.getInt();
	            	dana = bufor.getInt();
	            	wskaznik = bufor.getInt();
	            	listaRekordow.add(new Rekord(klucz, dana, wskaznik));
	            }
	            bufor.clear();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		zamknijPlik();	
	}
	
	public void zapiszStrone()
	{
		otworzPlik();
		ISAM.iloscZapisow++;
		for(Rekord rekord : listaRekordow)
		{	
			bufor.putInt(rekord.getKlucz());
			bufor.putInt(rekord.getDane());
			bufor.putInt(rekord.getWskaznik());
		}
		
		try
		{
			bufor.flip();
			kanal.write(bufor);
			bufor.clear();
		}
		catch(IOException e)
		{
			bufor.clear();
			e.printStackTrace();
		}
		zamknijPlik();
	}
	
	private Boolean otworzPlik()
	{
		try {
			plik = new RandomAccessFile(ISAM.PLIK_DANYCH, "rw");
			kanal = plik.getChannel();
	        bufor = ByteBuffer.allocate(ROZMIAR_STRONY);
	        
			kanal.position(adresStrony);
		
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private Boolean zamknijPlik()
	{
		try {
			bufor.clear();
			plik.close(); //to zamyka tez kanal.
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public Integer getAdresStrony() {
		return adresStrony;
	}

	public void setAdresStrony(Integer adresStrony) {
		this.adresStrony = adresStrony;
	}

	public List<Rekord> getListaRekordow() {
		return listaRekordow;
	}

	public void setListaRekordow(List<Rekord> listaRekordow) {
		this.listaRekordow = listaRekordow;
	}
	
	public Rekord zwrocPierwszyRekord()
	{
		return listaRekordow.get(0);
	}
}