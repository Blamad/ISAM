package main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

public class ISAM {

	public static final String PLIK_DANYCH = "pliki\\dane";
	public static final String PLIK_INDEKSOW = "pliki\\indeksy";
	
	public static Integer	iloscOdczytow = 0, 
							iloscZapisow = 0,
							iloscStron = 0,
							rozmiarPlikuDanych = 0,
							iloscTestow = 0;
	
	private Boolean leciZPliku = false;
	
	private OpiekunStruktury opiekun = new OpiekunStruktury();
	
	private BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
	
	private Double pi = 3.14;
	
	public ISAM()
	{
		opiekun.czyscPliki();
		opiekun.inicjujStrukture();
		interfejs();
	}
	
	private void test2()
	{
		opiekun.testy = true;
		Random agnieszka = new Random();
		for(Integer i = 1; i <= iloscTestow; i++)
		{	
			Integer operacja = agnieszka.nextInt(100);
			Integer in = agnieszka.nextInt(iloscTestow)+1;
			if(operacja < 50)
				opiekun.dodajRekord(in, i);
			else
				if(operacja < 75)
					opiekun.edytujRekord(in, i*2);
				else
					opiekun.usunRekord(in);
		}
		System.out.println("Dodawanie\nIlo�� odczyt�w: " + new Double(ISAM.iloscOdczytow)/new Double(iloscTestow) + " ilos� zapis�w: " + new Double(ISAM.iloscZapisow)/new Double(iloscTestow));
		
		ISAM.iloscOdczytow = 0;
		ISAM.iloscZapisow = 0;
		opiekun.testy = false;
	}
	
	private void test()
	{
		opiekun.testy = true;
		Random agnieszka = new Random();
		for(Integer i = 1; i <= iloscTestow; i++)
		{	
			Integer in = agnieszka.nextInt(iloscTestow)+1;
			opiekun.dodajRekord(in, i);
		}
		System.out.println("Dodawanie\nIlo�� odczyt�w: " + new Double(ISAM.iloscOdczytow)/new Double(iloscTestow) + " ilos� zapis�w: " + new Double(ISAM.iloscZapisow)/new Double(iloscTestow));
		
		ISAM.iloscOdczytow = 0;
		ISAM.iloscZapisow = 0;
		
		for(Integer i = 1; i <= iloscTestow; i++)
			opiekun.edytujRekord(agnieszka.nextInt(iloscTestow)+1, i*2);
		System.out.println("Edycja\nIlo�� odczyt�w: " + new Double(ISAM.iloscOdczytow)/new Double(iloscTestow) + " ilos� zapis�w: " + new Double(ISAM.iloscZapisow)/new Double(iloscTestow));
		
		/*ISAM.iloscOdczytow = 0;
		ISAM.iloscZapisow = 0;
		
		for(Integer i = 1; i <= iloscTestow; i++)
			opiekun.usunRekord(i*2);
		System.out.println("Usuwanie\nIlo�� odczyt�w: " + new Double(ISAM.iloscOdczytow)/new Double(iloscTestow) + " ilos� zapis�w: " + new Double(ISAM.iloscZapisow)/new Double(iloscTestow));
		*/
		ISAM.iloscOdczytow = 0;
		ISAM.iloscZapisow = 0;
		opiekun.testy = false;
	}
	
	private void podajStatus()
	{
		System.out.println("Ilo�� odczyt�w: " + ISAM.iloscOdczytow + " ilos� zapis�w: " + ISAM.iloscZapisow);
	}
	
