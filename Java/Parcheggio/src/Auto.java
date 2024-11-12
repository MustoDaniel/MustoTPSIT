import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Auto {
    private String targa;
    private String marca;
    private String modello;

    public Auto(String targa, String marca, String modello) {
        this.targa = targa;
        this.marca = marca;
        this.modello = modello;
    }

    public String getTarga() {
        return targa;
    }

    public void setTarga(String targa) {
        this.targa = targa;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModello() {
        return modello;
    }

    public void setModello(String modello) {
        this.modello = modello;
    }

    public static boolean controlloTarga(String targa) throws IllegalArgumentException {
        String targaRegex = "^[A-Z]{2}\\d{3}[A-Z]{2}$";
        Pattern pattern = Pattern.compile(targaRegex);
        Matcher matcher = pattern.matcher(targa);   //Mette in relazione la targa con il pattern
        if(!matcher.matches())
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Targa: " + targa + " | Marca: " + marca + " | Modello: " + modello;
    }
}
