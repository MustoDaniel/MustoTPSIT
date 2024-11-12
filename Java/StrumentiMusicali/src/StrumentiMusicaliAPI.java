import Strumenti.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;

public class StrumentiMusicaliAPI {

    private ArrayList<Strumento> strumenti = new ArrayList<>();

    private static final String service = "https://crudcrud.com/api/";
    private static final String token = "84702bd2b9ff4993b91ab4d9c18b1bcb";
    private static final String entityName = "/strumentiMusicali";

    private static final String endpointURL = service + token + entityName;

    private final HttpClient client = HttpClient.newHttpClient();
    private HttpRequest request;
    private HttpResponse<String> response;
    private final Gson gson = new Gson();

    //Costruttore
    public StrumentiMusicaliAPI() {
        caricaStrumenti();
    }

    //Getter / Setter
    public ArrayList<Strumento> getStrumenti() {
        return strumenti;
    }

    //Metodi
    public void inserisciStrumento(Strumento s){

        request = HttpRequest.newBuilder()
                .uri(URI.create(endpointURL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(s)))
                .build();
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }
        catch (IOException | InterruptedException e){
            System.out.println("\nErrore nell'inserimento dello strumento: " + e.getMessage());
            return;
        }

        if(response.statusCode() >= 200 && response.statusCode() < 300){
            JsonObject json = gson.fromJson(response.body(), JsonObject.class);
            s.set_id(json.get("_id").getAsString());
            strumenti.add(s);
            System.out.println("\nInserimento avvenuto con successo! id = " + s.get_id());
        }
        else
            System.out.println("\nErrore nell'inserimento dello strumento: " + response);
    }

    public void rimuoviStrumento(String id){

        if(trovaStrumento(id) == null){
            System.out.println("Lo strumento non esiste");
            return;
        }

        request = HttpRequest.newBuilder()
                .uri(URI.create(endpointURL + "/" + id))
                .DELETE()
                .build();
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }
        catch (IOException | InterruptedException e) {
            System.out.println("\nErrore nella rimozione dello strumento: " + e.getMessage());
        }

        if(response.statusCode() >= 200 && response.statusCode() < 300){
            strumenti.remove(trovaStrumento(id));
            System.out.println("\nRimozione avvenuta con successo!");
        }
        else
            System.out.println("\nErrore nella rimozione dello strumento: " + response);
    }

    private void caricaStrumenti(){
        request = HttpRequest.newBuilder()
                .uri(URI.create(endpointURL))
                .GET()
                .build();
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("\nErrore nella richiesta Http: " + e.getMessage());
        }

        if(response.statusCode() >= 200 && response.statusCode() < 300){
            JsonArray array = gson.fromJson(response.body(), JsonArray.class);
            JsonObject strumento;
            Class<?> c = null;
            for(int i = 0; i < array.size(); i++){
                strumento = gson.fromJson(array.get(i).toString(), JsonObject.class);
                try {
                    c = Class.forName("Strumenti." + strumento.get("nome").getAsString());
                } catch (ClassNotFoundException e) {
                    System.out.println("\nClasse non trovata: " + e.getMessage());
                }
                strumenti.add(gson.fromJson(array.get(i), (Type) c));
            }
        }
        else{
            System.out.println("\nErrore caricamento strumenti : " + response);
        }
    }

    public String elencoStrumenti(){
        String elenco = "";

        for(Strumento s: strumenti)
            elenco += s + "\n";

        return elenco;
    }

    public Strumento trovaStrumento(String id){
        for (Strumento s: strumenti) {
            if(s.get_id().equals(id))
                return s;
        }
        return null;
    }
}
