import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;

public class WebCrawler {

    private final int maxLevel;

    public Set<String> visited = Collections.synchronizedSet(new HashSet<>());
    public ConcurrentHashMap<Integer, ArrayList<String>> gathered = new ConcurrentHashMap<>();
    public Set<String> invalidLinks = Collections.synchronizedSet(new HashSet<>());

    ThreadPoolExecutor threadPool;

    public WebCrawler(int maxLevel, int numThread) {
        this.maxLevel = Math.max(maxLevel, 0);
        threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(Math.max(numThread, 1));
        for(int i=0; i<=maxLevel; i++)
            gathered.put(i, new ArrayList<>());
    }

    public void crawl(String url){
        crawlWrapper(url, 0);

        while(threadPool.getActiveCount() > 0){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }

        threadPool.shutdown();
    }

    public void crawlWrapper(String url, int level){

        try{
            Connection con = Jsoup.connect(url);
            Document doc = con.get();

            synchronized(visited){
                visited.add(url);
            }

            for(Element link : doc.select("a[href]")){

                String hrefURL = link.absUrl("href");

                if(!gathered.get(level).contains(hrefURL)){
                    gathered.get(level).add(hrefURL);
                    System.out.println("livello " + level + " ->" + hrefURL);
                }

                if(level < maxLevel && !visited.contains(hrefURL))
                    threadPool.execute(() -> crawlWrapper(link.absUrl("href"), level+1));
            }

        }catch(IOException | IllegalArgumentException e){
            System.out.println(e.getMessage());
            synchronized (invalidLinks){
                invalidLinks.add(url);
            }
        }
    }
}