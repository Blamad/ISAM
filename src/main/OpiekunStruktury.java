package main;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import kontenery.Indeks;
import kontenery.NadzorIndeksow;
import kontenery.Rekord;
import kontenery.StronaDanych;

/**
 * Klasa zajmuje sie nadzorem operacji
 * @author Eryk
 *
 */
public class OpiekunStruktury {
	
	public Integer POCZATEK_SEKCJI_OVERFLOW = 0;
	
	private NadzorIndeksow indeksowanie = new NadzorIndeksow();
	private StronaDanych strona = null;
	
	private static final Boolean WYPISUJ_PUSTE = false;
	public Boolean testy = false;
	
	public void dodajRekord(Integer klucz, Integer wartosc)
	{
		Integer adresStrony = indeksowanie.znajdzStroneDlaKlucza(klucz);
		if(strona == null || !strona.getAdresStrony().equals(adresStrony))
			strona = new StronaDanych(adresStrony);
		
		Rekord rekord = new Rekord(klucz, wartosc, Rekord.BRAK_WSKAZNIKA);
		
		if(!strona.dodajRekord(rekord))
		{
			if(!testy)
				System.out.println("Istnieje ju¿ rekord o takim kluczu. ABORT.");
		}
		else
			if(!testy)
				System.out.println("Dodano rekord.");
		sprawdzCzyWymagaReorganizacji();
	}
	
	public void usunRekord(Integer klucz)
	{
		Integer adresStrony = indeksowanie.znajdzStroneDlaKlucza(klucz);
		if(strona == null || !strona.getAdresStrony().equals(adresStrony))
			strona = new StronaDanych(adresStrony);
		Rekord rekord = strona.szukajKluczaWStronie(klucz);
		if(rekord == null)
		{
			if(!testy)
				System.out.println("Nie znaleziono rekordu o podanym id.");
		}
		else
		{
			if(!testy)
				System.out.println("Rekord o wartoœci " + rekord.getDane() + " zosta³ usuniêty.");
			rekord.setDane(Rekord.WARTOSC_REKORD_DO_USUNIECIA);
			rekord.getStronaZawierajaca().zapiszStrone();
		}
	}
	
	public void edytujRekord(Integer klucz, Integer wartosc)
	{
		Integer adresStrony = indeksowanie.znajdzStroneDlaKlucza(klucz);
		
		if(strona == null || !strona.getAdresStrony().equals(adresStrony))
			strona = new StronaDanych(adresStrony);
		
		Rekord rekord = strona.szukajKluczaWStronie(klucz);
		if(rekord == null)
		{
			if(!testy)
				System.out.println("Nie znaleziono rekordu o podanym id.");
		}
		else
			if(rekord.getDane() != wartosc)
			{
				if(!testy)
					System.out.println("Zmieniono wartoœæ z " + rekord.getDane() + " na " + wartosc);
				rekord.setDane(wartosc);
				rekord.getStronaZawierajaca().zapiszStrone();
			}
			else
				if(!testy)
					System.out.println("Wartoœæ podawana i ju¿ isteniej¹ca sa równe.");
	}
	
	public void znajdzRekord(Integer klucz)
	{
		Integer adresStrony = indeksowanie.znajdzStroneDlaKlucza(klucz);
		
		if(strona == null || !strona.getAdresStrony().equals(adresStrony))
			strona = new StronaDanych(adresStrony);
		
		Rekord rekord = strona.szukajKluczaWStronie(klucz);
		if(rekord == null)
		{
			if(!testy)
				System.out.println("Nie znaleziono rekordu o podanym id.");
		}
		else
			if(!testy)
				System.out.println("Znaleziono rekord o kluczu: " + rekord.getKlucz() + " i wartoœci: " + rekord.getDane());
	}
	
	public void reorganizujStrukture()
	{
		reorganizujDane();
		if(!testy)
			System.out.println("Reorganizacja zakoñczona.");
	}

