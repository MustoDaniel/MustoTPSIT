import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.*;

public class PokedexAPI {

    private String url = "https://pokeapi.co/api/v2/pokemon/";
    HttpClient client;

    public PokedexAPI() {
        client = HttpClient.newHttpClient();
    }

    public Pokemon getPokemon(String nome) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + nome))
                .build();

        try {
            HttpResponse<String> response =  client.send(request, HttpResponse.BodyHandlers.ofString());

            if(response.statusCode() >= 200 && response.statusCode() < 300) {
                JSONObject object = new JSONObject(response.body());
                JSONArray types = new JSONArray(object.getJSONArray("types"));

                String type1 = types.getJSONObject(0).getJSONObject("type").getString("name");
                String type2 = "null";

                if(types.length() > 1)
                    type2 = types.getJSONObject(1).getJSONObject("type").getString("name");

                return new Pokemon(object.getInt("id"), nome, type1, type2);
            }

        } catch (IOException | InterruptedException e) {
            System.out.println("\nErrore richiesta Http : " + e.getMessage());
        }

        return null;
    }
}
