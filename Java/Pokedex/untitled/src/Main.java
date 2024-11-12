import javax.swing.*;
import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws MalformedURLException {

        PokedexDB database = new PokedexDB();
        PokedexAPI api = new PokedexAPI();
        GiphyAPI giphy = new GiphyAPI();

        int selezione = -1;
        Scanner sc = new Scanner(System.in);

        while(true){
            System.out.println("\n1. Visualizza tutti i pokemon");
            System.out.println("2. Cerca un pokemon");
            System.out.println("3. Esci");
            System.out.print("Inserisci un numero: ");


            try {
                selezione = sc.nextInt();
                sc.nextLine();
            } catch (Exception e) {
                selezione = -1;
                sc.nextLine();
            }

            switch(selezione){
                case 1:
                    database.stampa();
                break;
                case 2:
                    String nome = "";
                    while(nome.isEmpty()) {
                        System.out.print("\nInserisci un nome: ");
                        nome = sc.nextLine().trim().toLowerCase();
                    }

                    Pokemon p = api.getPokemon(nome);
                    if(p == null) {
                        System.out.println("\nIl pokemon " + nome + " non esiste");
                        break;
                    }

                    System.out.println("\nPokemon trovato: \n" + p);
                    giphy.display(nome);

                    String s = "";
                    while(!s.trim().equalsIgnoreCase("s") && !s.trim().equalsIgnoreCase("n")){
                        System.out.println("\nVuoi inserire il pokemon? s/n");
                        s = sc.nextLine().trim();
                    }

                    if(s.equalsIgnoreCase("s"))
                        database.insert(p);

                break;
                case 3:
                    System.out.println("\nCiao!");
                return;
                default:
                break;
            }
        }

    }
}