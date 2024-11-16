import javax.xml.transform.Result;
import java.sql.*;

public class FilmDB {
    private Connection connection;
    private Statement statement;

    private String URL = "jdbc:mysql://localhost:3306";
    private String DB_name = "FilmDB";

    public FilmDB() {
        try{
            connection = DriverManager.getConnection(URL, "root", "");
            statement = connection.createStatement();

            statement.execute("create database if not exists " + DB_name);
            statement.execute("use " + DB_name);
            String createTable = "create table if not exists film("
                    + "id varchar(64) primary key not null,"
                    + "nome varchar(32) not null,"
                    + "annoUscita int(4) not null,"
                    + "regista varchar(16) not null"
                    + ")";
            statement.execute(createTable);

            URL += "/" + DB_name;
            connection = DriverManager.getConnection(URL, "root", "");
        }
        catch (SQLException e){
            System.out.println("\n" + e.getMessage());
        }
    }

    public void insert(Film f){
        String query = "Insert into film values(" + f.id + ",' " + f.nome + "','" + f.annoUscita + "','" + f.regista + "');" ;

        try {
            statement = connection.createStatement();
            statement.execute(query);
        } catch (SQLException e) {
            System.out.println("\nErrore nell'inserimento del film " + e.getMessage());
            return;
        }

        System.out.println("\nPokemon inserito con successo!");
    }

    public void stampa(){
        String query = "Select * from film";

        try{
            statement = connection.createStatement();
            ResultSet resultSet =  statement.executeQuery(query);

            String result = "";
            if(!resultSet.isBeforeFirst()) {
                System.out.println("\nNon sono presenti film nel database");
                return;
            }
            int i = 1;
            while(resultSet.next()){
                Film f = new Film(resultSet.getString("id"), resultSet.getString("nome"), resultSet.getInt("annoUscita"), resultSet.getString("regista"));
                result += "\n" + f;
            }

            System.out.println(result);
        }
        catch(SQLException e){
            System.out.println("\nErrore connessione al database");
        }
    }
}