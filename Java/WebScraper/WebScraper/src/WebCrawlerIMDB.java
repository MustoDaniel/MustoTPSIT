import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class WebCrawlerIMDB {

    private final int maxLevel = 0;

    public Set<String> visited = Collections.synchronizedSet(new HashSet<>());
    public ConcurrentHashMap<Integer, HashSet<String>> gathered = new ConcurrentHashMap<>();

    private ThreadPoolExecutor threadPool;

    private ChromeOptions options;
    private WebDriver driver;

    private FilmDB db = new FilmDB();

    public WebCrawlerIMDB(int numThread) {
        threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(Math.max(numThread, 1));
        for(int i=0; i<=maxLevel; i++)
            gathered.put(i, new HashSet<>());

        System.setProperty("webdriver.chrome.driver", "lib/chromedriver-win64/chromedriver-win64/chromedriver.exe");

        options = new ChromeOptions()
                .addArguments("--headless=")
                .addArguments("--disable-gpu")
                .addArguments("user-agent=Chrome")
                .addArguments("--lang=it");

        driver = new ChromeDriver(options);
    }

    public void crawl(String url){
        crawlWrapper(url, 0);

        while(threadPool.getActiveCount() > 0){
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }

        threadPool.shutdown();
        //driver.close();
        db.stampa();
    }

    public void crawlWrapper(String url, int level){

        try{

            driver.get(url);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[contains(@href, 'chttp_t_250')]")));

            Document doc = Jsoup.parse(driver.getPageSource());

            synchronized(visited){
                visited.add(url);
            }

            for(Element summaryItem : doc.select("div[class*=cli-children]")){

                String hrefURL;
                String nome;
                int anno = -1;
                String durata = "null";
                String visibilita = "null";

                if(summaryItem.childNodeSize() >= 1){
                    hrefURL = "www.imdb.com" + summaryItem.child(0).selectFirst("a[href]").attribute("href").getValue();
                    nome = summaryItem.child(0).selectFirst("h3").text().replace("'", "''");;
                }
                else {
                    hrefURL = "null";
                    nome = "null";
                }
                if(summaryItem.childNodeSize() >= 2){
                    if(summaryItem.child(1).childNodeSize() >= 1)
                        anno = Integer.parseInt(summaryItem.child(1).child(0).text());
                    if(summaryItem.child(1).childNodeSize() >= 2)
                        durata = summaryItem.child(1).child(1).text();
                    if(summaryItem.child(1).childNodeSize() >= 3)
                        visibilita = summaryItem.child(1).child(2).text();
                }

                gathered.get(level).add(hrefURL);
                db.insert(new Film(hrefURL, nome, anno, durata, visibilita));

                System.out.println("nr. " + gathered.get(0).size() + " -> " + hrefURL);
                System.out.print("nome: " + nome);
                System.out.print("; anno: " + anno);
                System.out.print("; durata: " + durata);
                System.out.println("; visibilità: " + visibilita + "\n");

                if(level < maxLevel && !visited.contains(hrefURL))
                    threadPool.execute(() -> crawlWrapper(hrefURL, level+1));
            }

        }catch(Exception e){
            System.out.println(e.getMessage());
            //driver.close();
        }
    }
}