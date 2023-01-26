package parser;

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
//        Configuration.holdBrowserOpen = true; // delete
        Configuration.timeout = 8_000;
<<<<<<< HEAD
      //  Configuration.headless = true;
=======
        Configuration.headless = true;
>>>>>>> 2bfc88f3d884a6c759d13313c3bb22493db6fe82
    }
}
