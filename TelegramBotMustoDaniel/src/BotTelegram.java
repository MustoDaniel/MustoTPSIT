import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import javax.xml.crypto.Data;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BotTelegram extends TelegramLongPollingBot {
    public String getBotUsername() {
        return "MastroMusto_bot";
    }
    @Override
    public String getBotToken() {
        return "7689541962:AAHCnHZZq1mbcHZLQCcDZki-GPCaoO2im5M";
    }

    public void onUpdateReceived(Update update) {
        String msg = update.getMessage().getText().trim();
        String chatId = update.getMessage().getChatId().toString();
        Timestamp last_active = new Timestamp(System.currentTimeMillis());

        Database.insertUtente(Long.parseLong(chatId), last_active);

        String command = msg.split(" ")[0];
        String[] arguments = msg.replace(command, "").split(",");

        System.out.println(command);
        for(String s : arguments)
            System.out.println(s.trim());

        elaboraComando(command, arguments, Long.parseLong(chatId));

//        SendMessage sendMessage = new SendMessage();
//        sendMessage.setChatId(chatId);
//        sendMessage.setText(msg);

        System.out.println("\nmsg: " + msg + "\nchatId: " + chatId + "\nultimoUtilizzo: " + last_active);

//        try {
//            execute(sendMessage);
//        } catch (TelegramApiException e) {
//            // gestione errore in invio
//        }
    }

    public void elaboraComando(String command, String[] arguments, long chatId) {
        switch(command){
            case "/ricetta":
                ricetta(arguments, chatId);
            break;
            default:
                String errorMessage = "il comando non esiste\n\n" +
                        "comandi disponibili:\n\n" +
                        "1) /ricetta ingrediente1, ingrediente2, ecc.. : trova delle ricette che contengono gli ingredienti forniti";

                SendMessage errore = new SendMessage();
                errore.setChatId(chatId);
                errore.setText(errorMessage);

                try {
                    execute(errore);
                } catch (TelegramApiException e) {
                    System.out.println(e.getMessage());
                }
            break;
        }
    }

    private void ricetta(String[] ingredienti, long chatId) {
        try {
            Map<String, String> ricetteTrovate = Database.getRicette(ingredienti);

            if(ricetteTrovate.isEmpty()) {
                SendMessage message = new SendMessage();
                message.setChatId(String.valueOf(chatId));
                message.setText("Nessuna ricetta trovata");
                execute(message);
            }
            else{
                SendPhoto photo = new SendPhoto();
                SendMessage message = new SendMessage();

                for(Map.Entry<String, String> entry : ricetteTrovate.entrySet()) {

                    Thread.sleep(250);

                    photo.setChatId(String.valueOf(chatId));
                    photo.setPhoto(new InputFile(entry.getValue()));

                    message.setChatId(String.valueOf(chatId));
                    message.setText(entry.getKey());

                    execute(message);
                    execute(photo);
                }
            }
        } catch (TelegramApiException | InterruptedException e) {
            System.out.println("\n" + e.getMessage());
        }
    }

}
class MainBot {
    public static void main(String[] args) {
        try {
            Database db = new Database();
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            api.registerBot(new BotTelegram());
        } catch (TelegramApiException e) {
            // gestione errore in registrazione
        }
    }
}
