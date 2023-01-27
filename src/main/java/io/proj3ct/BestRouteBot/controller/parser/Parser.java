package io.proj3ct.BestRouteBot.controller.parser;

import io.proj3ct.BestRouteBot.controller.parser.pages.TicketsPage.Ticket;
import io.proj3ct.BestRouteBot.controller.parser.pages.TicketsPage.TicketsPage;
import io.proj3ct.BestRouteBot.controller.parser.pages.searchPage.TripType;
import io.proj3ct.BestRouteBot.controller.parser.pages.searchPage.SearchPage;

import javax.annotation.Nullable;
import java.util.List;

import static com.codeborne.selenide.Selenide.closeWebDriver;
import static com.codeborne.selenide.Selenide.open;

public class Parser extends BaseParser {

    private static final String URL = "https://russpass.tripandfly.ru/";

    /**
     * Метод для начала парсинга билетов
     * @param startLocation стартовая локация
     * @param endLocation локация прибытия
     * @param date дата отъезда
     * @param adultsAmount кол-во взрослых (<b>min = 1, max = 7</b>). Всего пассажиров: не более 7
     * @param childrenAmount кол-во детей (<b>max = 6</b>)
     * @param babyAmount кол-во младенцев (<b>max = 2</b>) Не может быть больше кол-ва взрослых
     * @param tripType тип поездки: эконом, бизнес, первый
     * @return лист билетов (<b>max = 10</b>), подобранных по заданным параметрам. Если билетов нет -> <i>null</i>
     */
    public @Nullable List<Ticket> getTickets(String startLocation, String endLocation, String date,
                                   int adultsAmount, int childrenAmount, int babyAmount, TripType tripType) {
        open(URL);
        TicketsPage ticketsPage =  new SearchPage()
                .addStartLocation(startLocation)
                .addFinishLocation(endLocation)
                .selectFromDate(date)
                .selectTripOptions(adultsAmount, childrenAmount, babyAmount, tripType)
                .find();
        List<Ticket> tickets = ticketsPage.getTicketsList();
        closeWebDriver();
        return tickets;
    }
}
