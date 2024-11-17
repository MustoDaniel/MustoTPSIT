import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args){
        ChessAPI api = new ChessAPI();
        ChessDB db = new ChessDB();
        Scanner sc = new Scanner(System.in);

        int selezione = -1;

        while(true){
            System.out.println("\n1. Visualizza giocatori");
            System.out.println("2. Inserisci giocatore");
            System.out.println("3. Esci");
            System.out.print("Inserisci un numero: ");

            try{
                selezione = sc.nextInt();
                sc.nextLine();
            }catch(InputMismatchException e) {
                selezione = -1;
                sc.nextLine();
            }

            switch (selezione) {
                case 1:
                    System.out.println(db.selectAll());
                    break;
                case 2:
                    String nome = "";
                    while (nome.isEmpty()) {
                        System.out.print("\nInserisci un nome: ");
                        nome = sc.nextLine().trim();
                    }

                    Player p = api.getPlayer(nome);
                    if(p == null) {
                        System.out.println("Il giocatore non esiste");
                        break;
                    }

                    System.out.println("\nGiocatore trovato: \n" + p);

                    String sn = "";
                    while (!sn.equalsIgnoreCase("s") && !sn.equalsIgnoreCase("n")) {
                        System.out.print("\nVuoi inserire il giocatore? s/n: ");
                        sn = sc.nextLine().trim();
                    }

                    if (sn.equalsIgnoreCase("s")) {
                        if (db.insertPlayer(p))
                            System.out.println("\nGiocatore inserito con successo!");
                    }
                break;
                case 3:
                    System.out.println("\nArrivederci!");
                return;
            }
        }
    }
}
