package com.swp.myleague.utils;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.HashMap;
import java.util.Map;

public class DriverFactory {
    public static WebDriver createDriver() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("profile.managed_default_content_settings.javascript", 1); // 1 = allow
        options.setExperimentalOption("prefs", prefs);
        options.addArguments("--incognito");
        // options.addArguments("--headless=new"); // nếu cần chạy CI không hiển thị

        return new ChromeDriver(options);
    }
}
