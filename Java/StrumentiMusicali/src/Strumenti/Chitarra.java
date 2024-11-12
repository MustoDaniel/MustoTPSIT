package Strumenti;

import java.util.InputMismatchException;
import java.util.Scanner;

public class Chitarra extends Strumento{

    public final String nome = "Chitarra";
    private String tipo;
    private int numeroDiCorde;

    public Chitarra(String marca, String modello, String tipo, int numeroDiCorde) {
        super(marca, modello);
        this.tipo = tipo;
        this.numeroDiCorde = numeroDiCorde;
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

        String tipo = "";
        while(tipo.isEmpty()){
            System.out.print("Inserisci tipo: ");
            tipo = scanner.nextLine().trim();
        }

        int numeroDiCorde = 0;
        while(numeroDiCorde < 6 || numeroDiCorde > 9){
            System.out.print("Inserisci numero di corde (>=6 & <=8):");
            try {
                numeroDiCorde = scanner.nextInt();
                scanner.nextLine();
            }catch (InputMismatchException e){
                scanner.nextLine();
            }
        }

        return new Chitarra(marca, modello, tipo, numeroDiCorde);
    }

    @Override
    public String toString() {
        return "{ id: " + _id + ", nome: " + nome + ", tipo: " + tipo + ", numero di corde: " + numeroDiCorde + ", " + super.toString() + "}";
    }
}
