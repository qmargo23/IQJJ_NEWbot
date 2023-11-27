package io.project.TelegramBot_IQJJ_NEWbot.service;

import io.project.TelegramBot_IQJJ_NEWbot.config.BotConfig;
import io.project.TelegramBot_IQJJ_NEWbot.model.User;
import io.project.TelegramBot_IQJJ_NEWbot.model.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {
    @Autowired
    private UserRepository userRepository;
    BotConfig config;
    static final String HELP_TEXT = "This bot is created to demonstrate Spring capabilities.\n\n" +
            "You can execute commands from the main menu on the left or by typing a command:\n\n" +
            "Type /start to see a welcome message\n\n" +
            "Type /mydata to see data stored about yourself\n\n" +
            "Type /deletedata to delete data stored about yourself\n\n" +
            "Type /register  to register yourself\n\n" +
            "Type /settings to see settings\n\n" +
            "Type /help to see this message again";

    public TelegramBot(BotConfig config) {
        this.config = config;
        List<BotCommand> listofCommands = new ArrayList<>();
        listofCommands.add(new BotCommand("/start", "get a welcome message"));
        listofCommands.add(new BotCommand("/register", "to register yourself"));
//        listofCommands.add(new BotCommand("/mydata", "get your data stored"));
//        listofCommands.add(new BotCommand("/deletedata", "delete my data"));
        listofCommands.add(new BotCommand("/help", "info how to use this bot"));
        listofCommands.add(new BotCommand("/settings", "set your preferences"));
        try {
            this.execute(new SetMyCommands(listofCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            switch (messageText) {
                case "/start":
                    registerUser(update.getMessage());
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case "/help":
                    sendMessage(chatId, HELP_TEXT);
                    break;
                    // подключим кнопки к сообщению
                case "/register":
                    register(chatId);
                    break;
                default:
                    sendMessage(chatId, "Sorry... I don't understand you");
            }
        }
        //если в update передали id  кнопки
         else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            //получим id сообщения - getMessageId()
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            //getChatId() только через  getCallbackQuery()
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            //проверяем какую кнопку нажали
            if(callbackData.equals("YES_BUTTON")){
                String text = "You pressed YES button";

                EditMessageText message = new EditMessageText();
                message.setChatId(String.valueOf(chatId));
                message.setText(text);
                message.setMessageId((int)messageId);

                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    log.error("Error occurred: " + e.getMessage());
                }
                //executeEditMessageText(text, chatId, messageId);
            }
            else if(callbackData.equals("NO_BUTTON" )){
                String text = "You pressed NO button";

                EditMessageText message = new EditMessageText();
                message.setChatId(String.valueOf(chatId));
                message.setText(text);
                message.setMessageId((int)messageId);

                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    log.error("Error occurred: " + e.getMessage());
                }
                //executeEditMessageText(text, chatId, messageId);
            }
        }
    }

    private void startCommandReceived(long chatId, String name) {
        String answer = "Hello, " + name + ", nice to meet you!";
        log.info("Replied to user " + name);
        sendMessage(chatId, answer);
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        //подключаем кнопки-клавиатуру
//        ReplyKeyboardMarkup keyboardMarkup = getKeyboardMarkup();
//        message.setReplyMarkup(keyboardMarkup);
//        executeMessage(message);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    private  ReplyKeyboardMarkup getKeyboardMarkup() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        //делаем кнопки поменьше
        keyboardMarkup.setResizeKeyboard(true);

        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("weather");
        row.add("get random joke");
        //первая строка
        keyboardRows.add(row);

        row = new KeyboardRow();
        row.add("register");
        row.add("check my data");
        row.add("delete my data");
        //вторая строка
        keyboardRows.add(row);
        //добавляем  в keyboardMarkup строки
        keyboardMarkup.setKeyboard(keyboardRows);
        return keyboardMarkup;
    }

    private void registerUser(Message msg) {

        if (userRepository.findById(msg.getChatId()).isEmpty()) {

            var chatId = msg.getChatId();
            var chat = msg.getChat();

            User user = new User();

            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());

            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            userRepository.save(user);
            log.info("user saved: " + user);
        }
    }

    private void register(long chatId) {

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        //текст сообщения
        message.setText("Do you really want to register?");

        // класс InlineKeyboardMarkup
        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        //создаем список списков, в котором сохраним кнопки
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        //создаем (пока)2 кнопки ряда
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        //создаем кнопку yesButton
        var yesButton = new InlineKeyboardButton();

        yesButton.setText("Yes");
        //setCallbackData - идентификатор , показывающий что нажата именно эта конпка
        //лучше сделать, чтобы id была КОНСТАНТОЙ: YES_BUTTON
        yesButton.setCallbackData("YES_BUTTON");

        var noButton = new InlineKeyboardButton();

        noButton.setText("No");
        noButton.setCallbackData("NO_BUTTON");

        rowInLine.add(yesButton);
        rowInLine.add(noButton);

        // добавляем в том порядке в которой добавляем
        rowsInLine.add(rowInLine);

        //
        markupInLine.setKeyboard(rowsInLine);
        message.setReplyMarkup(markupInLine);

      //  executeMessage(message);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }


}
