import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class WebCrawler {

    private final int maxLevel = 0;

    public Set<String> visited = Collections.synchronizedSet(new HashSet<>());
    public ConcurrentHashMap<Integer, HashSet<String>> gathered = new ConcurrentHashMap<>();

    ThreadPoolExecutor threadPool;

    public WebCrawler(int numThread) {
        threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(Math.max(numThread, 1));
        for(int i=0; i<=maxLevel; i++)
            gathered.put(i, new HashSet<>());
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
    }

    public void crawlWrapper(String url, int level){

        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        WebDriver driver = new ChromeDriver(options);

        try{

            driver.get(url);
            Thread.sleep(5000);
            Document doc = Jsoup.parse(driver.getPageSource());

            synchronized(visited){
                visited.add(url);
            }

            for(Element summaryItem : doc.select("div[class*=cli-children]")){

                String hrefURL = summaryItem.child(0).select("a[href]").get(0).absUrl("href");
                int anno = Integer.parseInt(summaryItem.child(1).child(0).text());
                String durata = summaryItem.child(1).child(1).text();
                String visibilita = summaryItem.child(1).child(2).text();

                //da stampare il titolo, la durata e la visibilità

                gathered.get(level).add(hrefURL);
                System.out.println("nr. " + gathered.get(0).size() + " ->" + hrefURL);
                System.out.print("anno: " + anno);
                System.out.print("; durata: " + durata);
                System.out.println("; visibilità: " + visibilita + "\n");

                if(level < maxLevel && !visited.contains(hrefURL))
                    threadPool.execute(() -> crawlWrapper(hrefURL, level+1));
            }

        }catch(InterruptedException | IllegalArgumentException e){
            System.out.println(e.getMessage());
        }
    }
}