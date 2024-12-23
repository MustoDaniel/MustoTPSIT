import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import javax.imageio.ImageIO;
import javax.xml.crypto.Data;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Array;
import java.sql.Timestamp;
import java.util.*;
import java.util.List;

public class BotTelegram extends TelegramLongPollingBot {
    public String getBotUsername() {
        return "MastroMusto_bot";
    }
    @Override
    public String getBotToken() {
        return "7689541962:AAHCnHZZq1mbcHZLQCcDZki-GPCaoO2im5M";
    }

    private static HashMap<Long, ArrayList<String>> ricetteUtente = new HashMap<>();    //Lista delle ultime ricette cercate dall'utente
    private static HashMap<Long, Integer> userMessageIds = new HashMap<>();             //Id dell'ultimo messaggio inviato dal bot all'utente

    private static HashMap<Long,ArrayList<String>> filtri = new HashMap<>();            //Lista dei filtri
    private static HashMap<Long, ArrayList<String>> checkedFiltri = new HashMap<>();    //Lista dei filtri selezionati

    private static HashMap<Long, ArrayList<String>> ingredienti = new HashMap<>();      //Lista degli ingredienti scritti dall'utente
    private static HashMap<Long, ArrayList<ArrayList<String>>> passaggi = new HashMap<>();  //Lista dei passaggi di preparazione della ricetta (vengono salvati solo quelli dell'ultima ricetta di cui si è richiesta la preparazione)

    public BotTelegram(){
        for(Long u : Database.getUtenti())
            caricaFiltri(u);
    }

    public void caricaFiltri(Long idUtente){
        ArrayList<String> f = Database.getFiltri();
        for (int i = 0; i < f.size(); i++) {
            String filtro = f.get(i);
            if ("15".equals(filtro) || "30".equals(filtro) || "60".equals(filtro)) {
                filtro += " min";
            }
            f.set(i, "⬜ " + filtro);
        }
        filtri.put(idUtente, new ArrayList<>(f));
    }

    //
    //ELABORAZONE MESSAGGI RICEVUTI
    //

    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String msg = update.getMessage().getText().trim();
            String chatId = update.getMessage().getChatId().toString();
            Timestamp last_active = new Timestamp(System.currentTimeMillis());

            if(!Database.getUtenti().contains(Long.parseLong(chatId))) {
                Database.insertUtente(Long.parseLong(chatId), last_active);
                caricaFiltri(Long.parseLong(chatId));
            }

            String command = msg.split(" ")[0];
            String[] arguments = msg.replace(command, "").split(",");

