import Strumenti.*;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args){
        StrumentiMusicaliAPI api = new StrumentiMusicaliAPI();
        Scanner scanner = new Scanner(System.in);
        int selezione = -1;
        String id;

        while(true){
            System.out.println("\n1. Aggiungi uno strumento");
            System.out.println("2. Rimuovi uno strumento");
            System.out.println("3. Cerca uno strumento");
            System.out.println("4. Visualizza tutti gli strumenti");
            System.out.println("5. Esci");

            System.out.print("\nInserisci un numero: ");

            try {
                selezione = scanner.nextInt();
                scanner.nextLine();
            }catch (InputMismatchException e){
                scanner.nextLine();
                selezione = -1;
            }

            switch(selezione){
                case 1:
                    System.out.println("\n1. Chitarra");
                    System.out.println("2. Pianoforte");
                    System.out.println("3. Triangolo");

                    System.out.print("\nInserisci un numero: ");
                    try{
                        selezione = scanner.nextInt();
                        scanner.nextLine();
                    }catch (InputMismatchException e){
                        System.out.println("valore non valido");
                        scanner.nextLine();
                        break;
                    }

                    switch(selezione){
                        case 1:
                            api.inserisciStrumento(Chitarra.input());
                        break;
                        case 2:
                            api.inserisciStrumento(Pianoforte.input());
                        break;
                        case 3:
                            api.inserisciStrumento(Triangolo.input());
                        break;
                        default:
                            System.out.println("valore non valido");
                        break;
                    }

                break;
                case 2:
                    id = "";
                    while(id.isEmpty()){
                        System.out.print("Inserisci id: ");
                        id = scanner.nextLine().trim();
                    }
                    api.rimuoviStrumento(id);
                break;
                case 3:
                    id = "";
                    while(id.isEmpty()){
                        System.out.print("Inserisci id: ");
                        id = scanner.nextLine().trim();
                    }
                    Strumento s = api.trovaStrumento(id);
                    if(s != null)
                        System.out.println("\nStrumento trovato:\n" + s);
                    else
                        System.out.println("\nNessuno strumento trovato con l'id : " + id);
                break;
                case 4:
                    String elenco = api.elencoStrumenti();
                    if(elenco.trim().isEmpty())
                        System.out.println("\nNon è presente nessuno strumento");
                    else
                        System.out.println("\nStrumenti trovati: \n" + elenco);
                break;
                case 5:
                    System.out.println("\nArrivederci!");
                return;
                default:
                break;
            }
        }
    }
}