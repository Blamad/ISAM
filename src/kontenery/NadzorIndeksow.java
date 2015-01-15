package kontenery;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import main.ISAM;


/**
 * klasa zajmuj¹ca siê wyszukiwaniem indeksów. Pobiera sobie w œrodku jak¹œ czêœæ pliku indeksowanego (w³asny rozmiar strony)
 * i w nim szuka po prostu indeksu. Umo¿liwia te¿ edycjê indeksów (dodawanie, usuwanie, zmiany).
 * @author Eryk
 *
 */
public class NadzorIndeksow {

	private static final Integer ILOSC_INDEKSOW_NA_STRONE = 10;
	private static final Integer ROZMIAR_BUFORA = 2 * 4 * ILOSC_INDEKSOW_NA_STRONE; //2 dane * 4 bajty (integer) * ilosc indeksow
	
	private List<Indeks> listaIndeksow = new ArrayList();
	
	private Integer numerStronyIndeksow = 0;
	
	private RandomAccessFile plik;
	private FileChannel kanal;
	private ByteBuffer bufor;
	
	public NadzorIndeksow()
	{
		ISAM.iloscStron = 0;
	}
	
	public void inicjujIndeks()
	{
		listaIndeksow.add(new Indeks(0, 0));
		zapiszStrone();
		ISAM.iloscStron = 1;
		numerStronyIndeksow = 0;
	}
	
	public Integer znajdzStroneDlaKlucza(Integer klucz)
	{
		Integer adresStronyIndeksow = 0;
		Integer adresStronyDanych = 0;
		
		//Zasada lokalnoœci
		if(!listaIndeksow.isEmpty() && listaIndeksow.get(listaIndeksow.size()-1).getKlucz() <= klucz)
		{
			for(Indeks indeks : listaIndeksow)
			{
				if(indeks.getKlucz() > klucz)
					return adresStronyDanych;
				adresStronyDanych = indeks.getAdresStrony();
			}
			/*
			if(listaIndeksow.get(0).getKlucz() < klucz && numerStronyIndeksow.equals(0))
				return listaIndeksow.get(0).getAdresStrony();
				*/
		}
		
		adresStronyDanych = 0;
		
		while(pobierzStroneIndeksow(adresStronyIndeksow))
		{
			//Warunek brzegowy: jezeli pierwszy klucz nowej strony jest wiekszy ni¿ nasz, to nasz klucz nale¿y jeszcze do ostatniego indeksu poprzedniej strony.
			if(listaIndeksow.get(0).getKlucz() > klucz)
				return adresStronyDanych;
			
			for(Indeks indeks : listaIndeksow)
			{
				if(indeks.getKlucz() > klucz)
					return adresStronyDanych;
				adresStronyDanych = indeks.getAdresStrony();
			}
			
			adresStronyIndeksow += ROZMIAR_BUFORA;
		}
		return adresStronyDanych;
	}
	
	private Boolean pobierzStroneIndeksow(Integer adresStrony)
	{
		otworzPlik(adresStrony);
		Integer tmpNumerStronyIndeksow = adresStrony/ROZMIAR_BUFORA;
		if(tmpNumerStronyIndeksow >= ISAM.iloscStron/ILOSC_INDEKSOW_NA_STRONE+1)
			return false;
		numerStronyIndeksow = tmpNumerStronyIndeksow;
		ISAM.iloscOdczytow++;
		try {
			if(kanal.read(bufor) > 0)
			{
				bufor.flip();
				listaIndeksow.clear();
				Integer klucz, numer;
				
				for (int i = 0; i < bufor.limit(); i+=8)
	            {
	            	klucz = bufor.getInt();
	            	numer = bufor.getInt();
	            	listaIndeksow.add(new Indeks(klucz, numer));
	            }
	            bufor.clear();
			}
			else
				return false;
		} catch (IOException e) {
			e.printStackTrace();
			zamknijPlik();
			return false;
		}
		
		zamknijPlik();
		return true;
	}
	
	public void zapiszStrone()
	{
		otworzPlik(null);
		try {
			kanal.position(plik.length());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		ISAM.iloscZapisow++;
		
		for(Indeks indeks : listaIndeksow)
		{	
			bufor.putInt(indeks.getKlucz());
			bufor.putInt(indeks.getAdresStrony());
		}
		
		try
		{
			bufor.flip();
			kanal.write(bufor);
			bufor.clear();
			ISAM.iloscStron = new Long(plik.length()).intValue() / 8;
		}
		catch(IOException e)
		{
			bufor.clear();
			e.printStackTrace();
		}
			listaIndeksow.clear();
		
		zamknijPlik();
	}
	
	private Boolean otworzPlik(Integer adresStrony)
	{
		try {
			plik = new RandomAccessFile(ISAM.PLIK_INDEKSOW, "rw");
			kanal = plik.getChannel();
	        bufor = ByteBuffer.allocate(ROZMIAR_BUFORA);
	        
	        if(adresStrony != null)
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

	public void dodajIndeks(Indeks indeks)
	{
		this.listaIndeksow.add(indeks);
		if(listaIndeksow.size() == ILOSC_INDEKSOW_NA_STRONE)
			this.zapiszStrone();
	}
}