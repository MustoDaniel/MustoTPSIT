import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Time;
import java.sql.Timestamp;

public class ChessAPI {
    private String URL = "https://api.chess.com/pub/player/";
    Gson gson = new Gson();

    public ChessAPI(){}

    public Player getPlayer(String nome){
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request1 = HttpRequest.newBuilder().uri(URI.create(URL + nome)).GET().build();
        HttpRequest request2 = HttpRequest.newBuilder().uri(URI.create(URL + nome + "/stats")).GET().build();
        try {
            HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
            HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());

            if(response1.statusCode() >= 200 && response1.statusCode() < 300 && response2.statusCode() >= 200 && response2.statusCode() < 300){
                JSONObject json1 = new JSONObject(response1.body());
                JSONObject json2 = new JSONObject(response2.body());

                int rapidlast, rapidbest, bulletlast, bulletbest, blitzlast, blitzbest;
                rapidlast = rapidbest = bulletlast = bulletbest = blitzlast  = blitzbest = -1;

                if(json2.has("chess_rapid")){
                    rapidlast = json2.getJSONObject("chess_rapid").getJSONObject("last").getInt("rating");
                    rapidbest = json2.getJSONObject("chess_rapid").getJSONObject("best").getInt("rating");
                }
                if(json2.has("chess_bullet")){
                    bulletlast = json2.getJSONObject("chess_bullet").getJSONObject("last").getInt("rating");
                    bulletbest = json2.getJSONObject("chess_bullet").getJSONObject("best").getInt("rating");
                }
                if(json2.has("chess_blitz")){
                    blitzlast = json2.getJSONObject("chess_blitz").getJSONObject("last").getInt("rating");
                    blitzbest = json2.getJSONObject("chess_blitz").getJSONObject("best").getInt("rating");
                }

                return new Player(json1.getInt("player_id"), nome, new Timestamp(json1.getLong("last_online")*1000), rapidlast, rapidbest, bulletlast, bulletbest, blitzlast, blitzbest);
            }

        } catch (IOException | InterruptedException e) {
            System.out.println("\n" + e);
            return null;
        }

        return null;
    }
}
