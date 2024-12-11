import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

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

            String nome = doc.select("h1[class=gz-title-recipe gz-mBottom2x]").text().trim();   //Nome della ricetta

            String tipo = doc.selectFirst("div.gz-breadcrumb")  //Tipo della ricetta es: Dolci / Primi piatti / Contorni ecc..
                    .select("a[href]").text().trim();

            Float rating = Float.parseFloat(doc.selectFirst("div[class=gz-rating-panel rating_panel]") //valutazione della ricetta es: 4.4 (su 5)
                    .attr("data-content-rate")
                    .replace(',', '.').trim());

            int idRicetta = db.insertRicetta(nome, tipo, url, linkImmagine, rating);  //inserimento della ricetta nel database; viene restituito l'id che è stato assegnato automaticamente alla ricetta da parte del database (auto_increment)

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

        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}
