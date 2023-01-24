package io.proj3ct.BestRouteBot.controller.parser;

import com.codeborne.selenide.Configuration;
import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Базовый парсер: инициализация настроек браузера для всех парсеров
 */
public class BaseParser {

    BaseParser() {
        WebDriverManager.chromedriver().setup();
        Configuration.browser = "chrome";
        Configuration.driverManagerEnabled = true;
        Configuration.holdBrowserOpen = true; // delete
        Configuration.timeout = 8_000;
    }
}
