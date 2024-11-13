import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;

public class WebCrawler {

    private final int maxLevel;

    Set<String> visited = Collections.synchronizedSet(new HashSet<>());
    ConcurrentHashMap<Integer, ArrayList<String>> gathered = new ConcurrentHashMap<>();

    ExecutorService threadPool = Executors.newFixedThreadPool(10);

    public WebCrawler(int maxLevel) {
        this.maxLevel = Math.max(maxLevel, 0);
        for(int i=0; i<=maxLevel; i++)
            gathered.put(i, new ArrayList<>());
    }

    public void crawl(String url){
        crawlWrapper(url, 0);
    }

    public void crawlWrapper(String url, int level){

        synchronized(visited){
            visited.add(url);
        }
        gathered.get(level).add(url);

        try{
            Connection con = Jsoup.connect(url);
            Document doc = con.get();

            for(Element link : doc.select("a[href]")){

                String title = link.attr("title");
                String hrefURL = link.absUrl("href");

                if(!gathered.get(level).contains(hrefURL)){
                    gathered.get(level).add(hrefURL);
                    System.out.println(title + ": " + hrefURL);
                }

                if(level < maxLevel && !visited.contains(hrefURL))
                    threadPool.execute(() -> crawlWrapper(link.absUrl("href"), level+1));
            }

        }catch(IOException e){
            System.out.println(e.getMessage());
            throw new Error(e);
        }
    }
}
