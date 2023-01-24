package io.proj3ct.BestRouteBot.controller.service;

import java.io.Serializable;
import java.time.LocalDate;
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
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.vdurmont.emoji.EmojiParser;

import io.proj3ct.BestRouteBot.controller.config.BotConfig;
import io.proj3ct.BestRouteBot.controller.parser.Parser;
import io.proj3ct.BestRouteBot.controller.parser.pages.TicketsPage.Ticket;
import io.proj3ct.BestRouteBot.controller.parser.pages.searchPage.TripType;
import io.proj3ct.BestRouteBot.model.CityRepository;
import io.proj3ct.BestRouteBot.model.User;
import io.proj3ct.BestRouteBot.model.UserRepository;
import io.proj3ct.BestRouteBot.view.CreateButtons;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private static final String MENU_BUTTON = "main menu button";
    private static final String SETTINGS_BUTTON = "settings button";
    private static final String SEARCH_BUTTON = "search menu button";
    private static final String FIND_FASTEST = "fastest route";
    private static final String FIND_CHEAPEST = "cheapest route";
    private static final String RETURN_TO_MAIN_MENU = "return to main menu";
    private static final String HELP_BUTTON = "help button";
    private static final String DEPARTURE_BUTTON = "departure button";
    private static final String DESTINATION_BUTTON = "destination button";
    private static final String DEPARTURE_DATE_BUTTON = "departure date button";
    private static final String COMMAND_DOESNT_EXIST = "Извините, данной команды не существует";
    private static final String ERROR_OCCURRED = "Error occurred: ";
    private static final String RETURN_BUTTON_TEXT = ":arrow_left: Назад";
    private static final String NO_DATA = "Не указано";
    private static final String WHICH_SEARCH = "Какой маршрут искать?";
    private static final String HELP_TEXT = """
            Этот бот создан, чтобы помочь тебе построить удачный маршрут.
            Ты можешь найти самый быстрый, самый дешевый или оптимальный маршрут, выбрав необходимые параметры в настройках. В них же можно задать  точку отправления и назначения.

            /settings - просмотр текущих настроек.

            /find - выполнение поиска по заданным настройкам
                        
            /menu - главное меню
                        
            """;

    private final BotConfig config;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CityRepository cityRepository;
    private boolean isInSettingsDeparture = false;
    private boolean isInSettingsDestination = false;
    private boolean isInSettingsDate = false;

    public TelegramBot(BotConfig config) {
        this.config = config;
        List<BotCommand> listOfCommands = List.of(
                new BotCommand("/start", "получить приветственное сообщение"),
                new BotCommand("/menu", "открыть главное меню"),
                new BotCommand("/settings", "настройки маршрута"),
                new BotCommand("/find", "поиск подходящего маршрута"),
                new BotCommand("/help", "справка об использовании бота"));

        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Bot command list setup error");
        }

    }

    public static String universalCapitalize(String string) {
        string = string.toLowerCase();
        if (string.contains("-") || string.contains(" ")) {
            String symbol = string.contains("-") ? "-" : " ";
            String[] strArr = string.split(symbol);
            StringBuilder sb = new StringBuilder();
            for (String str : strArr) {
                sb.append(capitalize(str)).append(symbol);
            }
            return sb.substring(0, sb.length() - 1);
        }
        return capitalize(string);
    }

    public static String capitalize(String str) {
        if (str == null || str.length() <= 1) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
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

            if (messageText.contains("/send") && config.getOwnerId() == chatId) {
                var textToSend = EmojiParser.parseToUnicode(messageText.substring(messageText.indexOf(" ")));
                var users = userRepository.findAll();
                for (User user : users) {
                    SendMessage message = MessageUtil.sendMessage(user.getChatId(), textToSend);
                    executeChecked(message);
                }
                return;
            }

            switch (messageText) {
                case "/start" -> {
                    registerUser(update.getMessage());
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                }
                case "/menu" -> {
                    SendMessage message = MessageUtil.sendMessage(chatId, menuText(chatId), menu());
                    executeChecked(message);
                }
                case "/settings" -> {
                    SendMessage message = MessageUtil.sendMessage(chatId, menuText(chatId), settingsMenu());
                    executeChecked(message);
                }
                case "/find" -> {
                    InlineKeyboardMarkup inlineKeyboardMarkup = searchMenuMarkup();
                    SendMessage message = MessageUtil.sendMessage(chatId, WHICH_SEARCH, inlineKeyboardMarkup);
                    executeChecked(message);
                }
                case "/help" -> {
                    SendMessage message = MessageUtil.sendMessage(chatId, HELP_TEXT);
                    executeChecked(message);
                }
                default -> wordProcessing(update, chatId);
            }
            isInSettingsDeparture = false;
            isInSettingsDestination = false;
            isInSettingsDate = false;
            log.info("Message received from user: " + update.getMessage().getChat().getFirstName());
        } else if (update.hasCallbackQuery()) {
            callBackResponse(update);
        }
    }

    private void wordProcessing(Update update, long chatId) {
        String msg = update.getMessage().getText();
        SendMessage message;
        if ((isInSettingsDeparture || isInSettingsDestination) && !isCityExist(msg)) {
            isInSettingsDestination = false;
            isInSettingsDeparture = false;
            message = MessageUtil.sendMessage(chatId, "Вы ввели несуществующий город");
            executeChecked(message);
            message = MessageUtil.sendMessage(chatId, menuText(chatId), settingsMenu());
            executeChecked(message);
            log.info("introduced a non-existent city: " + msg);
            return;
        }

        User user = getUser(chatId);

        String dest = user.getDestination();
        String dep = user.getDeparture();
        if ((isInSettingsDeparture && dest != null && (dest.equals(universalCapitalize(msg)))) ||
                isInSettingsDestination && dep != null && (dep.equals(universalCapitalize(msg)))) {
            isInSettingsDestination = false;
            isInSettingsDeparture = false;
            message = MessageUtil.sendMessage(chatId, "Города отправления и прибытия совпадают");
            executeChecked(message);
            message = MessageUtil.sendMessage(chatId, menuText(chatId), settingsMenu());
            executeChecked(message);
            log.info("Departure and arrival cities are the same: " + msg);
            return;
        }

        if (isInSettingsDeparture) {
            isInSettingsDeparture = false;
            log.info("The user " + update.getMessage().getChat().getFirstName() +
                    " entered the departure city: " + msg);
            user.setDeparture(universalCapitalize(msg));
            userRepository.save(user);
        } else if (isInSettingsDestination) {
            isInSettingsDestination = false;
            log.info("The user " + update.getMessage().getChat().getFirstName() +
                    " entered the destination city: " + msg);
            user.setDestination(universalCapitalize(msg));
            userRepository.save(user);
        } else if (isInSettingsDate) {
            isInSettingsDate = false;
            log.info("The user " + update.getMessage().getChat().getFirstName() +
                    " entered the date of departure: " + msg);
            LocalDate date;
            try {
                date = LocalDate.parse(msg);
            } catch (Exception e) {
                log.error(ERROR_OCCURRED + e.getMessage());
                message = MessageUtil.sendMessage(chatId, "Дата введена некорректно, используйте формат yyyy-mm-dd");
                executeChecked(message);
                message = MessageUtil.sendMessage(chatId, menuText(chatId), settingsMenu());
                executeChecked(message);
                return;
            }

            LocalDate nowDate = LocalDate.now();
            LocalDate maxDate = nowDate.plusYears(1);
            if (nowDate.isAfter(date) ||
                    !date.isBefore(LocalDate.of(maxDate.getYear(), maxDate.getMonth(), 1))) {
                message = MessageUtil.sendMessage(chatId, "Эту дату выбрать нельзя. Дата должна быть больше текущей " +
                        "и не больше +11 месяцев от текущего месяца");
                executeChecked(message);
                message = MessageUtil.sendMessage(chatId, menuText(chatId), settingsMenu());
                executeChecked(message);
                return;
            }
            user.setDate(date);
            userRepository.save(user);
        } else {
            message = MessageUtil.sendMessage(chatId, COMMAND_DOESNT_EXIST);
            executeChecked(message);
            message = MessageUtil.sendMessage(chatId, menuText(chatId), menu());
            executeChecked(message);
            return;
        }
        message = MessageUtil.sendMessage(chatId, menuText(chatId), settingsMenu());
        executeChecked(message);
    }

    private void startCommandReceived(long chatId, String firstName) {
        String answer = EmojiParser.parseToUnicode("Привет, " + firstName +
                "! Я бот, который поможет тебе построить наилучший маршрут :blush:");

        InlineKeyboardMarkup inlineKeyboardMarkup = CreateButtons.oneInLineButton("Меню", MENU_BUTTON);
        String path = "src/main/resources/pictures/map.jpg";
        SendPhoto photo = MessageUtil.sendPhoto(chatId, answer, path, inlineKeyboardMarkup);
        try {
            execute(photo);
        } catch (TelegramApiException e) {
            log.error(ERROR_OCCURRED + e.getMessage());
        }
        log.info("Replied to user " + firstName);
    }

    private void registerUser(Message msg) {
        if (!userRepository.existsById(msg.getChatId())) {
            var chatId = msg.getChatId();
            var chat = msg.getChat();

            User user = new User();
            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());

            userRepository.save(user);
            log.info("User saved: " + user);
        }
    }

    private InlineKeyboardMarkup settingsMenu() {
        String[] titles = new String[]{"Отправление", "Прибытие", "Дата отправления",
                EmojiParser.parseToUnicode(RETURN_BUTTON_TEXT)};
        String[] callBacks = new String[]{DEPARTURE_BUTTON, DESTINATION_BUTTON, DEPARTURE_DATE_BUTTON, RETURN_TO_MAIN_MENU};
        return CreateButtons.inLineButtons(1, 4, titles, callBacks);
    }

    private InlineKeyboardMarkup menu() {
        String[] titles = new String[]{EmojiParser.parseToUnicode(":gear: Настройки"),
                EmojiParser.parseToUnicode(":mag: Найти"),
                EmojiParser.parseToUnicode(":information_source: Справка")};
        String[] callBacks = new String[]{SETTINGS_BUTTON, SEARCH_BUTTON, HELP_BUTTON};
        return CreateButtons.inLineButtons(1, 3, titles, callBacks);
    }

    private String menuText(long chatId) {
        String departure = null;
        String destination = null;
        LocalDate date = null;
        User user = userRepository.findById(chatId).orElse(null);
        if (user != null) {
            departure = user.getDeparture();
            destination = user.getDestination();
            date = user.getDate();
        }
        return "Ты можешь настроить параметры маршрута, нажав на кнопку \"Настройки\".\n\n" +
                "Текущие настройки:\n\n" +
                "Отправление: " + ((departure != null) ? universalCapitalize(departure) : NO_DATA) +
                "\nПрибытие: " + ((destination != null) ? universalCapitalize(destination) : NO_DATA) +
                "\nДата: " + ((date != null) ? date : NO_DATA);
    }

    private <T extends Serializable, Method extends BotApiMethod<T>> void executeChecked(Method method) {
        try {
            execute(method);
        } catch (TelegramApiException e) {
            log.error(ERROR_OCCURRED + e.getMessage());
        }
    }

    private boolean isCityExist(String msg) {
        return cityRepository.existsById(universalCapitalize(msg));
    }

    private void callBackResponse(Update update) {
        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        String callbackData = update.getCallbackQuery().getData();

        switch (callbackData) {
            case MENU_BUTTON -> {
                DeleteMessage deleteMessage = MessageUtil.deleteMessage(chatId, messageId);
                executeChecked(deleteMessage);
                SendMessage message = MessageUtil.sendMessage(chatId, menuText(chatId), menu());
                executeChecked(message);
            }
            case SETTINGS_BUTTON -> {
                EditMessageText editMessage = MessageUtil.editMessage(chatId, messageId, menuText(chatId), settingsMenu());
                executeChecked(editMessage);
            }
            case SEARCH_BUTTON -> {
                InlineKeyboardMarkup inlineKeyboardMarkup = searchMenuMarkup();
                EditMessageText editMessage = MessageUtil.editMessage(
                        chatId, messageId, WHICH_SEARCH, inlineKeyboardMarkup);
                executeChecked(editMessage);
            }
            case FIND_CHEAPEST -> {
                String text = "Поиск самого дешевого маршрута...";
                findRoute(chatId, true);
//                EditMessageText editMessage = MessageUtil.editMessage(chatId, messageId, text,
//                        CreateButtons.oneInLineButton(EmojiParser.parseToUnicode(RETURN_BUTTON_TEXT), SEARCH_BUTTON));
//                executeChecked(editMessage);
            }
            case FIND_FASTEST -> {
                String text = "Поиск самого быстрого маршрута...";
                findRoute(chatId, false);
//                EditMessageText editMessage = MessageUtil.editMessage(chatId, messageId, text,
//                        CreateButtons.oneInLineButton(EmojiParser.parseToUnicode(RETURN_BUTTON_TEXT), SEARCH_BUTTON));
//                executeChecked(editMessage);
            }
            case RETURN_TO_MAIN_MENU -> {
                EditMessageText editMessage = MessageUtil.editMessage(chatId, messageId, menuText(chatId), menu());
                executeChecked(editMessage);
            }
            case HELP_BUTTON -> {
                EditMessageText editMessage = MessageUtil.editMessage(chatId, messageId, HELP_TEXT,
                        CreateButtons.oneInLineButton(EmojiParser.parseToUnicode(RETURN_BUTTON_TEXT), RETURN_TO_MAIN_MENU));
                executeChecked(editMessage);
            }
            case DEPARTURE_BUTTON -> {
                isInSettingsDeparture = true;
                EditMessageText editMessage = MessageUtil.editMessage(chatId, messageId, "Введите город отправления:");
                executeChecked(editMessage);
            }
            case DESTINATION_BUTTON -> {
                isInSettingsDestination = true;
                EditMessageText editMessage = MessageUtil.editMessage(chatId, messageId, "Введите город прибытия:");
                executeChecked(editMessage);
            }
            case DEPARTURE_DATE_BUTTON -> {
                isInSettingsDate = true;
                EditMessageText editMessage = MessageUtil.editMessage(chatId, messageId, "Введите дату отправления в формате yyyy-mm-dd:");
                executeChecked(editMessage);
            }
        }
        log.info("Callback data received from user: " + update.getMessage().getChat().getFirstName());
    }

    private void findRoute(long chatId, boolean isCheapest) {
        User user = getUser(chatId);
        List<Ticket> ticketsList = new Parser().getTickets(user.getDeparture(), user.getDestination(), user.getDate().toString(),
                1, 0, 0, TripType.Economic);
        for (Ticket ticket : ticketsList) {
            String text = "Ссылка: " + ticket.getUrl() +
                    "\n" + ticket.getWayPoints() +
                    "\n" + ticket.getDateStart() + " " + ticket.getTimeStart() +
                    "   " + ticket.getDateEnd() + " " + ticket.getTimeEnd() +
                    "\n" + ticket.getTripTime() +
                    "\n" + ticket.getTransferAmount() +
                    "\n" + ticket.getPrice();
            SendMessage message = MessageUtil.sendMessage(chatId, text);
            executeChecked(message);
        }
    }

    private User getUser(long chatId) {
        User user = userRepository.findById(chatId).orElse(null);
        if (user == null) {
            log.error(ERROR_OCCURRED + "user NPE");
            throw new NullPointerException();
        }
        return user;
    }

    private InlineKeyboardMarkup searchMenuMarkup() {
        String[] titles = new String[]{
                "Быстрее", "Дешевле", EmojiParser.parseToUnicode(RETURN_BUTTON_TEXT)};
        String[] callBacks = new String[]{
                FIND_FASTEST, FIND_CHEAPEST, RETURN_TO_MAIN_MENU};
        return CreateButtons.inLineButtons(1, 3, titles, callBacks);
    }

}
