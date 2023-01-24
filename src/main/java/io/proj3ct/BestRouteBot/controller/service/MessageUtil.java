package io.proj3ct.BestRouteBot.controller.service;

import java.io.File;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageUtil {
    public static SendPhoto sendPhoto(long chatId, String answer, String path, InlineKeyboardMarkup inlineKeyboardMarkup) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);
        sendPhoto.setCaption(answer);
        sendPhoto.setReplyMarkup(inlineKeyboardMarkup);
        sendPhoto.setPhoto(new InputFile(
                new File(path)
        ));
        return sendPhoto;
    }

    public static SendMessage sendMessage(long chatId, String textToSend, InlineKeyboardMarkup inlineKeyboardMarkup) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        if (inlineKeyboardMarkup != null) {
            message.setReplyMarkup(inlineKeyboardMarkup);
        }
        return message;
    }

    public static SendMessage sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        return message;
    }

    public static EditMessageText editMessage(long chatId, int messageId, String text, InlineKeyboardMarkup inlineKeyboardMarkup) {
        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(chatId);
        editMessage.setMessageId(messageId);
        editMessage.setText(text);
        editMessage.setReplyMarkup(inlineKeyboardMarkup);
        return editMessage;
    }

    public static EditMessageText editMessage(long chatId, int messageId, String text) {
        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(chatId);
        editMessage.setMessageId(messageId);
        editMessage.setText(text);
        return editMessage;
    }

    public static DeleteMessage deleteMessage(long chatId, int messageId) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(chatId);
        deleteMessage.setMessageId(messageId);
        return deleteMessage;
    }

}
