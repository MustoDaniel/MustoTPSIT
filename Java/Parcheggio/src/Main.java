import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args){
        Parcheggio parcheggio = new Parcheggio("Parcheggio Daniel");
        System.out.println("Benvenuto nel Parcheggio!");

        Scanner scanner = new Scanner(System.in);
        int option = -1;
        String targa, marca, modello;

        while(true){
            System.out.println("\n1. Aggiungi un'auto");
            System.out.println("2. Rimuovi un'auto");
            System.out.println("3. Cerca un'auto per targa");
            System.out.println("4. Cerca auto per marca");
            System.out.println("5. Visualizza tutte le auto nel parcheggio");
            System.out.println("6. Esci dal programma");
            System.out.print("Inserisci la tua opzione: ");

            try{
                option = scanner.nextInt();
                scanner.nextLine();
            }catch (InputMismatchException e){
                scanner.nextLine();
            }

            switch(option){
                case 1:
                    targa = "";
                    while(!Auto.controlloTarga(targa)) {
                        System.out.print("\nInserisci la targa (Formato: AA111AA): ");
                        targa = scanner.nextLine().trim().toUpperCase();
                    }

                    marca = "";
                    while(marca.isEmpty()) {
                        System.out.print("Inserisci la marca: ");
                        marca = scanner.nextLine().trim();
                    }

                    modello = "";
                    while(modello.isEmpty()) {
                        System.out.print("Inserisci il modello: ");
                        modello = scanner.nextLine().trim();
                    }

                    if(parcheggio.inserisciAuto(targa, marca, modello))
                        System.out.println("\nAuto inserita con successo!");
                break;
                case 2:
                    targa = "";
                    while(!Auto.controlloTarga(targa)) {
                        System.out.print("\nInserisci la targa (Formato: AA111AA): ");
                        targa = scanner.nextLine().trim().toUpperCase();
                    }

                    if(parcheggio.rimuoviAuto(targa))
                        System.out.println("\nAuto rimossa con successo!");
                    else
                        System.out.println("\nAuto non trovata");
                break;
                case 3:
                    targa = "";
                    while(!Auto.controlloTarga(targa)) {
                        System.out.print("\nInserisci la targa (Formato: AA111AA): ");
                        targa = scanner.nextLine().trim().toUpperCase();
                    }

                    Auto a = parcheggio.cercaAutoPerTarga(targa);
                    if(a == null)
                        System.out.println("\nNel parcheggio non è presente un auto con la targa : " + targa);
                    else
                        System.out.println("\nAuto trovata:\n" + a);
                break;
                case 4:
                    marca = "";
                    while (marca.isEmpty()) {
                        System.out.print("\nInserisci la marca: ");
                        marca = scanner.nextLine().trim();
                    }

                    String result = parcheggio.cercaAutoPerMarca(marca);
                    if(result.isEmpty())
                        System.out.println("\nNon è stata trovata nessuna auto della marca "+ marca);
                    else
                        System.out.println("\nAuto trovate: \n" + result);
                break;
                case 5:
                    if(parcheggio.toString().isEmpty())
                        System.out.println("\nNessuna auto parcheggiata");
                    else
                        System.out.println("\nAuto presenti nel parcheggio: \n"+ parcheggio);
                break;
                case 6:
                    System.out.println("\nArrivederci!");
                return;
                default:
                break;
            }
        }
    }
}




























