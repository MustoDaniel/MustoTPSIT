import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class GoogleBooksAPI {
    public static void CercaLibri(String key){

        try{
            String richiestaElenco = "https://www.googleapis.com/books/v1/volumes?q=";

            try{
                key = URLEncoder.encode(key, "UTF-8");
            }catch(UnsupportedEncodingException e){
                System.out.println("Errore nell'encoding della stringa per la ricerca");
            }

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(richiestaElenco + key)).build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if(response.statusCode() == 200){
                JSONObject object = new JSONObject(response.body());
                JSONArray array;
                try{
                    array = object.getJSONArray("items");
                }catch(JSONException e){
                    System.out.println("\nSpiacenti! Non è stato trovato nessun libro con questo titolo o contenente questa parola");
                    return;
                }

                System.out.println("\nSono stati trovati " + object.getInt("totalItems") + " libri: ");

                for(int i = 0; i < array.length(); i++){
                    JSONObject libro = array.getJSONObject(i);
                    JSONObject volumeInfo = libro.getJSONObject("volumeInfo");

                    String titolo = volumeInfo.getString("title");

                    String autore = "";
                    try{
                        autore = volumeInfo.getJSONArray("authors").getString(0);
                    }catch(JSONException e){
                        autore = "not found";
                    }

                    int anno = -1;
                    try{
                        anno = Integer.parseInt(volumeInfo.getString("publishedDate").substring(0, 4));
                    }catch(JSONException e){}

                    System.out.println((i+1)+ ". " + "titolo: " + titolo + " | autore: " + autore + " | anno: " + anno);
                }
            }
            else{
                System.out.println("Errore nella richiesta HTTPS");
            }

        }catch(java.io.IOException e){
            System.out.println("Errore nell'invio della richiesta Http");
        }catch (InterruptedException e){
            System.out.println("Errore nell'invio della richiesta Http");
        }
    }
}
