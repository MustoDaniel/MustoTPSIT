import javax.xml.transform.Result;
import java.sql.*;

public class PokedexDB {
    private Connection connection;
    private Statement statement;

    private String URL = "jdbc:mysql://localhost:3306";
    private String DB_name = "pokedex";

    public PokedexDB() {
        try{
            connection = DriverManager.getConnection(URL, "root", "");
            statement = connection.createStatement();

            statement.execute("create database if not exists " + DB_name);
            statement.execute("use " + DB_name);
            String createTable = "create table if not exists pokemon("
                    + "id int(6) primary key not null,"
                    + "nome varchar(32) not null,"
                    + "tipo1 varchar(16) not null,"
                    + "tipo2 varchar(16)"
                    + ")";
            statement.execute(createTable);

            URL += "/" + DB_name;
            connection = DriverManager.getConnection(URL, "root", "");
        }
        catch (SQLException e){
            System.out.println("\n" + e.getMessage());
        }
    }

    public void insert(Pokemon pk){
        String query = "Insert into pokemon values(" + pk.getId() + ",' " + pk.getNome() + "','" + pk.getTipo1() + "','" + pk.getTipo2() + "');" ;

        try {
            statement = connection.createStatement();
            statement.execute(query);
        } catch (SQLException e) {
            System.out.println("\nErrore nell'inserimento del pokemon " + e.getMessage());
            return;
        }

        System.out.println("\nPokemon inserito con successo!");
    }

    public void stampa(){
        String query = "Select * from pokemon";

        try{
            statement = connection.createStatement();
            ResultSet resultSet =  statement.executeQuery(query);

            String result = "";
            if(!resultSet.isBeforeFirst()) {
                System.out.println("\nNon sono presenti pokemon nel pokedex");
                return;
            }
            int i = 1;
            while(resultSet.next()){
                Pokemon p = new Pokemon(resultSet.getInt("id"), resultSet.getString("nome"), resultSet.getString("tipo1"), resultSet.getString("tipo2"));
                result += "\n" + p;
            }

            System.out.println(result);
        }
        catch(SQLException e){
            System.out.println("\nErrore connessione al database");
        }
    }
}
