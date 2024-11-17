public class Main {
    public static void main(String[] args){
        WebCrawlerIMDB w = new WebCrawlerIMDB(1);
        w.crawl("https://www.imdb.com/chart/top/?ref_=nv_mv_250");
    }
}
