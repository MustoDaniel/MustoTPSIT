import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.glassfish.grizzly.http.util.TimeStamp;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Database {
    private static HikariDataSource dataSource;    //utilizzo di HikariCP per la gestione delle connessioni al database da parte dei thread

    private static final String URL = "jdbc:mysql://localhost:3306";
    private static final String DB_NAME = "BotRicetteDB";
    private static final int maxSizeConnectionPool = 50;

    public Database() {
        try {

            // Connessione iniziale per creare il database e le tabelle
            try (Connection connection = DriverManager.getConnection(URL, "root", "")) {
                Statement statement = connection.createStatement();

                // Creazione database se non esiste
                statement.execute("create database if not exists " + DB_NAME);
            }

            // Configurazione di HikariCP
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(URL + "/" + DB_NAME);
            config.setUsername("root");
            config.setPassword("");
            config.setMaximumPoolSize(maxSizeConnectionPool); // Limita il numero di connessioni simultanee

            // Creazione del pool di connessioni
            dataSource = new HikariDataSource(config);

            try (Connection connection = getConnection()) {
                Statement statement = connection.createStatement();

                // Creazione tabella ingrediente
                String createTable = "create table if not exists ingrediente(" +
                        "id int primary key auto_increment not null," +
                        "nome varchar(64) unique not null" +
                        ")";
                statement.execute(createTable);

                // Creazione tabella ricetta
                createTable = "create table if not exists ricetta(" +
                        "id int primary key not null auto_increment," +
                        "nome varchar(128) not null," +
                        "tipo varchar(64)," +
                        "linkRicetta varchar(128) unique not null," +
                        "linkImmagine varchar(128) unique," +
                        "rating float" +
                        ")";
                statement.execute(createTable);

                // Creazione tabella ricetta_ingrediente
                createTable = "create table if not exists ricetta_ingrediente(" +
                        "idRicetta int not null," +
                        "idIngrediente int not null," +
                        "primary key (idRicetta, idIngrediente)," +
                        "foreign key (idIngrediente) references ingrediente(id) on delete cascade on update cascade," +
                        "foreign key (idRicetta) references ricetta(id) on delete cascade on update cascade" +
                        ")";
                statement.execute(createTable);

                // Creazione tabella utente
                createTable = "create table if not exists utente(" +
                        "id BIGINT primary key not null," +
                        "last_active datetime"+
                        ")";
                statement.execute(createTable);
            }
        } catch (SQLException e) {
            //System.out.println("\n" + e.getMessage());
        }
    }

    //Restituisce una connesione dal pool
    private static synchronized Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    //Inserisce un ingrediente e restituisce l'id generato
    public static int insertIngrediente(String nome) {
        String query = "INSERT INTO ingrediente(nome) VALUES (?)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, nome.replace("'", "''").trim().toLowerCase());
            statement.execute();

            ResultSet rs = statement.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            //System.out.println(e.getMessage());
        }
        return -1;
    }

    //Inserisce la ricetta e restituisce l'id generato
    public static int insertRicetta(String nome, String tipo, String linkRicetta, String linkImmagine, Float rating) {
        String query = "INSERT INTO ricetta(nome, tipo, linkRicetta, linkImmagine, rating) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, nome.replace("'", "''").trim().toLowerCase());
            statement.setString(2, tipo.replace("'", "''").trim().toLowerCase());
            statement.setString(3, linkRicetta);
            statement.setString(4, linkImmagine);
            statement.setFloat(5, rating);
            statement.execute();

            ResultSet rs = statement.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return -1;
    }

    //inserisce un'associazione tra ricetta e ingrediente
    public static void insertRicettaIngrediente(int idR, int idI) {
        String query = "INSERT INTO ricetta_ingrediente VALUES (?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, idR);
            statement.setInt(2, idI);
            statement.execute();
        } catch (SQLException e) {
            //System.out.println(e.getMessage());
        }
    }

    //inserisce l'utente dato l'id e l'ultimo accesso (last_active)
    public static void insertUtente(long id, Timestamp last_active){
        String query = "INSERT INTO utente VALUES (?, ?)";
        try(Connection connection = getConnection()){
            PreparedStatement statement = connection.prepareStatement(query);

            statement.setLong(1, id);
            statement.setTimestamp(2, last_active);
            statement.execute();
        }catch (SQLException e){

            System.out.println(e.getMessage());
            System.out.println("Utente già presente, aggiornamento dell'ultimo accesso");

            query = "UPDATE utente SET last_active = ? WHERE id = ?";
            try(Connection connection = getConnection()){
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setTimestamp(1, last_active);
                statement.setLong(2, id);
                statement.execute();
            }
            catch (SQLException e2) {
                System.out.println(e2.getMessage());
            }
        }
    }

    //recupera l'id di un ingrediente dato il suo nome
    public static int getIdIngrediente(String nome) {
        String query = "SELECT id FROM ingrediente WHERE nome = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, nome.replace("'", "''"));
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            } else {
                throw new Error("Ingrediente non trovato");
            }
        } catch (SQLException e) {
            throw new Error(e);
        }
    }

    public static Map<String, String> getRicette(String[] ingredienti){
        StringBuffer query = new StringBuffer("select r.nome, r.linkImmagine from ricetta r " +
                "join ricetta_ingrediente ri on r.id = ri.idRicetta " +
                "join ingrediente i on ri.idIngrediente = i.id " +
                "where i.nome in (");
        for(String i : ingredienti)
            query.append("'" + i.replaceAll("'", "''").trim() + "',");
        query.setCharAt(query.lastIndexOf(","), ')');
        query.append(" group by r.id, r.nome having count(distinct i.nome) >= " + ingredienti.length);

        try(Connection connection = getConnection()){
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query.toString());

            HashMap<String, String> result = new HashMap<>();

            while (rs.next())
                result.put(rs.getString("nome"), rs.getString("linkImmagine"));

            return result;

        }catch (SQLException e){
            System.out.println(e.getMessage());
            return null;
        }
    }
}
