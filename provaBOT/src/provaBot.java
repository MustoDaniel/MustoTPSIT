import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class provaBot extends TelegramLongPollingBot {

    @Override
    public String getBotUsername() {
        return "Echo bot";
    }

    @Override
    public String getBotToken() {
        return "7944870754:AAGCn9y5N3XS-KhMZzKSgjVczZd9cs8NE1E";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            // Handle /timer command
            if (messageText.startsWith("/timer")) {
                String[] parts = messageText.split(" ");
                if (parts.length == 2) {
                    try {
                        int seconds = Integer.parseInt(parts[1]);
                        sendMessage(chatId, "⏳ Timer started for " + seconds + " seconds!");

                        // Schedule the timer to send an alert after the specified seconds
                        Executors.newSingleThreadScheduledExecutor().schedule(() -> {
                            try {
                                sendAlertWithButton(chatId, "⏰ Time's up! 🚨");
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                        }, seconds, TimeUnit.SECONDS);
                    } catch (NumberFormatException e) {
                        sendMessage(chatId, "❌ Invalid time format. Please use /timer <seconds>.");
                    }
                } else {
                    sendMessage(chatId, "❌ Usage: /timer <seconds>");
                }
            }
        }

        // Handle callback query (button press)
        if (update.hasCallbackQuery()) {
            String callbackQueryId = update.getCallbackQuery().getId();
            String callbackData = update.getCallbackQuery().getData();

            if (callbackData.equals("acknowledge_timer")) {
                // Respond to button press
                AnswerCallbackQuery answer = new AnswerCallbackQuery();
                answer.setCallbackQueryId(callbackQueryId);
                answer.setText("Thank you for acknowledging the timer!");
                answer.setShowAlert(true);  // Show a pop-up alert in Telegram
                try {
                    execute(answer); // Send the callback query response
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        try {
            execute(message); // Send the message
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendAlertWithButton(long chatId, String text) throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);

        // Enable sound notifications (this triggers the phone's sound alert)
        message.enableNotification(); // Enables push notification sound

        // Create inline keyboard with a button
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Acknowledge Timer");
        button.setCallbackData("acknowledge_timer"); // Unique callback data

        row.add(button);
        rows.add(row);
        inlineKeyboardMarkup.setKeyboard(rows);
        message.setReplyMarkup(inlineKeyboardMarkup);

        execute(message); // Send message with inline button
    }
}
