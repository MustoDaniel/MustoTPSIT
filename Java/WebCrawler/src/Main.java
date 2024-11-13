public class Main {
    public static void main(String[] args) {
        WebCrawler w = new WebCrawler(1); //partendo da 0
        w.crawl("https://www.wikipedia.com");
    }
}