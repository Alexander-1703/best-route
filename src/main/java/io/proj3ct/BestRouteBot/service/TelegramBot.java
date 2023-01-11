package io.proj3ct.BestRouteBot.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import io.proj3ct.BestRouteBot.config.BotConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private static final String HELP_TEXT = """
            Этот бот создан, чтобы помочь тебе построить удачный маршрут.
            Ты можешь найти самый быстрый, самый дешевый или оптимальный маршрут, выбрав необходимые параметры в настройках. В них же можно задать точку отправления и точку назначения.

            /changesettings - настройка параметров маршрута и его поиска. Выбор точки отправления и назначения и фильтр, по которому будет выполнен поиск лучшего маршрута.

            /settings - просмотр текущих настроек.

            /find - выполнение поиска по заданным настройкам""";
    private final BotConfig config;

    public TelegramBot(BotConfig config) {
        this.config = config;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "получить приветственное сообщение"));
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
                case "/start":
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case "/help":
                    sendMessage(chatId, HELP_TEXT);
                    break;
                default:
                    sendMessage(chatId, "Извините, такой команды не существует");
                    break;
            }
        }
    }

    private void startCommandReceived(long chatId, String firstName) {
        String answer = "Привет, " + firstName + "! Я бот, который поможет тебе построить наилучший маршрут";
        log.info("Replied to user " + firstName);
        sendMessage(chatId, answer);
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }
}
