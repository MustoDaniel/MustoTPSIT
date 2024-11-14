public class Main {
    public static void main(String[] args) {
        WebCrawler w = new WebCrawler(2, 15); //livello parte da 0; minimo thread: 1
        float startTime, endTime;

        System.out.println("\nInizio crawling\n");
        startTime = System.nanoTime();

        w.crawl("https://www.wikipedia.com/");

        endTime = System.nanoTime();
        System.out.println("\nFine crawling\n");
        System.out.println("Tempo impiegato: " + (endTime - startTime) / 1000000000 + " secondi");
        System.out.println("Link visitati: " + w.visited.size());
        System.out.println("Link raccolti: ");
        for (int i = 0; i < w.gathered.size(); i++)
            System.out.println("Livello " + i + ": " + w.gathered.get(i).size());
        System.out.println("Link non validi: " + w.invalidLinks.size());
    }
}