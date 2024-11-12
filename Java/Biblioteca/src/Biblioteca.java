import javax.swing.plaf.ListUI;
import java.util.ArrayList;

public class Biblioteca {
    private ArrayList<Libro> biblioteca;

    public Biblioteca() {
        biblioteca = new ArrayList<Libro>();
    }

    public void aggiungiLibro(String titolo, String autore, int anno){
        biblioteca.add(new Libro(titolo, autore, anno));
        System.out.println("\nLibro aggiunto con successo!");
    }

    public ArrayList<Libro> cercaLibro(String key) throws LibroNotFoundException{
        ArrayList<Libro> libriTrovati = new ArrayList<>();
        for(Libro l : biblioteca){
            if(l.getTitolo().trim().equalsIgnoreCase(key) ||l.getTitolo().trim().toLowerCase().contains(key.toLowerCase()))
                libriTrovati.add(l);
        }
        if(libriTrovati.isEmpty())
            throw new LibroNotFoundException();
        return libriTrovati;
    }

    public void visualizzaLibri() throws BibliotecaIsEmptyException{
        if(biblioteca.isEmpty())
            throw new BibliotecaIsEmptyException();
        for(Libro l : biblioteca)
            System.out.println(l.toString());
    }
}