            System.out.println(command);
            for(String s : arguments)
                System.out.println(s.trim());
            elaboraComando(command.toLowerCase().trim(), arguments, Long.parseLong(chatId));
            //sendCheckboxOptions(update.getMessage().getChatId());
        } else if (update.hasCallbackQuery()) {
            // Estraiamo la callback query
            CallbackQuery callbackQuery = update.getCallbackQuery();
            // Chiama la funzione per gestire la callback e aggiornare il pulsante cliccato
            //handleCallbackQuery(callbackQuery);
            //handleUserSelection(callbackQuery);
            handleCallback(callbackQuery);
        }
    }

    //
    //ELABORAZIONE COMANDI
    //

    public void elaboraComando(String command, String[] arguments, long chatId) {
        switch(command){
            case "ricetta":
                InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rows = new ArrayList<>();

                rows.add(new ArrayList<>());

                InlineKeyboardButton buttonFiltro = new InlineKeyboardButton();
                buttonFiltro.setText("Filtri");
                buttonFiltro.setCallbackData("filtri");

                InlineKeyboardButton buttonRicerca = new InlineKeyboardButton();
                buttonRicerca.setText("Ricerca");
                buttonRicerca.setCallbackData("ricerca");

                rows.get(0).add(buttonFiltro);
                rows.get(0).add(buttonRicerca);

                keyboardMarkup.setKeyboard(rows);

                SendMessage message = new SendMessage();
                message.setChatId(chatId);
                message.setText("Applica filtri o inizia ricerca");
                message.setReplyMarkup(keyboardMarkup);

                try {
                    Message m = execute(message);
                    if(userMessageIds.get(chatId) != null)
                        deleteMessage(chatId, userMessageIds.get(chatId));
                    userMessageIds.put(chatId, m.getMessageId());
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }

                ArrayList<String> ing = new ArrayList<>();
                for(String a : arguments)
                    ing.add(a);
                ingredienti.put(chatId, ing);
            break;
            case "preferiti":
                ricetteUtente.put(chatId, Database.getPreferiti(chatId));
                try{ingredienti.get(chatId).clear();}catch (Exception e){};

                if(ricetteUtente.get(chatId).isEmpty()){
                    message = new SendMessage();
                    message.setChatId(chatId);
                    message.setText("Non hai ancora salvato delle ricette");

                    try {
                        Message m = execute(message);

                        deleteMessage(chatId, userMessageIds.get(chatId));
                        userMessageIds.put(chatId, m.getMessageId());
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                }else
                    sendPagedRecipeButtons(chatId, ricetteUtente.get(chatId), 0);
            break;
            case "random":
                //antipasti, primi, secondi, dolci, contorni, bevande
                InlineKeyboardMarkup keyboardMarkup2 = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rows2 = new ArrayList<>();

                InlineKeyboardButton antipasti =  new InlineKeyboardButton();
                antipasti.setText("Antipasti  \uD83C\uDF64 ");
                antipasti.setCallbackData("random_antipasti");

                InlineKeyboardButton primi =  new InlineKeyboardButton();
                primi.setText("Primi  \uD83C\uDF5D ");
                primi.setCallbackData("random_primi");

                InlineKeyboardButton secondi =  new InlineKeyboardButton();
                secondi.setText("Secondi  \uD83C\uDF56 ");
                secondi.setCallbackData("random_secondi");

                InlineKeyboardButton dolci =  new InlineKeyboardButton();
                dolci.setText("Dolci  \uD83C\uDF70 ");
                dolci.setCallbackData("random_dolci");

                InlineKeyboardButton contorni =  new InlineKeyboardButton();
                contorni.setText("Contorni  \uD83E\uDD57 ");
                contorni.setCallbackData("random_contorni");

                InlineKeyboardButton bevande =  new InlineKeyboardButton();
                bevande.setText("Bevande  \uD83C\uDF77 ");
                bevande.setCallbackData("random_bevande");

                rows2.add(new ArrayList<>());
                rows2.get(0).add(antipasti);
                rows2.get(0).add(primi);

                rows2.add(new ArrayList<>());
                rows2.get(1).add(secondi);
                rows2.get(1).add(dolci);

                rows2.add(new ArrayList<>());
                rows2.get(2).add(contorni);
                rows2.get(2).add(bevande);

                keyboardMarkup2.setKeyboard(rows2);

                SendMessage message2 = new SendMessage();
                message2.setChatId(chatId);
                message2.setText("Scegli un'opzione: ");
                message2.setReplyMarkup(keyboardMarkup2);

                try {
                    Message m = execute(message2);
                    if(userMessageIds.containsKey(chatId))
                        deleteMessage(chatId, userMessageIds.get(chatId));
                    userMessageIds.put(chatId, m.getMessageId());
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }

                break;
            case "cerca":
                ArrayList<String> cerca = Database.getRicettePerNome(arguments[0]);
                if(cerca.isEmpty() || arguments[0].trim().isEmpty()){
                    message = new SendMessage();
                    message.setChatId(chatId);
                    message.setText("Nessuna ricetta trovata!\nnome inserito = " + arguments[0]);

                    try {
                        Message m = execute(message);

                        if(userMessageIds.containsKey(chatId))
                            deleteMessage(chatId, userMessageIds.get(chatId));
                        userMessageIds.put(chatId, m.getMessageId());
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                }else{
                    ricetteUtente.put(chatId, cerca);
                    sendPagedRecipeButtons(chatId, ricetteUtente.get(chatId), 0);
                }
            break;
            case "consiglia":
                String consiglio = Database.getConsiglio(chatId);
                if(consiglio.isEmpty()){
                    message = new SendMessage();
                    message.setChatId(chatId);
                    message.setText("Al momento non è possibile consigliare una ricetta\nVisita o salva altre ricette");

                    try {
                        Message m = execute(message);

                        if(userMessageIds.containsKey(chatId))
                            deleteMessage(chatId, userMessageIds.get(chatId));
                        userMessageIds.put(chatId, m.getMessageId());
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                }
                else{
                    ArrayList<String> r = new ArrayList<>();
                    r.add(consiglio);

                    ricetteUtente.put(chatId, r);
                    sendPagedRecipeButtons(chatId, ricetteUtente.get(chatId), 0);
                }
            break;
            case "top":
                int N = 0;
                try{ N = Integer.parseInt(arguments[0].trim()); } catch(Exception e){ }

                ArrayList<String> top = Database.getTopN(N);

                if(top.isEmpty()){
                    message = new SendMessage();
                    message.setChatId(chatId);
                    message.setText("Impossibile trovare le top ricette\nNumero inserito: " + arguments[0]);

                    try {
                        Message m = execute(message);

                        if(userMessageIds.containsKey(chatId))
                            deleteMessage(chatId, userMessageIds.get(chatId));
                        userMessageIds.put(chatId, m.getMessageId());
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                }
                else{
                    ricetteUtente.put(chatId, top);
                    sendPagedRecipeButtons(chatId, ricetteUtente.get(chatId), 0);
                }
            break;
            case "ciao": case "Ciao" :
                message = new SendMessage();
                message.setChatId(chatId);
                message.setText("Ciao! \uD83D\uDC4B");

                try {
                    Message m = execute(message);

                    if(userMessageIds.containsKey(chatId))
                        deleteMessage(chatId, userMessageIds.get(chatId));
                    userMessageIds.put(chatId, m.getMessageId());
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            break;
            case "grazie" : case "Grazie" :
                message = new SendMessage();
                message.setChatId(chatId);
                message.setText("Non c'è di che! \uD83E\uDEE1");

                try {
                    Message m = execute(message);

                    if(userMessageIds.containsKey(chatId))
                        deleteMessage(chatId, userMessageIds.get(chatId));
                    userMessageIds.put(chatId, m.getMessageId());
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "aiuto" : default:
                String errorMessage =
                        "comandi disponibili:\n\n" +
                        "1)ricetta [ingrediente1, ingrediente2, ..] : trova delle ricette che contengono gli ingredienti forniti\n\n" +
                        "2)preferiti : mostra le ricette salvate\n\n" +
                        "3)random : fornisce una ricetta casuale, è possibile specificare il tipo\n\n" +
                        "4)cerca [nomeRicetta] : fornisce la ricetta cercata in base a nome\n\n" +
                        "5)consiglia : consiglia una ricetta in base alle ultime ricette visitate\n\n" +
                        "6)top [N] : fornisce le prime N ricette in base alla valutazione\n\n" +
                        "7)aiuto : lista dei comandi";

                SendMessage errore = new SendMessage();
                errore.setChatId(chatId);
                errore.setText(errorMessage);

                try {
                    Message m = execute(errore);
                    if(userMessageIds.containsKey(chatId))
                        deleteMessage(chatId, userMessageIds.get(chatId));
                    userMessageIds.put(chatId, m.getMessageId());
                } catch (TelegramApiException e) {
                    System.out.println(e.getMessage());
                }
            break;
        }
    }

    //reindirizza o esegue operazioni in base al pulsante che è stato cliccato
    public void handleCallback(CallbackQuery callbackQuery) {
        Long chatId = callbackQuery.getMessage().getChatId();
        String callbackData = callbackQuery.getData();
        switch (callbackData){
            case "filtri":
                sendCheckboxOptions(chatId);
                break;
            case "ricerca":
                try{
                    if(!ingredienti.get(chatId).isEmpty())
                        ricetteUtente.put(chatId, Database.getRicette(ingredienti.get(chatId), checkedFiltri.get(chatId)));
                }catch (Exception e){
                    System.out.println(e.getMessage());
                }
                if(ricetteUtente.get(chatId).isEmpty()){
                    SendMessage message = new SendMessage();
                    message.setChatId(chatId);
                    message.setText("Nessuna ricetta trovata" + "\ningredienti: " + ingredienti.get(chatId) + "\nfiltri: " + checkedFiltri.get(chatId));

                    try {
                        Message m = execute(message);

                        if(userMessageIds.containsKey(chatId))
                            deleteMessage(chatId, userMessageIds.get(chatId));
                        userMessageIds.put(chatId, m.getMessageId());
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                }else
                    sendPagedRecipeButtons(chatId, ricetteUtente.get(chatId), 0);
                break;
        }
        if(callbackData.startsWith("index_"))
            handleUserSelection(callbackQuery);
        else if (callbackData.startsWith("prev_page") || callbackData.startsWith("next_page"))
            handleCallbackQuery(callbackQuery);
        else if (callbackData.startsWith("recipe_"))
            sendInfoRicetta(callbackQuery);
        else if(callbackData.startsWith("preparazione_"))
            preparazione(callbackQuery);
        else if(callbackData.startsWith("avanti_") || callbackData.startsWith("indietro_"))
            preparazioneNext(callbackQuery);
        else if(callbackQuery.getData().startsWith("salva_")) {
            int idRicetta = Integer.parseInt(callbackQuery.getData().substring(6));
            Database.insertPreferiti(idRicetta, callbackQuery.getMessage().getChatId());

            deleteMessage(chatId, callbackQuery.getMessage().getMessageId());

            callbackQuery.setData("recipe_" + ricetteUtente.get(chatId).indexOf(Database.getNomeRicetta(idRicetta)));
            sendInfoRicetta(callbackQuery);
        }
        else if(callbackQuery.getData().startsWith("rimuovi_")){
            int idRicetta = Integer.parseInt(callbackQuery.getData().substring(8));
            Database.deletePreferiti(idRicetta, callbackQuery.getMessage().getChatId());

            deleteMessage(chatId, callbackQuery.getMessage().getMessageId());

            callbackQuery.setData("recipe_" + ricetteUtente.get(chatId).indexOf(Database.getNomeRicetta(idRicetta)));
            sendInfoRicetta(callbackQuery);
        }
        else if(callbackQuery.getData().contains("random_")){
            ArrayList<String> r = new ArrayList<>();
            r.add(Database.getRandom(callbackQuery.getData().substring(7)));

            ricetteUtente.put(chatId, r);

            callbackQuery.setData("recipe_0");
            sendInfoRicetta(callbackQuery);
        }
        else if(callbackQuery.getData().contains("fine")){
            sendPagedRecipeButtons(chatId, ricetteUtente.get(chatId), 0);
        }
    }

    //
    //FILTRI
    //

    //Invia le checkbox per selezionare i filtri
    public void sendCheckboxOptions(Long chatId) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (int i = 0; i < filtri.get(chatId).size(); i += 2) {
            List<InlineKeyboardButton> row = new ArrayList<>();

            // Aggiungi il primo pulsante alla riga
            InlineKeyboardButton button1 = new InlineKeyboardButton();
            button1.setText(filtri.get(chatId).get(i));
            button1.setCallbackData("index_" + i);  // callback per tracciare l'opzione selezionata
            row.add(button1);

            // Aggiungi il secondo pulsante alla riga se esiste
            if (i + 1 < filtri.get(chatId).size()) {
                InlineKeyboardButton button2 = new InlineKeyboardButton();
                button2.setText(filtri.get(chatId).get(i + 1));
                button2.setCallbackData("index_" + (i + 1));  // callback per tracciare l'opzione selezionata
                row.add(button2);
            }

            // Aggiungi la riga alla tastiera
            rows.add(row);
        }

        InlineKeyboardButton buttonRicerca = new InlineKeyboardButton();
        buttonRicerca.setText("Ricerca");
        buttonRicerca.setCallbackData("ricerca");
        rows.add(new ArrayList<>());
        rows.getLast().add(buttonRicerca);

        // Aggiungi i pulsanti alla tastiera
        keyboardMarkup.setKeyboard(rows);

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Seleziona i filtri:");
        message.setReplyMarkup(keyboardMarkup);

        try {
            Message m = execute(message);
            deleteMessage(chatId, userMessageIds.get(chatId));

            userMessageIds.put(chatId, m.getMessageId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Gestisce la selezione dei filtri
    private void handleUserSelection(CallbackQuery callbackQuery) {
        String callbackData = callbackQuery.getData();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();

        if (callbackData.startsWith("index_")) {
            int index = Integer.parseInt(callbackData.substring(6));

            if (filtri.get(chatId).get(index).startsWith("⬜")) {
                filtri.get(chatId).set(index, filtri.get(chatId).get(index).replace("⬜", "✅"));
                ArrayList<String> ckF = checkedFiltri.get(chatId);
                if(ckF == null)
                    ckF = new ArrayList<>();
                ckF.add(filtri.get(chatId).get(index).replace("✅", "").trim());
                checkedFiltri.put(chatId, ckF);
            }
            else {
                filtri.get(chatId).set(index, filtri.get(chatId).get(index).replace("✅", "⬜"));
                ArrayList<String> ckF = checkedFiltri.get(chatId);
                ckF.remove(filtri.get(chatId).get(index).replace("⬜", "").trim());
                checkedFiltri.put(chatId, ckF);
            }

            InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            for (int i = 0; i < filtri.get(chatId).size(); i += 2) {
                List<InlineKeyboardButton> row = new ArrayList<>();

                InlineKeyboardButton button1 = new InlineKeyboardButton();
                button1.setText(filtri.get(chatId).get(i));
                button1.setCallbackData("index_" + i);
                row.add(button1);

                if (i + 1 < filtri.get(chatId).size()) {
                    InlineKeyboardButton button2 = new InlineKeyboardButton();
                    button2.setText(filtri.get(chatId).get(i + 1));
                    button2.setCallbackData("index_" + (i + 1));
                    row.add(button2);
                }

                rows.add(row);
            }

            InlineKeyboardButton buttonRicerca = new InlineKeyboardButton();
            buttonRicerca.setText("Ricerca");
            buttonRicerca.setCallbackData("ricerca");
            rows.add(new ArrayList<>());
            rows.getLast().add(buttonRicerca);

            keyboardMarkup.setKeyboard(rows);

            EditMessageReplyMarkup editMessage = new EditMessageReplyMarkup();
            editMessage.setChatId(chatId);
            editMessage.setMessageId(messageId);
            editMessage.setReplyMarkup(keyboardMarkup);

            try {
                execute(editMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    //
    //PULSANTI RICETTE
    //

    //Invia le ricette trovate suddivise per "pagina" --> max 10 ricette per pagina
    private void sendPagedRecipeButtons(Long chatId, ArrayList<String> recipes, int page) {
        int buttonsPerPage = 10;  // Numero massimo di pulsanti per pagina
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        // Calcola l'inizio e la fine della pagina
        int start = page * buttonsPerPage;
        int end = Math.min(start + buttonsPerPage, recipes.size());

        // Aggiungi i pulsanti per le ricette
        for (int i = start; i < end; i++) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(recipes.get(i));
            button.setCallbackData("recipe_" + i);  // Aggiungi l'indice della ricetta alla callback
            row.add(button);
            rows.add(row);
        }

        // Aggiungi i pulsanti "Indietro" e "Avanti" per la paginazione
        List<InlineKeyboardButton> paginationRow = new ArrayList<>();

        if (page > 0) {
            // Pulsante "Indietro"
            InlineKeyboardButton backButton = new InlineKeyboardButton();
            backButton.setText("Indietro");
            backButton.setCallbackData("prev_page_" + (page - 1));  // Pagina precedente
            paginationRow.add(backButton);
        }

        if (end < recipes.size()) {
            // Pulsante "Avanti"
            InlineKeyboardButton nextButton = new InlineKeyboardButton();
            nextButton.setText("Avanti");
            nextButton.setCallbackData("next_page_" + (page + 1));  // Pagina successiva
            paginationRow.add(nextButton);
        }

        // Aggiungi i pulsanti di paginazione
        if (!paginationRow.isEmpty())
            rows.add(paginationRow);

        keyboardMarkup.setKeyboard(rows);

        // Invia il messaggio iniziale con la tastiera inline
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Ricette trovate: ");
        sendMessage.setReplyMarkup(keyboardMarkup);

        try {
            Message message = execute(sendMessage);  // Invia il messaggio
            Integer messageId = message.getMessageId();  // Ottieni il messageId

            if(userMessageIds.get(chatId) != null)
                deleteMessage(chatId, userMessageIds.get(chatId));
            userMessageIds.put(chatId, messageId);  // Salviamo il messageId nella mappa
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    //Gestisce l'update della pagina quando si cliccano i pulsanti Avanti o Indietro
    private void updatePagedRecipeButtons(Long chatId, ArrayList<String> recipes, int page) {
        int buttonsPerPage = 10;  // Numero massimo di pulsanti per pagina
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        // Calcola l'inizio e la fine della pagina
        int start = page * buttonsPerPage;
        int end = Math.min(start + buttonsPerPage, recipes.size());

        // Aggiungi i pulsanti per le ricette
        for (int i = start; i < end; i++) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(recipes.get(i));
            button.setCallbackData("recipe_" + i);  // Aggiungi l'indice della ricetta alla callback
            row.add(button);
            rows.add(row);
        }

        // Aggiungi i pulsanti "Indietro" e "Avanti" per la paginazione
        List<InlineKeyboardButton> paginationRow = new ArrayList<>();

        if (page > 0) {
            // Pulsante "Indietro"
            InlineKeyboardButton backButton = new InlineKeyboardButton();
            backButton.setText("Indietro");
            backButton.setCallbackData("prev_page_" + (page - 1));  // Pagina precedente
            paginationRow.add(backButton);
        }

        if (end < recipes.size()) {
            // Pulsante "Avanti"
            InlineKeyboardButton nextButton = new InlineKeyboardButton();
            nextButton.setText("Avanti");
            nextButton.setCallbackData("next_page_" + (page + 1));  // Pagina successiva
            paginationRow.add(nextButton);
        }

        // Aggiungi i pulsanti di paginazione
        if (!paginationRow.isEmpty())
            rows.add(paginationRow);

        keyboardMarkup.setKeyboard(rows);

        // Ottieni il messageId dal map usando il chatId
        Integer messageId = userMessageIds.get(chatId);

        if (messageId != null) {
            // Modifica il messaggio esistente
            EditMessageText editMessageText = new EditMessageText();
            editMessageText.setChatId(chatId);
            editMessageText.setMessageId(messageId);
            editMessageText.setText("Ricette trovate: ");
            editMessageText.setReplyMarkup(keyboardMarkup);

            try {
                execute(editMessageText);  // Modifica il messaggio esistente
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    //Reindirizza alla funzione updatePageRecipeButtonns
    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        String callbackData = callbackQuery.getData();
        Long chatId = callbackQuery.getMessage().getChatId();

        int page = Integer.parseInt(callbackData.split("_")[2]);
        updatePagedRecipeButtons(chatId, ricetteUtente.get(chatId), page);
    }

    //
    //INFO RICETTE
    //

    //Invia info ricetta
    private void sendInfoRicetta(CallbackQuery callbackQuery) {

        Long chatId = callbackQuery.getMessage().getChatId();
        int index = Integer.parseInt(callbackQuery.getData().substring(7));     //callbackData = "recipe_" + n --> index_ = 7 caratteri
        String nomeRicetta = ricetteUtente.get(chatId).get(index);

        Database.insertVisitati(Database.getIdRicetta(nomeRicetta), chatId);

        ArrayList<Object> infoRicetta = Database.getInfoRicetta(nomeRicetta);

        String linkRicetta = (String) infoRicetta.get(0);
        String linkImmagine = (String) infoRicetta.get(1);
        Float rating = (Float) infoRicetta.get(2);
        String ingredienti = "";

        for(String i : (ArrayList<String>) infoRicetta.getLast())
            ingredienti += "- " + i + "\n";

        String starRating = "⭐".repeat(Math.round(rating)); // Crea una stringa con stelle per il rating

        String messageText = "<b>" + nomeRicetta + "</b>\n\n" +
                "<u>Rating:</u> " + rating + " " + starRating + "\n\n" +
                "<u>Ingredienti:</u>\n" + ingredienti;

        SendPhoto photoMessage = new SendPhoto();
        photoMessage.setChatId(chatId);
        photoMessage.setPhoto(new InputFile(linkImmagine));
        photoMessage.setCaption(messageText);
        photoMessage.setParseMode("HTML");

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        InlineKeyboardButton preferiti = new InlineKeyboardButton();
        if(!Database.preferitiContains(Database.getIdRicetta(nomeRicetta), chatId)){
            preferiti.setText("Salva");
            preferiti.setCallbackData("salva_" + Database.getIdRicetta(nomeRicetta));
        }else{
            preferiti.setText("Rimuovi");
            preferiti.setCallbackData("rimuovi_" + Database.getIdRicetta(nomeRicetta));
        }

        InlineKeyboardButton preparazione = new InlineKeyboardButton();
        preparazione.setText("Preparazione");
        preparazione.setCallbackData("preparazione_" + index);

        InlineKeyboardButton link = new InlineKeyboardButton();
        link.setText("Vai al sito");
        link.setUrl(linkRicetta);

        InlineKeyboardButton indietro = new InlineKeyboardButton();
        indietro.setText("Indietro");
        indietro.setCallbackData("ricerca");

        rows.add(new ArrayList<>());
        rows.get(0).add(preferiti);
        rows.get(0).add(preparazione);

        rows.add(new ArrayList<>());
        rows.get(1).add(link);

        rows.add(new ArrayList<>());
        rows.get(2).add(indietro);

        keyboardMarkup.setKeyboard(rows);
        photoMessage.setReplyMarkup(keyboardMarkup);


        try {
            Message m = execute(photoMessage);
            if(userMessageIds.get(chatId) != null)
                deleteMessage(chatId, userMessageIds.get(chatId));

            userMessageIds.put(chatId, m.getMessageId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //
    //PREPARAZIONE PASSO PASSO
    //

    //Passaggi passo-passo per la preparazione della ricetta
    private void preparazione(CallbackQuery callbackQuery) {
        Long chatId = callbackQuery.getMessage().getChatId();
        int index = Integer.parseInt(callbackQuery.getData().substring(13)); //preparazione_ --> 13
        String nomeRicetta = ricetteUtente.get(chatId).get(index);

        passaggi.put(chatId, Database.getInfoPreparazione(nomeRicetta));

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        InlineKeyboardButton avanti = new InlineKeyboardButton();
        avanti.setText("Avanti");
        avanti.setCallbackData("avanti_" + 0);

        rows.add(new ArrayList<>());
        rows.get(0).add(avanti);

        keyboardMarkup.setKeyboard(rows);

        ArrayList<String> p = passaggi.get(chatId).get(0);

        SendPhoto photo = new SendPhoto();
        photo.setChatId(chatId);

        if(p.get(2).isEmpty())
            photo.setPhoto(new InputFile(p.get(1)));
        else {
            try {
                photo.setPhoto(new InputFile(mergeImages(p.get(1), p.get(2), p.get(3))));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        photo.setCaption(p.get(0));

        if(0 < p.size()-1)
            photo.setReplyMarkup(keyboardMarkup);
        else{
            // Pulsante "Fine"
            InlineKeyboardButton fine = new InlineKeyboardButton();
            fine.setText("Fine");
            fine.setCallbackData("fine");  // Pagina successiva
            rows.getLast().add(fine);
        }

        try {
            Message m = execute(photo);
            deleteMessage(chatId, userMessageIds.get(chatId));
            userMessageIds.put(chatId, m.getMessageId());
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void preparazioneNext(CallbackQuery callbackQuery) {
        Long chatId = callbackQuery.getMessage().getChatId();
        int index;

        if(callbackQuery.getData().contains("avanti_"))
            index = Integer.parseInt(callbackQuery.getData().substring(7)) + 1; //avanti_ --> 7
        else
            index = Integer.parseInt(callbackQuery.getData().substring(9)) - 1; //indietro_ --> 9

        if(index == 0){
            callbackQuery.setData("preparazione_0");
            preparazione(callbackQuery);
            return;
        }

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        InlineKeyboardButton avanti = new InlineKeyboardButton();
        avanti.setText("Avanti");
        avanti.setCallbackData("avanti_" + index);

        InlineKeyboardButton indietro = new InlineKeyboardButton();
        indietro.setText("Indietro");
        indietro.setCallbackData("indietro_" + index);

        rows.add(new ArrayList<>());
        rows.get(0).add(indietro);
        if(index < passaggi.get(chatId).size()-1)
            rows.get(0).add(avanti);
        else{
            // Pulsante "Fine"
            InlineKeyboardButton fine = new InlineKeyboardButton();
            fine.setText("Fine");
            fine.setCallbackData("fine");  // Pagina successiva
            rows.getLast().add(fine);
        }

        keyboardMarkup.setKeyboard(rows);

        ArrayList<String> p = passaggi.get(chatId).get(index);

        SendPhoto photo = new SendPhoto();
        photo.setChatId(chatId);

        if(p.get(2).isEmpty())
            photo.setPhoto(new InputFile(p.get(1)));
        else {
            try {
                photo.setPhoto(new InputFile(mergeImages(p.get(1), p.get(2), p.get(3))));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        photo.setCaption(p.get(0));
        photo.setReplyMarkup(keyboardMarkup);

        try {
            Message m = execute(photo);
            deleteMessage(chatId, userMessageIds.get(chatId));
            userMessageIds.put(chatId, m.getMessageId());
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    //Creazione di un'unica immagine date le 3 immagini della preparazione del passaggio della ricetta
    private static File mergeImages(String imgPath1, String imgPath2, String imgPath3) throws IOException {
        // Carica le immagini
        BufferedImage img1 = ImageIO.read(new URL(imgPath1));
        BufferedImage img2 = ImageIO.read(new URL(imgPath2));
        BufferedImage img3 = ImageIO.read(new URL(imgPath3));

        // Calcola le dimensioni finali (somma delle larghezze, altezza massima)
        int totalWidth = img1.getWidth() + img2.getWidth() + img3.getWidth();
        int maxHeight = Math.max(img1.getHeight(), Math.max(img2.getHeight(), img3.getHeight()));

        // Crea un'immagine vuota con le dimensioni calcolate
        BufferedImage combinedImage = new BufferedImage(totalWidth, maxHeight, BufferedImage.TYPE_INT_RGB);

        // Disegna le immagini una accanto all'altra
        Graphics g = combinedImage.getGraphics();
        g.drawImage(img1, 0, 0, null); // Prima immagine
        g.drawImage(img2, img1.getWidth(), 0, null); // Seconda immagine
        g.drawImage(img3, img1.getWidth() + img2.getWidth(), 0, null); // Terza immagine
        g.dispose();

        // Crea un file temporaneo
        File tempFile = File.createTempFile("merged_image", ".jpg");

        // Scrivi l'immagine combinata nel file
        ImageIO.write(combinedImage, "jpg", tempFile);

        // Ritorna il file appena creato
        return tempFile;
    }

    //
    //ELIMINAZIONE MESSAGGI
    //

    //Elimina messaggio
    private void deleteMessage(Long chatId, Integer messageId) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(chatId);
        deleteMessage.setMessageId(messageId);

        try {
            execute(deleteMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
