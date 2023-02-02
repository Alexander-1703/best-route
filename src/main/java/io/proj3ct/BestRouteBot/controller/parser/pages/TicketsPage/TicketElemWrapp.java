package io.proj3ct.BestRouteBot.controller.parser.pages.TicketsPage;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import java.util.Arrays;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.byClassName;
import static com.codeborne.selenide.Selectors.byXpath;
import static com.codeborne.selenide.Selenide.$;

/**
 * Класс - враппер для элемента билета на странице
 */
public class TicketElemWrapp {

    private final static By WAY_POINTS = byXpath(".//div[@class = 'route-info']");
    private final static By TIME_START = byClassName("time-title");
    private final static By TIME_END = byXpath(".//*[contains(@class, 'time-title--right')]");
    private final static By DATE_START = byClassName("route-date");
    private final static By DATE_END = byXpath(".//*[contains(@class, 'route-date--right')]");
    private final static By TRIP_TIME = byClassName("trip-duration__time");
    private final static By TRANSFER_AMOUNT = byClassName("quantity-transfer");
    private final static By MIN_PRICE = byClassName("wl-integer");

    private final SelenideElement rootElem;
    //private final String dateStart;

    TicketElemWrapp(SelenideElement rootElem) {
        this.rootElem = rootElem;
        $(rootElem).shouldBe(visible.because("Нет элемента билета"));
    }

    /**
     * Парсит билет со страницы
     * @return билет с информацией о нем
     */
    public Ticket getTicket() {
        String wayPoints = getElemText(WAY_POINTS, "Нет элемента с городами билета");
        String timeStart = getElemText(TIME_START, "Нет элемента с началом времени");
        String timeEnd = getElemText(TIME_END, "Нет элемента с конечном временем");


        String tripTimeString = getElemText(TRIP_TIME, "Нет элемента с продолжительностью");
        int tripTime = getTripTime(tripTimeString.);

        /**
         *  Pattern pattern = Pattern.compile(("(?<![А-яA-z0-9])(" + String.join("|", words) + ")(?![А-яA-z0-9])"));
         *             Matcher matcher = pattern.matcher(allTexts);
         *             while (matcher.find()) {
         *                 amount++;
         *         }
         */


        String transferAmount = getElemText(TRANSFER_AMOUNT, "Нет элемента с кол-вом переcадок");
        String price = getElemText(MIN_PRICE, "Нет элемента с ценой");

        String dateStart = rootElem.$(DATE_START).is(visible) ? rootElem.$(DATE_START).text() : "";
        String dateEnd = rootElem.$(DATE_END).is(visible) ? rootElem.$(DATE_END).text() : "";

        return new Ticket(wayPoints, timeStart, timeEnd, dateStart, dateEnd, transferAmount, price, tripTime);
    }

    private String getElemText(By elem, String errMsg) {
        return rootElem.$(elem).shouldBe(visible.because(errMsg)).text();
    }

    private int getTripTime(String[] tripTimeInfo) {
        int result = 0;
        System.out.println(Arrays.stream(tripTimeInfo).toList());
        for (String elem: tripTimeInfo) {

            int number = Integer.parseInt(elem.split(" ")[0]);
            if (elem.contains("д")) {
                result += number * 24 * 60;
            } else if (elem.contains("ч")) {
                result += number * 60;
            } else if (elem.contains("мин")) {
                result += number;
            }
        }
        return result;

    }
}
