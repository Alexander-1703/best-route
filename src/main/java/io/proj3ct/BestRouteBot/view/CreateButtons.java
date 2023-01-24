package io.proj3ct.BestRouteBot.view;

import java.util.ArrayList;
import java.util.List;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

public class CreateButtons {

    public static InlineKeyboardMarkup oneInLineButton(String title, String callBack) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();

        var button = new InlineKeyboardButton();
        button.setText(title);
        button.setCallbackData(callBack);
        rowInLine.add(button);
        rowsInLine.add(rowInLine);

        inlineKeyboardMarkup.setKeyboard(rowsInLine);
        return inlineKeyboardMarkup;
    }

    //заполнение кнопок происходит следующим образом:
    //  1 2 3
    //  4 5 6
    public static InlineKeyboardMarkup inLineButtons(int numPerLine, int numLines, String[] titles, String[] callBacks) {
        if (numLines * numPerLine != titles.length || titles.length != callBacks.length) {
            throw new IllegalArgumentException();
        }
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        for (int i = 1; i <= numLines; i++) {
            List<InlineKeyboardButton> rowInLine = new ArrayList<>();
            for (int j = 1; j <= numPerLine; j++) {
                var button = new InlineKeyboardButton();
                button.setText(titles[(i - 1) * numPerLine + j - 1]);
                button.setCallbackData(callBacks[(i - 1) * numPerLine + j - 1]);
                rowInLine.add(button);
            }
            rowsInLine.add(rowInLine);
        }

        inlineKeyboardMarkup.setKeyboard(rowsInLine);
        return inlineKeyboardMarkup;
    }
}