	public void interfejs()
	{
		System.out.println("        Eryk Sobczak, 137406");
		System.out.println("Struktury baz danych: Projekt drugi");
		System.out.println("            ISAM\n");
		System.out.println("Zatwierdzenie wiersza uruchamia dana akcje.");
		System.out.println("+ [klucz] [promie�] [wysoko��] : dodanie rekordu o podanej warto�ci i kluczu");
		System.out.println("e [klucz] [promie�] [wysoko��] : edycja rekordu o podanym kluczu do nowej warto�ci");
		System.out.println("s [klucz] : wyszukiwanie podanego klucza");
		System.out.println("- [klucz] : usuni�cie podanego klucza");
		System.out.println("d : wy�wietlenie zawarto�ci pliku danych");
		System.out.println("i : wy�wietlenie zawarto�ci pliku indeks�w");
		System.out.println("r : reorganizuj struktur� pliku");
		System.out.println("x : wy�wietlenie ilo�ci dost�p�w do plik�w");
		System.out.println("f [opcjonalnie �cie�ka] - wykonanie komend zawartych w pliku tekstowym");
		System.out.println("q : zako�czenie programu;");
		
		for(;;)
		{	
			String args = "";
			try {
				args = reader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			} //pozyskaj arguementy
			
			String[] argumenty = args.split(" ");
			Integer klucz, promien, wysokosc, wartosc;
			
			if(args.length() < 1)
				continue;
			
			switch(argumenty[0])
			{
			case "+":
				try
				{
					klucz = Integer.parseInt(argumenty[1]);
					promien = Integer.parseInt(argumenty[2]);
					wysokosc = Integer.parseInt(argumenty[3]);
					wartosc = new Double(pi * new Double(promien * promien * wysokosc)).intValue();
				}
				catch(NumberFormatException e)
				{
					e.printStackTrace();
					continue;
				}
				opiekun.dodajRekord(klucz, wartosc);
				break;
			case "e":
				try
				{
					klucz = Integer.parseInt(argumenty[1]);
					promien = Integer.parseInt(argumenty[2]);
					wysokosc = Integer.parseInt(argumenty[3]);
					wartosc = new Double(pi * new Double(promien * promien * wysokosc)).intValue();
				}
				catch(NumberFormatException e)
				{
					e.printStackTrace();
					continue;
				}
				opiekun.edytujRekord(klucz, wartosc);
				break;
			case "-":
				try
				{
					klucz = Integer.parseInt(argumenty[1]);
				}
				catch(NumberFormatException e)
				{
					e.printStackTrace();
					continue;
				}
				opiekun.usunRekord(klucz);
				break;				
			case "s":
				try
				{
					klucz = Integer.parseInt(argumenty[1]);
				}
				catch(NumberFormatException e)
				{
					e.printStackTrace();
					continue;
				}
				opiekun.znajdzRekord(klucz);
				break;
			case "d":
				opiekun.wypiszZawartoscPliku(PLIK_DANYCH);
				break;
			case "i":
				opiekun.wypiszZawartoscPliku(PLIK_INDEKSOW);
				break;
			case "r":
				opiekun.reorganizujStrukture();
				break;
			case "x":
				podajStatus();
				break;
			case "t":
				try
				{
					iloscTestow = Integer.parseInt(argumenty[1]);
				}
				catch(NumberFormatException e)
				{
					e.printStackTrace();
					continue;
				}
				test2();
				break;
			case "f":
				if(!leciZPliku)
				{
					String sciezka = "pliki\\test";
					if(argumenty.length != 1)
						sciezka = argumenty[1];
					try {
						reader = new BufferedReader(new FileReader(sciezka));
						leciZPliku = true;
						System.out.println("Przechodze na sterowanie z pliku..");
					} catch (FileNotFoundException e) {
						System.out.println("Nie znaleziono pliku o �cie�ce " + sciezka);
					}
				}
				break;
			case "q":
				if(leciZPliku)
				{
					reader = new BufferedReader(new InputStreamReader(System.in));
					leciZPliku = false;
					System.out.println("Przechodze na sterowanie z konsoli..");
				}
				else
					return;
				break;
			default:
				System.out.println("Nie rozpoznano polecenia.");
				break;
			}
		}
	}
}
