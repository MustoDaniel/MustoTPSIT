import javax.sql.DataSource;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

//per effettuare connessione e operazioni su database sql:
import java.sql.*;

public class Parcheggio {
    private String nome;
    private ArrayList<Auto> listaAuto;

    //PATH del file dove vengono salvate le auto
    private static final String filePath = "src/Targhe.txt"; //Static perchè così per tutti i parcheggi creati c'è lo stesso file
                                                             //final == const
    //Credenziali per la connessione al Database
    private final String DB_name = "parcheggio_java";
    private final String DB_url = "jdbc:mysql://localhost:3306/" + DB_name;
    private final String DB_username = "root";
    private final String DB_password = "";

    
    public Parcheggio(String nome) {
        this.nome = nome;
        this.listaAuto = new ArrayList<>();
        //inserisciAutoDaFile();
        inserisciAutoDalDatabase();
    }

    public String getNome() {
        return nome;
    }

    public boolean inserisciAuto(String targa, String marca, String modello) throws AlreadyExistingPlateException{
        try{
            if(cercaAutoPerTarga(targa) != null){
                throw new AlreadyExistingPlateException("\nLa targa " + targa + " è già presente nel parcheggio");
            }
            Auto a = new Auto(targa, marca, modello);
            listaAuto.add(a);
            inserisciAutoSuFile(a);
            inserisciAutoSulDatabase(a);
        }catch (IllegalArgumentException | AlreadyExistingPlateException e){
            System.out.println(e.getMessage());
            return false;
        }catch (IOException e){
            System.out.println("\nImpossibile aprire/chiudere il file");
        }
        return true;
    }

    public boolean rimuoviAuto(String targa) {
        Auto a = cercaAutoPerTarga(targa);
        if(a != null){
            return listaAuto.remove(a) && rimuoviAutoDaFile(targa) && rimuoviAutoDalDatabase(targa);
        }
        return false;
    }

    public Auto cercaAutoPerTarga(String targa) {
        for(Auto a : listaAuto){
            if(a.getTarga().equals(targa.trim()))
                return a;
        }
        return null;
    }

    public String cercaAutoPerMarca(String marca){
        StringBuilder sb = new StringBuilder();
        for(Auto a : listaAuto){
            if(a.getMarca().equalsIgnoreCase(marca.trim()))
                sb.append(a + "\n");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(Auto a : listaAuto)
            sb.append(a+"\n");
        return sb.toString();
    }

    //FUNZIONI FILE

    private void inserisciAutoDaFile(){
        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            for(String line : lines){
                String[] split = line.split(",");
                inserisciAuto(split[0].trim(), split[1].trim(), split[2].trim());
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean inserisciAutoSuFile(Auto auto) throws IOException {
        if(Files.readAllLines(Paths.get(filePath)).toString().contains(auto.getTarga()))
            return false;
        FileWriter fw = new FileWriter(filePath, true);
        fw.write(auto.getTarga() + " , " + auto.getMarca() + " , " + auto.getModello() + "\n");
        fw.flush();
        fw.close();
        return true;
    }

    private boolean rimuoviAutoDaFile(String targa){
        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath)).stream().toList();
            FileWriter fw = new FileWriter(filePath);
            for (String line : lines) {
                if (!line.contains(targa)) {
                    fw.write(line + "\n");
                    fw.flush();
                }
            }
            fw.close();
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //FUNZIONI DATABASE

    private void inserisciAutoDalDatabase(){
        try{
            Connection connection = DriverManager.getConnection(DB_url, DB_username, DB_password);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select * from auto");

            while(resultSet.next()){
                inserisciAuto(resultSet.getString("Targa"), resultSet.getString("Marca"), resultSet.getString("Modello"));
            }

            connection.close();
            statement.close();
            resultSet.close();

        }catch (SQLException e){
            System.out.println("\nATTENZIONE!!!!!!" + "\nErrore di apertura/lettura del database:\n" + e.getMessage() + "\n!!!!!!!!!!!!\n");
        }
    }

    private boolean inserisciAutoSulDatabase(Auto auto){
        try{
            Connection connection = DriverManager.getConnection(DB_url, DB_username, DB_password);
            Statement statement = connection.createStatement();

            ResultSet resultSet = statement.executeQuery("select Targa from auto where Targa = '" + auto.getTarga() + "'");
            if(resultSet.isBeforeFirst())   //Significa che ha trovato un risultato dalla query, altrimenti saremmo nella posizione AfterLast
                return false;

            String insert = "insert into auto values ('" + auto.getTarga() + "', '" + auto.getMarca() + "', '" + auto.getModello() + "');";
            boolean notInserted = statement.execute(insert);

            connection.close();
            statement.close();
            resultSet.close();

            return !notInserted;
        }catch (SQLException e){
            System.out.println("\nATTENZIONE!!!!!!" + "\nErrore di apertura/scrittura del database:\n" + e.getMessage() + "\n!!!!!!!!!!!!\n");
            return false;
        }
    }

    private boolean rimuoviAutoDalDatabase(String targa){
        try{
            Connection connection = DriverManager.getConnection(DB_url, DB_username, DB_password);
            Statement statement = connection.createStatement();

            String remove = "delete from auto where Targa = '" + targa + "';";
            boolean notRemoved = statement.execute(remove);

            connection.close();
            statement.close();

            return !notRemoved;
        }catch (SQLException e){
            System.out.println("\nATTENZIONE!!!!!!" + "\nErrore di apertura/rimozioneEntry del database:\n" + e.getMessage() + "\n!!!!!!!!!!!!\n");
            return false;
        }
    }
}
