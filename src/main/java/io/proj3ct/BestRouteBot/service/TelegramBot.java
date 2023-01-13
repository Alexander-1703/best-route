package io.proj3ct.BestRouteBot.service;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.vdurmont.emoji.EmojiParser;

import io.proj3ct.BestRouteBot.config.BotConfig;
import io.proj3ct.BestRouteBot.model.User;
import io.proj3ct.BestRouteBot.model.UserRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private static final String MENU_BUTTON = "main menu button";
    private static final String SETTINGS_BUTTON = "settings button";
    private static final String FIND_BUTTON = "find button";
    private static final String RETURN_TO_MAIN_MENU = "return to main menu";
    private static final String HELP_BUTTON = "help button";
    private static final String DEPARTURE_BUTTON = "departure button";
    private static final String DESTINATION_BUTTON = "destination button";
    private static final String DEPARTURE_DATE_BUTTON = "departure date button";
    private static final String COMMAND_DOESNT_EXIST = "Извините, такой команды не существует";
    private static final String HELP_TEXT = """
            Этот бот создан, чтобы помочь тебе построить удачный маршрут.
            Ты можешь найти самый быстрый, самый дешевый или оптимальный маршрут, выбрав необходимые параметры в настройках. В них же можно задать  точку отправления и назначения.

            /changesettings - настройка параметров маршрута и его поиска. Выбор точки отправления и назначения и фильтр, по которому будет выполнен поиск лучшего маршрута.

            /settings - просмотр текущих настроек.

            /find - выполнение поиска по заданным настройкам
                        
            /menu - главное меню
                        
            """;

    private final BotConfig config;
    @Autowired
    private UserRepository userRepository;

    public TelegramBot(BotConfig config) {
        this.config = config;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "получить приветственное сообщение"));
        listOfCommands.add(new BotCommand("/menu", "открыть главное меню"));
        listOfCommands.add(new BotCommand("/settings", "настройки маршрута"));
        listOfCommands.add(new BotCommand("/changesettings", "изменить текущие настройки"));
        listOfCommands.add(new BotCommand("/find", "поиск подходящего маршрута"));
        listOfCommands.add(new BotCommand("/help", "справка об использовании бота"));

        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Bot command list setup error");
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
                case "/start" -> {
                    registerUser(update.getMessage());
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                }
                case "/menu" -> sendMessage(chatId, menuText(), menu());
                case "/help" -> sendMessage(chatId, HELP_TEXT);
                default -> sendMessage(chatId, COMMAND_DOESNT_EXIST);
            }
            log.info("Message received from user: " + update.getMessage().getChat().getFirstName());
        } else if (update.hasCallbackQuery()) {
            int messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            String callbackData = update.getCallbackQuery().getData();

            switch (callbackData) {
                case MENU_BUTTON -> {
                    DeleteMessage deleteMessage = new DeleteMessage();
                    deleteMessage.setChatId(chatId);
                    deleteMessage.setMessageId(messageId);
                    executeChecked(deleteMessage);
                    sendMessage(chatId, menuText(), menu());
                }
                case SETTINGS_BUTTON -> {
                    EditMessageText editMessage = new EditMessageText();
                    editMessage.setChatId(chatId);
                    editMessage.setMessageId(messageId);
                    editMessage.setText(menuText());
                    editMessage.setReplyMarkup(settingsMenu());
                    executeChecked(editMessage);
                }
                case FIND_BUTTON -> {
                    EditMessageText editMessage = new EditMessageText();
                    editMessage.setChatId(chatId);
                    editMessage.setMessageId(messageId);
                    //searchBestRouteMethod();
                    editMessage.setText("Тут должен выполняться алгоритм поиска оптимального маршрута, но пока его нет(");
                    editMessage.setReplyMarkup(
                            oneInLineButton(EmojiParser.parseToUnicode(":arrow_left: Назад"), RETURN_TO_MAIN_MENU));
                    executeChecked(editMessage);
                }
                case RETURN_TO_MAIN_MENU -> {
                    EditMessageText editMessage = new EditMessageText();
                    editMessage.setChatId(chatId);
                    editMessage.setMessageId(messageId);
                    editMessage.setText(menuText());
                    editMessage.setReplyMarkup(menu());
                    executeChecked(editMessage);
                }
                case HELP_BUTTON -> {
                    EditMessageText editMessage = new EditMessageText();
                    editMessage.setChatId(chatId);
                    editMessage.setMessageId(messageId);
                    editMessage.setText(HELP_TEXT);
                    editMessage.setReplyMarkup(
                            oneInLineButton(EmojiParser.parseToUnicode(":arrow_left: Назад"), RETURN_TO_MAIN_MENU));
                    executeChecked(editMessage);
                }
            }
            log.info("Callback data received from user: " + update.getMessage().getChat().getFirstName());
        }
    }

    private InlineKeyboardMarkup settingsMenu() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        InlineKeyboardButton departureButton = new InlineKeyboardButton();
        departureButton.setText("Отправление");
        departureButton.setCallbackData(DEPARTURE_BUTTON);
        rowInLine.add(departureButton);
        rowsInLine.add(rowInLine);

        rowInLine = new ArrayList<>();
        InlineKeyboardButton destinationButton = new InlineKeyboardButton();
        destinationButton.setText("Прибытие");
        destinationButton.setCallbackData(DESTINATION_BUTTON);
        rowInLine.add(destinationButton);
        rowsInLine.add(rowInLine);

        rowInLine = new ArrayList<>();
        InlineKeyboardButton departureDateButton = new InlineKeyboardButton();
        departureDateButton.setText("Дата отправления");
        departureDateButton.setCallbackData(DEPARTURE_DATE_BUTTON);
        rowInLine.add(departureDateButton);
        rowsInLine.add(rowInLine);

        rowInLine = new ArrayList<>();
        InlineKeyboardButton returnButton = new InlineKeyboardButton();
        returnButton.setText(EmojiParser.parseToUnicode(":arrow_left: Назад"));
        returnButton.setCallbackData(RETURN_TO_MAIN_MENU);
        rowInLine.add(returnButton);
        rowsInLine.add(rowInLine);

        inlineKeyboardMarkup.setKeyboard(rowsInLine);
        return inlineKeyboardMarkup;
    }

    private InlineKeyboardMarkup oneInLineButton(String title, String id) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();

        var button = new InlineKeyboardButton();
        button.setText(title);
        button.setCallbackData(id);
        rowInLine.add(button);
        rowsInLine.add(rowInLine);

        inlineKeyboardMarkup.setKeyboard(rowsInLine);
        return inlineKeyboardMarkup;
    }

    private void startCommandReceived(long chatId, String firstName) {
        String answer = EmojiParser.parseToUnicode("Привет, " + firstName +
                "! Я бот, который поможет тебе построить наилучший маршрут :blush:");

        InlineKeyboardMarkup inlineKeyboardMarkup = oneInLineButton("Меню", MENU_BUTTON);

        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);
        sendPhoto.setCaption(answer);
        sendPhoto.setReplyMarkup(inlineKeyboardMarkup);
        //поменять на относительный путь или url
        sendPhoto.setPhoto(new InputFile(
                new File("D:\\JavaProjects\\BestRoute\\src\\main\\resources\\pictures\\map.jpg")
        ));
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }

        log.info("Replied to user " + firstName);
    }

    private void sendMessage(long chatId, String textToSend, InlineKeyboardMarkup inlineKeyboardMarkup) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        if (inlineKeyboardMarkup != null) {
            message.setReplyMarkup(inlineKeyboardMarkup);
        }

        executeChecked(message);
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        executeChecked(message);
    }

    private void registerUser(Message msg) {
        if (userRepository.findById(msg.getChatId()).isEmpty()) {
            var chatId = msg.getChatId();
            var chat = msg.getChat();

            User user = new User();
            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());

            userRepository.save(user);
            log.info("User saved: " + user);
        }
    }

    private InlineKeyboardMarkup menu() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();

        InlineKeyboardButton settingsButton = new InlineKeyboardButton();
        String settings = EmojiParser.parseToUnicode(":gear: Настройки");
        settingsButton.setText(settings);
        settingsButton.setCallbackData(SETTINGS_BUTTON);
        rowInLine.add(settingsButton);
        rowsInLine.add(rowInLine);

        rowInLine = new ArrayList<>();
        InlineKeyboardButton findButton = new InlineKeyboardButton();
        String find = EmojiParser.parseToUnicode(":mag: Найти");
        findButton.setText(find);
        findButton.setCallbackData(FIND_BUTTON);
        rowInLine.add(findButton);
        rowsInLine.add(rowInLine);

        rowInLine = new ArrayList<>();
        InlineKeyboardButton helpButton = new InlineKeyboardButton();
        String help = EmojiParser.parseToUnicode(":information_source: Справка");
        helpButton.setText(help);
        helpButton.setCallbackData(HELP_BUTTON);
        rowInLine.add(helpButton);
        rowsInLine.add(rowInLine);

        inlineKeyboardMarkup.setKeyboard(rowsInLine);
        return inlineKeyboardMarkup;
    }

    private String menuText() {
        //параметры должны приходить на вход извне
        return "Ты можешь настроить параметры маршрута, нажав на кнопку \"Настройки\".\n\n" +
                "Текущие настройки:\n\n" +
                "Отправление: Орск\n" +
                "Прибытие: Санкт-Петербург\n" +
                "Дата: 29.01.2023";
    }

    private <T extends Serializable, Method extends BotApiMethod<T>> void executeChecked(Method method) {
        try {
            execute(method);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }
}
