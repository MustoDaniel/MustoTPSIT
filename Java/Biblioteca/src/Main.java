import java.time.Year;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("Benvenuto nella Biblioteca!");

        Biblioteca biblioteca = new Biblioteca();
        Scanner scanner = new Scanner(System.in);

        while(true){
            System.out.println("\n1. Aggiungi un libro");
            System.out.println("2. Cerca un libro per titolo");
            System.out.println("3. Cerca libri online");
            System.out.println("4. Visualizza tutti i libri");
            System.out.println("5. Esci");

            System.out.print("Scegli un'opzione: ");

            int selezione = -1;

            try{
                selezione = scanner.nextInt();
                scanner.nextLine();
            }
            catch(InputMismatchException e){
                scanner.nextLine();
            }

            switch(selezione){
                case 1:
                    String titolo = "";
                    while(titolo.isEmpty()){
                        System.out.print("\nInserisci il titolo: ");
                        titolo = scanner.nextLine().trim();
                    }

                    String autore = "";
                    while(autore.isEmpty()){
                        System.out.print("Inserisci l'autore: ");
                        autore = scanner.nextLine().trim();
                    }

                    int anno = -1;
                    int currentYear = Year.now().getValue();
                    while(anno < 1 || anno > currentYear){
                        System.out.print("Inserisci l'anno di pubblicazione: ");
                        try{
                            anno = scanner.nextInt();
                            scanner.nextLine();
                        }
                        catch(InputMismatchException e){
                            scanner.nextLine();
                        }
                    }

                    biblioteca.aggiungiLibro(titolo, autore, anno);
                    break;
                case 2:
                    String key = "";
                    ArrayList<Libro> libriTrovati;

                    while(key.isEmpty()){
                        System.out.print("\nTitolo o parola per la ricerca: ");
                        key = scanner.nextLine().trim();
                    }
                    try{
                        libriTrovati =  biblioteca.cercaLibro(key);
                    }
                    catch(LibroNotFoundException e){
                        System.out.println("\nSpiacenti! Non è stato trovato nessun libro con questo titolo o contenente questa parola");
                        break;
                    }
                    System.out.println("Sono stati trovati i seguenti libri: ");
                    for(Libro l : libriTrovati)
                        System.out.println("--> " + l.toString());
                    break;
                case 3:
                    String key2 = "";

                    while(key2.isEmpty()){
                        System.out.print("\nTitolo o parola per la ricerca: ");
                        key2 = scanner.nextLine().trim();
                    }

                    GoogleBooksAPI.CercaLibri(key2);

                    break;
                case 4:
                    try{
                        System.out.println("\nLibri disponibili: ");
                        biblioteca.visualizzaLibri();
                    }
                    catch (BibliotecaIsEmptyException e){
                        System.out.println("\nLa biblioteca è ancora vuota. Aggiungi un libro :)");
                        break;
                    }
                    break;
                case 5:
                    System.out.println("\nAlla prossima!");
                    return;
                default:
            }
        }
    }
}