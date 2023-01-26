package parser.pages.searchPage;

import org.openqa.selenium.By;
import parser.pages.Loadable;
import parser.pages.TicketsPage.TicketsPage;
import java.util.List;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.*;
import static com.codeborne.selenide.Selenide.$;

/**
 * Начальная страница поиска билетов - ввод параметров для поиска
 */
public class SearchPage implements Loadable {

    private static final By PANEL = byClassName("panel--search");
    private static final By FROM_FIELD = byXpath("//*[@formcontrolname = 'departureStation']//input");
    private static final By ARRIVE_FIELD = byXpath("//*[@formcontrolname = 'arrivalStation']//input");
    private static final By START_DATE_FIELD = byXpath("//ui-kit-input[@id = 'ui-kit-input-1']");
    private static final By BACK_DATE_FIELD = byXpath("//ui-kit-input[@id = 'ui-kit-input-2']");
    private static final By PASSNGR_OPTIONS = byId("click-element");
    private static final By FIND_BTN = byXpath("//button[@class = 'button--prime']");

    public SearchPage() {
        loaded();
    }


    public SearchPage addStartLocation(String startLocation) {
        $(FROM_FIELD).shouldBe(visible.because("Нет поля для ввода 'откуда'")).setValue(startLocation);
        chooseLocation(startLocation);
        return this;
    }

    public SearchPage addFinishLocation(String finishLocation) {
        $(ARRIVE_FIELD).shouldBe(visible.because("Нет поля для ввода 'куда'")).setValue(finishLocation);
        chooseLocation(finishLocation);
        return this;
    }

    private void chooseLocation(String location) { // Can be problems with cases-CASES (from TG)
         By firstListLocation = byXpath(
                String.format("//wl-station-description//*[contains(text(), '%s')]", location)
        );
        $(firstListLocation).shouldBe(visible.because("Таких населенных пунктов нет")).click();
    }

    public SearchPage selectFromDate(String date) {    // yyyy-mm-dd    если месяц \ число < 10 -> add '0' !!!
        $(START_DATE_FIELD).shouldBe(visible.because("Нет поля для ввода даты 'туда'")).click();
        String dateLocator = String.format(
                "//*[contains(@aria-label, '%s')]",
                formatDate(date)
        );
        By needDateElem = byXpath(dateLocator);
        By nextMonthElem = byXpath("//*[@class = 'ui-kit-datepicker__navigation']/div[position() = 2]");

        while (!($(needDateElem).is(visible))) {
            $(nextMonthElem).shouldBe(visible.because("Нет стрелки след. месяца")).click();
        }
        $(needDateElem).click();
        return this;
    }

    /**
     * Создание даты для локатора на странцие
     * @param date дата в виде yyyy-mm-dd
     * @return дата в виде dd.mm.yyyy
     */
    private StringBuilder formatDate(String date) {
        String[] dateInfo = date.split("-");
        return new StringBuilder()
                .append(dateInfo[2]).append(".")
                .append(dateInfo[1]).append(".")
                .append(dateInfo[0]);
    }

    public SearchPage selectTripOptions(int adultsAmount, int childrenAmount, int babyAmount, TripType flightType) {
        $(PASSNGR_OPTIONS).shouldBe(visible.because("Нет кнопки для опций полета")).click();
        selectPsngrs(adultsAmount, childrenAmount, babyAmount); // всего 7 пассажиров, не больше 2 младенцев
        selectTripType(flightType);
        return this;
    }

    /**
     * Выбор необходимого кол-ва каждого вида пассажира
     * @param adultsAmount кол-во взрослых
     * @param childrenAmount кол-во детей
     * @param babyAmount кол-во младенцев
     */
    private void selectPsngrs(int adultsAmount, int childrenAmount, int babyAmount) {
        String xpathRootElem = "//*[@class = 'search-additional__counters']/div[position() = %d]";
        List<PassengerWrapper> passengers = List.of(
                new PassengerWrapper(byXpath(String.format(xpathRootElem, 1)), adultsAmount),
                new PassengerWrapper(byXpath(String.format(xpathRootElem, 2)), childrenAmount),
                new PassengerWrapper(byXpath(String.format(xpathRootElem, 3)), babyAmount)
        );

        for (var pass: passengers) {
            pass.addPassengers();
        }
    }

    /**
     * Выбор типа поездки: эконом, бизнесс, первый
     * @param flightType тип поездки
     */
    private void selectTripType(TripType flightType) {
        $(PASSNGR_OPTIONS).shouldBe(visible.because("Нет кнопки для опций полета")).click();
        By rootElem = byClassName("search-additional__service-class");
        By typeElem = byXpath(String.format(".//*[@value = '%s']/..", flightType.toString()));
        $(rootElem).$(typeElem).shouldBe(visible.because("Нет элемента с выбором типа полета")).click();
    }

    public TicketsPage find() {
        try {Thread.sleep(2_000);} catch (Exception e) {}  // Their server is slow, need to wait. Don't know how to fix
        $(FIND_BTN).shouldBe(visible.because("Нет кнопки поиска")).hover().click();
        return new TicketsPage();
    }

    @Override
    public void loaded() {
        $(PANEL).shouldBe(visible.because("Нет главной панели"));
    }
}

