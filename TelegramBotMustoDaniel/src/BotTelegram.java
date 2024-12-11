import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import javax.xml.crypto.Data;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

public class BotTelegram extends TelegramLongPollingBot {
    public String getBotUsername() {
        return "MastroMusto_bot";
    }
    @Override
    public String getBotToken() {
        return "7689541962:AAHCnHZZq1mbcHZLQCcDZki-GPCaoO2im5M";
    }

    public void onUpdateReceived(Update update) {
        String msg = update.getMessage().getText();
        String chatId = update.getMessage().getChatId().toString();
        Timestamp last_active = new Timestamp(System.currentTimeMillis());

        Database.insertUtente(Long.parseLong(chatId), last_active);

        String[] arguments = msg.split(",");
        String command = arguments[0].split(" ")[0];
        arguments[0] = arguments[0].split(" ")[1];

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
                SendMessage errore = new SendMessage();
                errore.setChatId(chatId);
                errore.setText("Il comando non esiste");
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
            ResultSet rs = Database.getRicette(ingredienti);
            String ricetteTrovate = "";

            while(rs.next())
                ricetteTrovate += rs.getString(0) + "\n";

            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText(ricetteTrovate);

            execute(message);
        } catch (TelegramApiException | SQLException e) {
            System.out.println(e.getMessage());
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
