import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.concurrent.ThreadPoolExecutor;

public class WebScraper {
    private final int maxLevel = 0;

    private ThreadPoolExecutor threadPool;

    private static final String url1 = "https://www.giallozafferano.it/ricette-cat/";
    private static final String url2 = "https://www.fattoincasadabenedetta.it/ricetta";

    public WebScraper(int numThread) {}

    public void scrapeGialloZafferano(){
        Connection con = Jsoup.connect(url1);
        try {
            Document doc = con.get();
            System.out.println(doc.html());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
