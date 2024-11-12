package Strumenti;

import java.util.InputMismatchException;
import java.util.Scanner;

public class Pianoforte extends Strumento {

    public final String nome = "Pianoforte";
    private String tipo;
    private int numeroDiTasti;

    public Pianoforte(String marca, String modello, String tipo, int numeroDiTasti) {
        super(marca, modello);
        this.tipo = tipo;
        this.numeroDiTasti = numeroDiTasti;
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

        int numeroDiTasti = 0;
        while(numeroDiTasti < 80 || numeroDiTasti > 111){
            System.out.print("Inserisci numero di tasti (>=80 & <=110):");
            try {
                numeroDiTasti = scanner.nextInt();
                scanner.nextLine();
            }catch (InputMismatchException e){
                scanner.nextLine();
            }
        }

        return new Pianoforte(marca, modello, tipo, numeroDiTasti);
    }

    @Override
    public String toString() {
        return "{ id: " + _id + ", nome: " + nome + ", tipo: " + tipo + ", numero di tasti: " + numeroDiTasti + ", " + super.toString() + "}";
    }
}
