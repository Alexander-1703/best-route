package parser.pages.searchPage;

import org.openqa.selenium.By;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.byAttribute;
import static com.codeborne.selenide.Selectors.byXpath;
import static com.codeborne.selenide.Selenide.$;

/**
 * Класс - враппер для элементов типов пассажира (взрослый, ребенок, младенец)
 */
class PassengerWrapper {

    private static final By COUNTER = byAttribute("aria-live", "assertive");
    private static final By ADD_PSNG_BTN = byXpath(".//div[@role = 'button'][2]");

    private By rootElem;
    private int needAmount;

    PassengerWrapper(By rootElem, int needAmount) {
        $(rootElem).shouldBe(visible.because("Нет элемента типа пассажира"));
        this.rootElem = rootElem;
        this.needAmount = needAmount;
    }

    /**
     * Нажимать "+" для данного типа пассажира, пока не будет достигнуто необх. кол-во
     */
    void addPassengers() {
        int currAmount = Integer.parseInt(
                $(rootElem).$(COUNTER).shouldBe(visible.because("Нет элемента с кол-вом пассжаира")).text()
        );

        while (currAmount < needAmount) {
            $(rootElem).find(ADD_PSNG_BTN).shouldBe(visible.because("Нет кнопки добавления кол-во пассажиров")).click();
            currAmount++;
        }
    }
}
