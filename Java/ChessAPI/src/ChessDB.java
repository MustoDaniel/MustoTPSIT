import javax.swing.plaf.nimbus.State;
import java.sql.*;
import java.util.ArrayList;

public class ChessDB {
    public String URL = "jdbc:mysql://localhost:3306";
    public String DB_name = "Chess";
    public static Connection connection;

    public ChessDB() {
        try {
            connection = DriverManager.getConnection(URL, "root", "");
            Statement statement = connection.createStatement();

            statement.execute("create database if not exists chess");
            statement.execute("use chess");
            statement.execute("create table if not exists player(" +
                    "player_id int(16) not null primary key," +
                    "username varchar(32) not null," +
                    "last_online int(32)," +
                    "rapidbest int(6)," +
                    "rapidlast int(6)," +
                    "bulletbest int(6)," +
                    "bulletlast int(6)," +
                    "blitzbest int(6)," +
                    "blitzlast int(6)" +
                    ");");

            connection = DriverManager.getConnection(URL + "/" + DB_name, "root", "");
        } catch (SQLException e) {
            System.out.println("\n" + e);
        }
    }

    public boolean insertPlayer(Player p){
        try {
            Statement statement = connection.createStatement();
            statement.execute("insert into player values('" + p.player_id + "','" + p.username + "','" + p.last_online.getTime() + "','" +
                    p.rapidbest + "','" + p.rapidlast + "','" + p.bulletbest + "','" + p.bulletlast + "','" + p.blitzbest + "','" + p.blitzlast + "');");
            return true;
        } catch (SQLException e) {
            System.out.println("\n" + e);
            return false;
        }
    }

    public boolean insertPlayerByArrayList(ArrayList<Object> p){
        try {
            Statement statement = connection.createStatement();
            statement.execute("insert into player values(" + p.get(0) + ",'" + p.get(1) + "','" + p.get(2) + "','" +
                    p.get(3) + "','" + p.get(4) + "','" + p.get(5) + "','" + p.get(6) + "','" + p.get(7) + "','" + p.get(8) + "');");
            return true;
        } catch (SQLException e) {
            System.out.println("\n" + e);
            return false;
        }
    }

    public String selectAll(){
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("select * from player");
            String elenco = "";

            if(!rs.isBeforeFirst())
                return "Non sono presenti giocatori nel database";
            while(rs.next()){
                Player p;
                p = new Player(rs.getInt("player_id"), rs.getString("username"), new Timestamp(rs.getLong("last_online")),
                        rs.getInt("rapidbest"), rs.getInt("rapidlast"), rs.getInt("bulletbest"), rs.getInt("bulletlast"),
                        rs.getInt("blitzbest"), rs.getInt("blitzlast"));
                elenco += "\n" + p;
            }
            return elenco;
        } catch (SQLException e) {
            System.out.println("\n" + e);
            return "null";
        }
    }
}
