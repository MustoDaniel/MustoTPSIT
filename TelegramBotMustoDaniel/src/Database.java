import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.*;
import java.util.ArrayList;

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

            //Creazione delle tabelle del database
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

                // Creazione tabella filtro
                createTable = "create table if not exists filtro(" +
                        "id int primary key not null auto_increment," +
                        "nome varchar(16) unique not null" +
                        ")";
                statement.execute(createTable);
                insertFiltri();

                // Creazione tabella ricetta_filtro
                createTable = "create table if not exists ricetta_filtro(" +
                        "idRicetta int not null," +
                        "idFiltro int not null," +
                        "primary key(idRicetta, idFiltro)," +
                        "foreign key (idRicetta) references ricetta(id) on delete cascade on update cascade," +
                        "foreign key (idFiltro) references filtro(id) on delete cascade on update cascade" +
                        ")";
                statement.execute(createTable);

                // Creazione tabella preparazione
                createTable = "create table if not exists preparazione(" +
                        "id int primary key not null auto_increment," +
                        "preparazione LONGTEXT," +
                        "linkImmagine1 varchar(256)," +
                        "linkImmagine2 varchar(256)," +
                        "linkImmagine3 varchar(256)," +
                        "idRicetta int not null," +
                        "foreign key(idRicetta) references ricetta(id) on delete cascade on update cascade" +
                        ")";
                statement.execute(createTable);

                // Creazione tabella preferiti
                createTable = "create table if not exists preferiti ( " +
                        "idUtente BIGINT not null, " +
                        "idRicetta INT not null, " +
                        "PRIMARY KEY (idUtente, idRicetta), " +
                        "FOREIGN KEY (idUtente) References UTENTE (id) ON DELETE CASCADE, " +
                        "FOREIGN KEY (idRicetta) References RICETTA (id) ON DELETE CASCADE " +
                        ")";
                statement.execute(createTable);

                // Creazione tabella visitati
                createTable = "create table if not exists visitati (" +
                        "idUtente BIGINT not null, " +
                        "idRicetta INT not null, " +
                        "data datetime, " +
                        "PRIMARY KEY (idUtente, idRicetta), " +
                        "FOREIGN KEY (idUtente) References UTENTE (id) ON DELETE CASCADE, " +
                        "FOREIGN KEY (idRicetta) References RICETTA (id) ON DELETE CASCADE " +
                        ")";
                statement.execute(createTable);


            }
        } catch (SQLException e) {
            System.out.println("\n" + e.getMessage());
        }
    }

    //Restituisce una connesione dal pool
    private static synchronized Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    //
    // FUNZIONI PER L'INSERIMENTO DELLE ENTRY NEL DATABASE
    //

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

    //inserimento dei filtri (i filtri sono già noti, non è necessario inserirli tramite webScraper)
    private void insertFiltri(){
        String query = "INSERT INTO filtro (nome) VALUES (?)";
        try(Connection connection = getConnection()){

            PreparedStatement statement = connection.prepareStatement(query);

            //Filtri difficoltà di preparazione della ricetta --> id da 1 a 5 (molto facile -- molto difficile)
            statement.setString(1, "molto facile");
            statement.execute();
            statement.setString(1, "facile");
            statement.execute();
            statement.setString(1, "media");
            statement.execute();
            statement.setString(1, "difficile");
            statement.execute();
            statement.setString(1, "molto difficile");
            statement.execute();

            //Filtri per tempo di preparazione massimo (minuti) --> id da 6 a 8
            statement.setString(1, "15");
            statement.execute();
            statement.setString(1, "30");
            statement.execute();
            statement.setString(1, "60");
            statement.execute();

            //Filtri per regime alimentare --> id da 9 a 12
            statement.setString(1, "light");
            statement.execute();
            statement.setString(1, "senza glutine");
            statement.execute();
            statement.setString(1, "senza lattosio");
            statement.execute();
            statement.setString(1, "vegetariano");
            statement.execute();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    //inserisce un'associazione tra ricetta e filtro
    public static void insertRicettaFiltro(int idR, String nomeF){
        String query = "INSERT INTO ricetta_filtro VALUES (?, ?)";
        int idFiltro = getIdFiltro(nomeF);

        if(idFiltro == -1)
            return;

        try(Connection connection = getConnection()){
            PreparedStatement statement = connection.prepareStatement(query);

            statement.setInt(1, idR);
            statement.setInt(2, idFiltro);
            statement.execute();

        }catch (SQLException e){
            //System.out.println(e.getMessage());
        }
    }

    //inserisce un passaggio della preparazione di una ricetta
    public static void insertPreparazione(String preparazione, String img1, String img2, String img3, int idRicetta){
        String query = "INSERT INTO preparazione (preparazione, linkImmagine1, linkImmagine2, linkImmagine3, idRicetta) VALUES (?, ?, ?, ?, ?)";
        try(Connection connection = getConnection()){
            PreparedStatement statement = connection.prepareStatement(query);

            statement.setString(1, preparazione.replace("'", "''").trim());
            statement.setString(2, img1);
            statement.setString(3, img2);
            statement.setString(4, img3);
            statement.setInt(5, idRicetta);
            statement.execute();

        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }

    //inserisce una ricetta tra i preferiti
    public static void insertPreferiti(int idRicetta, Long idUtente){
        String query = "INSERT INTO preferiti (idRicetta, idUtente) VALUES (?, ?)";
        try(Connection connection = getConnection()){
            PreparedStatement statement = connection.prepareStatement(query);

            statement.setInt(1, idRicetta);
            statement.setLong(2, idUtente);
            statement.execute();

        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }

    //Rimuove la ricetta idRicetta salvata dall'utente idUtente
    public static void deletePreferiti(int idRicetta, Long idUtente){
        String query = "delete from preferiti where idUtente =  ? and idRicetta =  ?";
        try(Connection connection = getConnection()){
            PreparedStatement statement = connection.prepareStatement(query);

            statement.setLong(1, idUtente);
            statement.setInt(2, idRicetta);
            statement.execute();

        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }

    //inserisce una entry (ricetta) nella tabella dei visitati dell'utente idUtente
    public static void insertVisitati(int idRicetta, Long idUtente){
        String query = "INSERT INTO visitati (idRicetta, idUtente, data) VALUES (?, ?, ?)";
        try(Connection connection = getConnection()){
            PreparedStatement statement = connection.prepareStatement(query);

            statement.setInt(1, idRicetta);
            statement.setLong(2, idUtente);
            statement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            statement.execute();

            query = "select count(*) from visitati where idUtente = " + idUtente;
            statement = connection.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            while(rs.next()){
                if(rs.getInt(1) > 10)       //Tengo conto solo delle ultime 10 ricette visitate
                    deleteLastVisited(idUtente);
            }

        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }

    //Elimino la ricetta visitata con la data meno recente
    private static void deleteLastVisited(Long idUtente){
        String query = "select data from visitati where idUtente = " + idUtente + " order by data asc";
        try(Connection connection = getConnection()){
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);

            Timestamp data = new Timestamp(System.currentTimeMillis());
            if(rs.next()) {
                data = rs.getTimestamp("data");
                data.setNanos(0);
            }

            query = "delete from visitati where idUtente = ? and data = ?";
            PreparedStatement p = connection.prepareStatement(query);
            p.setLong(1, idUtente);
            p.setTimestamp(2, data);
            p.execute();
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }

    //
    // FUNZIONI PER L'ESECUZIONE DELLE QUERY
    //

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

    //recupera l'id del filtro dato il suo nome
    private static int getIdFiltro(String nome) {
        String query = "SELECT id FROM filtro WHERE nome = ?";
        try(Connection connection = getConnection()){
            PreparedStatement statement = connection.prepareStatement(query);

            statement.setString(1, nome.replace("'", "''"));
            ResultSet rs = statement.executeQuery();

            if(rs.next()) {
                return rs.getInt("id");
            }else {
                System.out.println("Filtro non trovato");
            }
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
        return -1;
    }

    //ritorna il nome delle ricetta che soddisfano i requisiti degli ingredienti e dei filtri
    public static ArrayList<String> getRicette(ArrayList<String> ingredienti, ArrayList<String> filtri){
        StringBuffer query;
        if(filtri == null || filtri.isEmpty()){
            query = new StringBuffer("select r.nome from ricetta r " +
                    "join ricetta_ingrediente ri on r.id = ri.idRicetta " +
                    "join ingrediente i on ri.idIngrediente = i.id " +
                    "where i.nome in (");
            for(String i : ingredienti)
                query.append("'" + i.replaceAll("'", "''").trim() + "',");
            query.setCharAt(query.lastIndexOf(","), ')');
            query.append(" group by r.id, r.nome having count(distinct i.nome) >= " + ingredienti.size() + " order by r.nome");
        }
        else{
            query = new StringBuffer("select r.nome from ricetta r " +
                    "join ricetta_ingrediente ri on r.id = ri.idRicetta " +
                    "join ingrediente i on ri.idIngrediente = i.id " +
                    "join ricetta_filtro rf on r.id = rf.idRicetta " +
                    "join filtro f on rf.idFiltro = f.id " +
                    "where i.nome in (");
            for(String i : ingredienti)
                query.append("'" + i.replaceAll("'", "''").trim() + "',");
            query.setCharAt(query.lastIndexOf(","), ')');

            query.append(" and f.nome in (");
            for (String f : filtri)
                query.append("'" + f.replaceAll("'", "''").trim() + "',");
            query.setCharAt(query.lastIndexOf(","), ')');
            query.append(" group by r.id, r.nome having count(distinct i.nome) >= " + ingredienti.size() + " and count(distinct f.nome) >= " + filtri.size() + " order by r.nome");
        }

        //SELECT r.nome FROM ricetta r JOIN ricetta_ingrediente ri ON r.id = ri.idRicetta JOIN ingrediente i ON ri.idIngrediente = i.id
        // JOIN ricetta_filtro rf ON r.id = rf.idRicetta
        // JOIN filtro f ON rf.idFiltro = f.id
        // WHERE i.nome IN ('sedano') AND f.nome IN ('senza glutine')
        // GROUP BY r.id, r.nome
        // HAVING COUNT(DISTINCT i.nome) >= 1 AND COUNT(DISTINCT f.nome) >= 1;

        try(Connection connection = getConnection()){
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query.toString());

            ArrayList<String> result = new ArrayList<>();

            while (rs.next())
                result.add(rs.getString("nome"));

            return result;

        }catch (SQLException e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    //ritorna la lista dei filtri
    public static ArrayList<String> getFiltri(){
        String query = "select nome from filtro order by id";

        try(Connection connection = getConnection()){
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);

            ArrayList<String> result = new ArrayList<>();

            while (rs.next())
                result.add(rs.getString("nome"));

            return result;
        }catch (SQLException e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    //ritorna alcune info sulla ricetta dato il nome
    public static ArrayList<Object> getInfoRicetta(String nome){
        String query = "select linkRicetta, linkImmagine, rating from ricetta where nome = \"" + nome + "\"";
        String query2 = "select i.nome from ricetta r join ricetta_ingrediente ri on r.id = ri.idRicetta join ingrediente i on i.id = ri.idIngrediente where r.nome = \"" + nome + "\"";

        try(Connection connection = getConnection()){
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);

            ArrayList<Object> result = new ArrayList<>();

            rs.next();

            result.add(rs.getString("linkRicetta"));
            result.add(rs.getString("linkImmagine"));
            result.add(rs.getFloat("rating"));

            //inserimento ingredienti
            ArrayList<String> ingredienti = new ArrayList<>();
            rs = statement.executeQuery(query2);

            while(rs.next())
                ingredienti.add(rs.getString("nome"));
            result.add(ingredienti);

            return result;
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }

        return null;
    }

    //ritorna tutti i passaggi per la preparazione della ricetta
    public static ArrayList<ArrayList<String>> getInfoPreparazione(String nome){
        String query = "select p.preparazione, p.linkImmagine1, p.linkImmagine2, p.linkImmagine3 " +
                "from ricetta r join preparazione p on p.idRicetta = r.id where r.nome = \"" + nome + "\"";

        try(Connection connection = getConnection()){
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);

            ArrayList<ArrayList<String>> result = new ArrayList<>();

            int index = 0;
            while(rs.next()){
                result.add(new ArrayList<>());
                result.get(index).add(rs.getString("preparazione"));
                result.get(index).add(rs.getString("linkImmagine1"));
                result.get(index).add(rs.getString("linkImmagine2"));
                result.get(index).add(rs.getString("linkImmagine3"));
                index += 1;
            }

            return result;

        }catch (SQLException e){
            System.out.println(e.getMessage());
        }

        return null;
    }

    //ritorna la lista degli utenti
    public static ArrayList<Long> getUtenti(){
        String query = "select id from utente";

        try(Connection connection = getConnection()){
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);

            ArrayList<Long> result = new ArrayList<>();

            while(rs.next())
                result.add(rs.getLong("id"));

            return result;

        }catch (SQLException e){
            System.out.println(e.getMessage());
        }

        return new ArrayList<>();
    }

    //ritorna l'id della ricetta dato il suo nome
    public static int getIdRicetta(String nome){
        String query = "SELECT id FROM ricetta WHERE nome = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, nome);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            } else {
                throw new Error("ricetta non trovata");
            }
        } catch (SQLException e) {
            throw new Error(e);
        }
    }

    //ritorna il nome della ricetta dato il suo id
    public static String getNomeRicetta(int id){
        String query = "SELECT nome FROM ricetta WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, id);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                return rs.getString("nome");
            } else {
                throw new Error("ricetta non trovata");
            }
        } catch (SQLException e) {
            throw new Error(e);
        }
    }

    //ritorna un insieme di ricette che il cui nome contiene il parametro fornito
    public static ArrayList<String> getRicettePerNome(String nome){
        String query = "SELECT nome FROM ricetta WHERE nome like '%" + nome.toLowerCase().trim() + "%'";
        ArrayList<String> result = new ArrayList<>();
        try (Connection connection = getConnection()){
            Statement statement = connection.prepareStatement(query);

            ResultSet rs = statement.executeQuery(query);
            while (rs.next())
                result.add(rs.getString("nome"));

            return result;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return result;
    }

    //ritorna le ricette salvate dall'utente idUtente
    public static ArrayList<String> getPreferiti(Long idUtente) {
        String query = "select r.nome from ricetta r join preferiti p on r.id = p.idRicetta where idUtente = " + idUtente;
        ArrayList<String> result = null;
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();

            result = new ArrayList<>();
            ResultSet rs = statement.executeQuery(query);
            while (rs.next())
                result.add(rs.getString("nome"));

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return result;
    }

    //ritorna true o false se la ricetta idRicetta è stata salvata tra i preferiti dell'utente idUtente
    public static boolean preferitiContains(int idRicetta, Long idUtente){
        String query = "SELECT * FROM preferiti WHERE idRicetta = ? and idUtente = ?";
        try (Connection connection = getConnection()){
            PreparedStatement statement = connection.prepareStatement(query);

            statement.setInt(1, idRicetta);
            statement.setLong(2, idUtente);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            throw new Error(e);
        }
    }

    //ritorna una ricetta random dato il tipo della ricetta (dolce, antipasto, ecc..)
    public static String getRandom(String tipo){
        String query = "SELECT nome FROM ricetta WHERE tipo like '%" + tipo + "%'";
        ArrayList<String> result = new ArrayList<>();
        try (Connection connection = getConnection()){
            Statement statement = connection.prepareStatement(query);

            ResultSet rs = statement.executeQuery(query);
            while (rs.next())
                result.add(rs.getString("nome"));
            int randomInt = (int) (Math.random() * result.size());

            return result.get(randomInt);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return "";
    }

    //ricetta random tra le ricette visitate e quelle salvate
    public static String getConsiglio(Long idUtente){
        if(getCountVisitati(idUtente) + getCountSalvati(idUtente) < 0)
            return "";
        String query = "select distinct r.nome from ricetta r join visitati v on r.id = v.idRicetta join preferiti p on r.id = p.idRicetta where p.idUtente = " + idUtente;
        ArrayList<String> result = new ArrayList<>();
        try (Connection connection = getConnection()){
            Statement statement = connection.prepareStatement(query);

            ResultSet rs = statement.executeQuery(query);
            while (rs.next())
                result.add(rs.getString("nome"));
            int randomInt = (int) (Math.random() * result.size());

            return result.get(randomInt);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return "";
    }

    //ritorna il numero delle ricette visitate dall'utente (max 10)
    private static int getCountVisitati(Long idUtente){
        String query = "select count(*) from visitati where idUtente = " + idUtente;
        try (Connection connection = getConnection()){
            Statement statement = connection.prepareStatement(query);

            ResultSet rs = statement.executeQuery(query);

            if(rs.next())
                return rs.getInt(1);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return 0;
    }

    //ritorna ill numero delle ricette salvate dall'utente
    private static int getCountSalvati(Long idUtente){
        String query = "select count(*) from preferiti where idUtente = " + idUtente;
        try (Connection connection = getConnection()){
            Statement statement = connection.prepareStatement(query);

            ResultSet rs = statement.executeQuery(query);

            if(rs.next())
                return rs.getInt(1);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return 0;
    }

    //ritorna le prime N ricette con rating più alto
    public static ArrayList<String> getTopN(int N){
        String query = "select nome from ricetta order by rating desc";
        ArrayList<String> result = new ArrayList<>();

        if(N <= 0)
            return result;

        try (Connection connection = getConnection()){
            Statement statement = connection.prepareStatement(query);

            ResultSet rs = statement.executeQuery(query);

            int index = 0;
            while(rs.next() && index < N) {
                result.add(rs.getString("nome"));
                index += 1;
            }

            return result;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return result;
    }
}
