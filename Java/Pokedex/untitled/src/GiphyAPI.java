import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.*;

public class GiphyAPI {
    private static final String apiKey = "9DdvvpjC8Z2hwfwQ0xPJkLeeBcPJoAvT";
    private static String url = "https://api.giphy.com/v1/gifs/search?api_key=" + apiKey + "&&q=";

    public GiphyAPI() {}

    public void display(String nome){
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url + nome)).GET().build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONArray json = new JSONObject(response.body()).getJSONArray("data");
            JSONObject giph = json.getJSONObject(0).getJSONObject("images").getJSONObject("original");

            JFrame frame = new JFrame(nome);
            JLabel label = new JLabel(new ImageIcon(new URL(giph.getString("url"))));
            frame.add(label);
            frame.setSize(giph.getInt("width"), giph.getInt("height"));
            frame.setAlwaysOnTop(true);
            frame.setVisible(true);

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
