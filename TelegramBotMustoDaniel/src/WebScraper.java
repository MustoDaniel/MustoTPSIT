import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.validation.constraints.Null;
import javax.xml.crypto.Data;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebScraper {
    private ThreadPoolExecutor threadPool;

    private static final String url1 = "https://www.giallozafferano.it/ricette-cat/page1";
    //private static final String url2 = "https://www.fattoincasadabenedetta.it/ricetta";

    Database db = new Database();

    public WebScraper() {
        threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(50);
    }

    public void scrapeRicette(){
        scrapeRicetteWrapper(url1, 1, 480); //le ricette nel sito di gialloZafferano sono suddivise in 480 pagine

        while(threadPool.getActiveCount() > 0){
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }
        threadPool.shutdown();
    }

    //Funzione ricorsiva per ottenere il link ad ogni ricetta nel sito di GialloZafferano

    private void scrapeRicetteWrapper(String url, int currentPage, int maxPage) {
        String page = url.replace("page" + (currentPage - 1), "page" + currentPage);    //il link per ogni pagina è uguale, tranne l'ultima parte "/pageN" in cui al posto di N c'è il numero della pagina

        //Il thread si occupa di estrarre il link delle ricette da ogni pagina
        threadPool.execute(() -> {
            try {
                Connection con = Jsoup.connect(page);
                Document doc = con.get();

                Elements linkImmagini = doc.select("div.gz-card-image").select("a[href]");
                Elements linkRicette = doc.select("h2.gz-title");

                for (int i = 0; i < linkRicette.size(); i++) {
                    int finalI = i;
                    threadPool.execute(() -> {
                        scrapeInfoRicetta(linkRicette.get(finalI).select("a[href]").attr("href"), linkImmagini.get(finalI).attr("href"));  //passaggio del link trovato alla funzione scrapeInfoRicetta per ottenere informazioni specifiche sulla ricetta
                    });
                }

                if (currentPage < maxPage)
                    scrapeRicetteWrapper(page, currentPage+1, maxPage);
            }
            catch (IOException e) {
                System.out.println(e.getMessage());
            }
        });
    }

    //funzione per ottenere informazioni specifiche sulla ricetta, dato il link della ricetta su GialloZafferano
    private void scrapeInfoRicetta(String url, String linkImmagine){
        try{
            Connection con = Jsoup.connect(url);
            Document doc = con.get();

            //INFO RICETTA

            String nome = doc.select("h1[class=gz-title-recipe gz-mBottom2x]").text().trim();   //Nome della ricetta

            String tipo = doc.selectFirst("div.gz-breadcrumb")  //Tipo della ricetta es: Dolci / Primi piatti / Contorni ecc..
                    .select("a[href]").text().trim();

            Float rating = Float.parseFloat(doc.selectFirst("div[class=gz-rating-panel rating_panel]") //valutazione della ricetta es: 4.4 (su 5)
                    .attr("data-content-rate")
                    .replace(',', '.').trim());

            int idRicetta = db.insertRicetta(nome, tipo, url, linkImmagine, rating);  //inserimento della ricetta nel database; viene restituito l'id che è stato assegnato automaticamente alla ricetta da parte del database (auto_increment)

            //INFO INGREDIENTI

            for(Element element : doc.select("dd.gz-ingredient")) { //iterazione sugli ingredienti della ricetta
                String ingrediente = element.select("a").text();
                int idIngrediente = db.insertIngrediente(ingrediente);  //inserimento dell'ingrediente nel database; allo stesso modo della ricetta, viene restituito l'id
                if(idIngrediente == -1)                                 //L'id restituito è uguale a -1 se l'ingrediente era già presente nel database
                    db.insertRicettaIngrediente(idRicetta, db.getIdIngrediente(ingrediente));   //in questo caso prima cerco l'id di quell'ingrediente e poi inserisco l'associazione tra ricetta e ingrediente nella tabella ricetta_ingrediente
                else
                    db.insertRicettaIngrediente(idRicetta, idIngrediente);  //stessa cosa della riga sopra, ma l'id ce lo abbiamo già dall'operazione di inserimento dell'ingrediente

                //NOTA: non serve controllare l'id della ricetta perchè (in teoria) ogni thread controlla una ricetta diversa,
                // quindi non eseguirò mai le stesse operazioni più volte con la stessa ricetta
            }

            if(nome.equalsIgnoreCase("torta frangipane"))
                System.out.println("Torta frangipane");

            //INFO FILTRI

            //filtri regime alimentare
            for(Element element : doc.select("div.gz-list-featured-data-other").select("span[class=gz-name-featured-data-other]"))
                Database.insertRicettaFiltro(idRicetta, element.text().trim().toLowerCase());

            //filtro difficoltà
            String difficoltà;

            try{ difficoltà = doc.selectFirst("div.gz-name-featured-data:contains(Difficoltà) strong").text().trim().toLowerCase(); }
            catch (NullPointerException e) { difficoltà = "" ; }

            if(!difficoltà.isEmpty())
                Database.insertRicettaFiltro(idRicetta, difficoltà);

            //filtro tempo di preparazione
                String tempoPreparazioneText, tempoCotturaText;

                try{ tempoPreparazioneText = doc.selectFirst("span.gz-name-featured-data:contains(Preparazione) strong").text().trim(); }
                catch (NullPointerException e) { tempoPreparazioneText = ""; }

                try{ tempoCotturaText = doc.selectFirst("span.gz-name-featured-data:contains(Cottura) strong").text().trim(); }
                catch (NullPointerException e) { tempoCotturaText = ""; }

                // Calcolare il tempo totale di preparazione e cottura in minuti
                int tempoPreparazione = calcolaMinutiTotali(tempoPreparazioneText);
                int tempoCottura = calcolaMinutiTotali(tempoCotturaText);

                int tempoTotale = tempoPreparazione + tempoCottura;

            if(tempoTotale > 0 && tempoTotale <= 15)
                Database.insertRicettaFiltro(idRicetta, "15");
            else if(tempoTotale <= 30)
                Database.insertRicettaFiltro(idRicetta, "30");
            else if(tempoTotale <= 60)
                Database.insertRicettaFiltro(idRicetta, "60");

            if(idRicetta == -1)
                return;

            //INFO PREPARAZIONE
            int index = 0;
            ArrayList<String> immagini = new ArrayList<>();
            String img1, img2, img3;
            for(Element step : doc.select("div.gz-content-recipe-step")){
                for(Element image : step.select("picture"))
                    immagini.add("https://ricette.giallozafferano.it" + image.select("img[src]").getFirst().attr("src").trim());

                try { img1 = immagini.get(index); } catch (Exception e) { img1 = ""; }
                try { img2 = immagini.get(index+1); } catch (Exception e) { img2 = ""; }
                try { img3 = immagini.get(index+2); } catch (Exception e) { img3 = ""; }

                String passaggio = step.select("p").getFirst().text().replaceAll("'", " ");

                Database.insertPreparazione(passaggio, img1, img2, img3, idRicetta);
                index += 3;
            }




        }
        catch (Exception e){
            //System.out.println(e.getMessage());
        }
    }

    // Funzione per calcolare i minuti totali da un testo che potrebbe essere "X h Y min", "X h", o "Y min"
    int calcolaMinutiTotali(String tempoText) {
        Pattern pattern = Pattern.compile("(\\d+)\\s*h(?:\\s*(\\d+)\\s*min)?|^(\\d+)\\s*min$"); //regex per il testo che potrebbe essere "X h Y min", "X h", o "Y min"
        Matcher matcher = pattern.matcher(tempoText);
        if (matcher.find()) {
            if (matcher.group(1) != null) {  // Caso "X h Y min" o "X h"
                int ore = Integer.parseInt(matcher.group(1));
                int minuti = (matcher.group(2) != null) ? Integer.parseInt(matcher.group(2)) : 0;
                return (ore * 60) + minuti;  // Converte le ore in minuti e somma i minuti
            } else if (matcher.group(3) != null) {  // Caso "Y min"
                return Integer.parseInt(matcher.group(3));
            }
        }
        return 0;  // Se non corrisponde a nessun formato, ritorna 0 minuti
    }
}
