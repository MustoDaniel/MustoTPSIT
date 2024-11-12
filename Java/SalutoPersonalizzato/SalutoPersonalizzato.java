import java.util.Scanner;

public class SalutoPersonalizzato {
    public static void main(String[] args) {
        // Crea un oggetto Scanner per leggere l'input dell'utente
        Scanner scanner = new Scanner(System.in);

        // Chiede all'utente di inserire il suo nome
        System.out.print("Inserisci il tuo nome: ");
        String nome = scanner.nextLine();

        // Stampa un saluto personalizzato
        System.out.println("Ciao, " + nome + "! Benvenuto nel mondo della programmazione!");

        // Chiude lo scanner
        scanner.close();
    }
}