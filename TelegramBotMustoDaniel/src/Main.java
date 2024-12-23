import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import javax.xml.crypto.Data;

public class Main {
    public static void main(String[] args) {
        try {
            Database db = new Database();
            WebScraper scraper = new WebScraper();
            //scraper.scrapeRicette(); --> da eseguire solo la prima volta per popolare il database;

            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            api.registerBot(new BotTelegram());
        } catch (TelegramApiException e) {
            // gestione errore in registrazione
        }
    }
}