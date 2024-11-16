public class Main {
    public static void main(String[] args){
        WebCrawler w = new WebCrawler(1);
        w.crawl("https://www.imdb.com/chart/top/?ref_=nv_mv_250");
    }
}