	private void sprawdzCzyWymagaReorganizacji()
	{
		Double rozmiarPrimary = new Double(ISAM.iloscStron*StronaDanych.ROZMIAR_STRONY);
		Double rozmiarOverflow = new Double(ISAM.rozmiarPlikuDanych - rozmiarPrimary);
		
		if(rozmiarOverflow/rozmiarPrimary >= 0.15)
		{
			//System.out.println(rozmiarOverflow+" / " + rozmiarPrimary + " = " + rozmiarOverflow/rozmiarPrimary);
			reorganizujDane();
		}
	}
	
	private void reorganizujDane()
	{
		//Przygotowanie plików
		try {
			//Zmiana nazwy starego pliku
			File dane = new File(ISAM.PLIK_DANYCH);
			File daneStary = new File(ISAM.PLIK_DANYCH+"_OLD");
			dane.renameTo(daneStary);
			
			File noweDane = new File(ISAM.PLIK_DANYCH);
			noweDane.createNewFile();
			
			Integer iloscStron = ISAM.iloscStron;
			
			czyscPliki();
			indeksowanie = new NadzorIndeksow();
			strona = null;
			
			RandomAccessFile daneReorganizowane = new RandomAccessFile(daneStary.getAbsolutePath(), "rw");
			FileChannel kanal = daneReorganizowane.getChannel();
			ByteBuffer bufor = ByteBuffer.allocate(StronaDanych.ROZMIAR_STRONY);
			
			FileChannel kanalOverflow = daneReorganizowane.getChannel();
			ByteBuffer buforOverflow = ByteBuffer.allocate(StronaDanych.ROZMIAR_STRONY);
			
			Integer wskaznikStronyOverflow = 0;
			
			for(Integer numerStrony = 0; numerStrony < iloscStron; numerStrony++)
			{
				kanal.position(numerStrony * StronaDanych.ROZMIAR_STRONY);
				
				if(kanal.read(bufor) > 0)
				{
					ISAM.iloscOdczytow++;
					bufor.flip();
					Integer klucz, dana, wskaznik;
					
					for (int i = 0; i < bufor.limit(); i+=12)
		            {
		            	klucz = bufor.getInt();
		            	dana = bufor.getInt();
		            	wskaznik = bufor.getInt();
		            	Rekord rekord = new Rekord(klucz, dana, Rekord.BRAK_WSKAZNIKA);
		            	if(rekord.czyRekordDoZapisu())
		            		dodajElementDoStrony(rekord);
		            	//Mamy Overflowowe wartoœci do sp³aszczenia
		            	
		            	while(wskaznik != Rekord.BRAK_WSKAZNIKA)
		            	{
		            		if(wskaznik >= wskaznikStronyOverflow + StronaDanych.ROZMIAR_STRONY || wskaznik < wskaznikStronyOverflow)
		            		{
		            			wskaznikStronyOverflow = wskaznik;
		            			kanalOverflow.position(wskaznikStronyOverflow);
		            			buforOverflow.clear();
		            			kanalOverflow.read(buforOverflow);
		            			buforOverflow.flip();
		            			ISAM.iloscOdczytow++;
		            		}
		            		else
		            		{
		            			buforOverflow.position(wskaznik - wskaznikStronyOverflow);
		            		}
	            			klucz = buforOverflow.getInt();
			            	dana = buforOverflow.getInt();
			            	wskaznik = buforOverflow.getInt();

	            			Rekord rekordOverflow = new Rekord(klucz, dana, Rekord.BRAK_WSKAZNIKA);
	            			if(rekordOverflow.czyRekordDoZapisu())
	            				dodajElementDoStrony(rekordOverflow);
		            	}
		            }
		            bufor.clear();
				}
			}
			
			if(adresBierzacejStrony == 0 && listaRekordow.isEmpty())
			{
				inicjujStrukture();
			}
			else
				zapiszResztki();
			
			ISAM.rozmiarPlikuDanych = 0;
			
			daneReorganizowane.close();
			daneStary.delete();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private Integer adresBierzacejStrony = 0;
	private List<Rekord> listaRekordow = new ArrayList();
	private void dodajElementDoStrony(Rekord rekord)
	{
		listaRekordow.add(rekord);
		if(listaRekordow.size() == StronaDanych.ILOSC_REKORDOW / 2)
		{
			StronaDanych strona = new StronaDanych();
			strona.utworzNowaStrone(adresBierzacejStrony);
			for(int i = 0; i < listaRekordow.size(); i++)
				strona.getListaRekordow().set(i+1, listaRekordow.get(i));
			
			strona.zapiszStrone();
			indeksowanie.dodajIndeks(new Indeks(listaRekordow.get(0).getKlucz(), adresBierzacejStrony));
			listaRekordow.clear();
			adresBierzacejStrony += StronaDanych.ROZMIAR_STRONY;
		}
	}
	
	private void zapiszResztki()
	{
		if(!listaRekordow.isEmpty())
		{
			StronaDanych strona = new StronaDanych();
			strona.utworzNowaStrone(adresBierzacejStrony);
			for(int i = 0; i < listaRekordow.size(); i++)
				strona.getListaRekordow().set(i+1, listaRekordow.get(i));
			
			strona.zapiszStrone();
			indeksowanie.dodajIndeks(new Indeks(listaRekordow.get(0).getKlucz(), adresBierzacejStrony));
			listaRekordow.clear();
		}
		
		adresBierzacejStrony = 0;
		indeksowanie.zapiszStrone();
		
	}
	
	public void inicjujStrukture()
	{	
		indeksowanie.inicjujIndeks();
		
		strona = new StronaDanych();
		strona.utworzNowaStrone(0);
		strona.zapiszStrone();
	}
	
	public void wypiszZawartoscPliku(String nazwaPliku)
	{
		RandomAccessFile plik = null;
		try
		{
			plik = new RandomAccessFile(nazwaPliku, "rw");
			FileChannel kanal = plik.getChannel();
			ByteBuffer bufor = ByteBuffer.allocate(1000*4*3);
			
			Integer licznik = 0;
			
			if(nazwaPliku.equals(ISAM.PLIK_DANYCH))
				while(kanal.read(bufor) > 0)
				{
					bufor.flip();
					Integer klucz, dana, wskaznik;
					
					for (int i = 0; i < bufor.limit(); i+=12)
		            {
						licznik++;
		            	klucz = bufor.getInt();
		            	dana = bufor.getInt();
		            	wskaznik = bufor.getInt();
		            	if(!WYPISUJ_PUSTE && klucz == Rekord.KLUCZ_REKORD_PUSTY)
		            		continue;
		            	System.out.println(licznik + " k: " + klucz + " d: " + dana + " p: " + wskaznik);
		            }
		            bufor.clear();
				}
			else
				while(kanal.read(bufor) > 0)
				{
					bufor.flip();
					Integer klucz, adres;
					
					for (int i = 0; i < bufor.limit(); i+=8)
		            {
						licznik++;
		            	klucz = bufor.getInt();
		            	adres = bufor.getInt();
		            	System.out.println(licznik +" k: "+klucz + " p: " + adres);
		            }
		            bufor.clear();
				}
			
			plik.close(); //to zamyka tez kanal.
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void czyscPliki()
	{
		try {
			RandomAccessFile plikDanych = new RandomAccessFile(ISAM.PLIK_DANYCH, "rw");
			RandomAccessFile plikIndeksow = new RandomAccessFile(ISAM.PLIK_INDEKSOW, "rw");
			
			plikDanych.setLength(0l);
			plikIndeksow.setLength(0l);
			
			plikDanych.close();
			plikIndeksow.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
