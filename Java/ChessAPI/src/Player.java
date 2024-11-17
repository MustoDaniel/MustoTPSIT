import java.sql.Time;
import java.sql.Timestamp;

public class Player {
    public int player_id;
    public String username;
    public Timestamp last_online;
    public int rapidbest;
    public int rapidlast;
    public int bulletbest;
    public int bulletlast;
    public int blitzbest;
    public int blitzlast;

    public Player(int player_id, String username, Timestamp last_online, int rapidbest, int rapidlast, int bulletbest, int bulletlast, int blitzbest, int blitzlast) {
        this.player_id = player_id;
        this.username = username;
        this.last_online = last_online;
        this.rapidbest = rapidbest;
        this.rapidlast = rapidlast;
        this.bulletbest = bulletbest;
        this.bulletlast = bulletlast;
        this.blitzbest = blitzbest;
        this.blitzlast = blitzlast;
    }

    @Override
    public String toString(){
        return "player_id: " + player_id + ", username: " + username + ", last_online: " + last_online + ", rapidbest: " + rapidbest
                + ", rapidlast: " + rapidlast + ", bulletbest: " + bulletbest + ", bulletlast: " + bulletlast + ", blitzbest: " + blitzbest + ", blitzlast: " + blitzlast;
    }
}
