package Strumenti;

import java.util.InputMismatchException;
import java.util.Scanner;

public class Triangolo extends Strumento{

    public final String nome = "Triangolo";
    private String materiale;
    private int dimensione;

    public Triangolo(String marca, String modello, String materiale, int dimensione) {
        super(marca, modello);
        this.materiale = materiale;
        this.dimensione = dimensione;
    }

    public static Strumento input(){
        Scanner scanner = new Scanner(System.in);

        String marca = "";
        while(marca.isEmpty()){
            System.out.print("\nInserisci marca: ");
            marca = scanner.nextLine().trim();
        }

        String modello = "";
        while(modello.isEmpty()){
            System.out.print("Inserisci modello: ");
            modello = scanner.nextLine().trim();
        }

        String materiale = "";
        while(materiale.isEmpty()){
            System.out.print("Inserisci materiale: ");
            materiale = scanner.nextLine().trim();
        }

        int dimensione = 0;
        while(dimensione < 5 || dimensione > 51){
            try {
                System.out.print("Inserisci dimensione (>=5 & <=50): ");
                dimensione = scanner.nextInt();
                scanner.nextLine();
            }catch (InputMismatchException e){
                scanner.nextLine();
            }
        }

        return new Triangolo(marca, modello, materiale, dimensione);
    }

    @Override
    public String toString() {
        return "{ id: " + _id + ", nome: " + nome + ", materiale: " + materiale + ", dimensione: " + dimensione + "cm, " + super.toString() + "}";
    }
}
