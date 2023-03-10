package io.proj3ct.BestRouteBot.controller.parser.pages.TicketsPage;

import java.util.ArrayList;
import java.util.List;
import com.codeborne.selenide.ElementsCollection;
import org.openqa.selenium.By;
import com.codeborne.selenide.SelenideElement;
import io.proj3ct.BestRouteBot.controller.parser.pages.Loadable;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.byClassName;
import static com.codeborne.selenide.Selectors.byTagName;
import static com.codeborne.selenide.Selectors.byXpath;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

/**
 * Страница с результатами поиска билетов
 */
public class TicketsPage implements Loadable {

    private static final By TICKET_CARD = byTagName("wl-search-results-card-cmmt");
    private static final By LEFT_OPTIONS_PANEL = byTagName("wl-filter-container");
    private static final By TRANSFER_DURATION = byXpath("//div[@class = 'wl-filter-transfer__duration-title']/..");
    private static final By EMPTY_WRAPPER = byClassName("search-not-found__title");
    private static final By MAIN_TICKETS_PANEL = byClassName("results-cards");
    private static final By COMPOS_ROUTE_INFO =
            byXpath("//*[text() = 'Составные  маршруты']/../div[contains(@class, 'result-tab-label__info')]");

    private static final By TICKETS_AMOUNT = byClassName("wl-filter-feature__header__content");

    private static final ElementsCollection ALL_TICKETS = $$(TICKET_CARD);


    public TicketsPage() {
        loaded();
    }

    /**
     * Парсинг билетов со страницы результатов
     * @return лист из билетов
     */
    public List<Ticket> getTicketsList() {

        if ($(EMPTY_WRAPPER).is(visible)) {
            return null;

        } else {
            $(LEFT_OPTIONS_PANEL).shouldBe(visible.because("Нет левой панели с опциями для билетов"));

            int ticketsAmount = Integer.parseInt(
                    $(TICKETS_AMOUNT).shouldBe(visible.because("Нет элемента с кол-вом билетов"))
                            .getOwnText()
                            .trim()
            );

            for (byte i = 0; i <= ticketsAmount / 10; i++)
                ALL_TICKETS.last().hover();

            ALL_TICKETS.shouldHave(size(ticketsAmount));
        }

        List<SelenideElement> ticketElems = $$(TICKET_CARD)
                .asFixedIterable()
                .stream()
                .limit(10).toList();
        List<Ticket> tickets = new ArrayList<>(10);

        for (var elem: ticketElems) {
            Ticket ticket = new TicketElemWrapp(elem).getTicket();
            tickets.add(ticket);
        }
        return tickets;
    }

    @Override
    public void loaded() {
        $(MAIN_TICKETS_PANEL).shouldBe(visible.because("Нет главной панели с билетами"));
        $(COMPOS_ROUTE_INFO).shouldBe(visible.because("Нет информации о составных маршрутах"));
    }
}
