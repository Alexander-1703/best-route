package io.proj3ct.BestRouteBot.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
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

import com.vdurmont.emoji.EmojiParser;

import io.proj3ct.BestRouteBot.config.BotConfig;
import io.proj3ct.BestRouteBot.model.User;
import io.proj3ct.BestRouteBot.model.UserRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private static final String MENU_BUTTON = "main_menu_button";
    private static final String SETTINGS_BUTTON = "settings_button";
    private static final String FIND_BUTTON = "find_button";

    private static final String HELP_TEXT = """
            Этот бот создан, чтобы помочь тебе построить удачный маршрут.
            Ты можешь найти самый быстрый, самый дешевый или оптимальный маршрут, выбрав необходимые параметры в настройках. В них же можно задать  точку отправления и назначения.

            /changesettings - настройка параметров маршрута и его поиска. Выбор точки отправления и назначения и фильтр, по которому будет выполнен поиск лучшего маршрута.

            /settings - просмотр текущих настроек.

            /find - выполнение поиска по заданным настройкам""";
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
            log.error("Ошибка настройки списка команд ботов");
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
                    startCommandReceived(chatId, update.getMessage());
                }
                case "/menu" -> sendMessage(chatId, menuText(), menu());
                case "/help" -> sendMessage(chatId, HELP_TEXT);
                default -> sendMessage(chatId, "Извините, такой команды не существует");
            }
        } else if (update.hasCallbackQuery()) {
            int messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            String callbackData = update.getCallbackQuery().getData();

            EditMessageText editMessage = new EditMessageText();
            editMessage.setChatId(chatId);
            editMessage.setMessageId(messageId);

            switch (callbackData) {
                case MENU_BUTTON -> {
                    editMessage.setText(menuText());
                    editMessage.setReplyMarkup(menu());
                }
                case SETTINGS_BUTTON -> {
                    editMessage.setText("Settings");
//                    editMessage.setReplyMarkup(menu());
                }
                case FIND_BUTTON -> {
                    editMessage.setText("Find");
//                    editMessage.setReplyMarkup(menu());
                }
            }

            executeChecked(editMessage);
        }
    }

    private void startCommandReceived(long chatId, Message message) {
        String firstName = message.getChat().getFirstName();
        String answer = EmojiParser.parseToUnicode("Привет, " + firstName +
                "! Я бот, который поможет тебе построить наилучший маршрут :blush:");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();

        var menuButton = new InlineKeyboardButton();
        menuButton.setText("Menu");
        menuButton.setCallbackData(MENU_BUTTON);
        rowInLine.add(menuButton);
        rowsInLine.add(rowInLine);

        inlineKeyboardMarkup.setKeyboard(rowsInLine);

        log.info("Replied to user " + firstName);
        sendMessage(chatId, answer, inlineKeyboardMarkup);
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

    private <T extends Serializable, Method extends BotApiMethod<T>> void executeChecked(Method method) {
        try {
            execute(method);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
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
            log.info("Пользователь сохранён: " + user);
        }
    }

    private InlineKeyboardMarkup menu() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();

        InlineKeyboardButton settingsButton = new InlineKeyboardButton();
        String settings = EmojiParser.parseToUnicode(":gear: Settings");
        settingsButton.setText(settings);
        settingsButton.setCallbackData(SETTINGS_BUTTON);
        rowInLine.add(settingsButton);
        rowsInLine.add(rowInLine);

        rowInLine = new ArrayList<>();
        InlineKeyboardButton findButton = new InlineKeyboardButton();
        String find = EmojiParser.parseToUnicode(":mag: Find");
        findButton.setText(find);
        findButton.setCallbackData(FIND_BUTTON);
        rowInLine.add(findButton);
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
}
