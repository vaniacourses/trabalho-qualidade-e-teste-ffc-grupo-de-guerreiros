package com.bancodigital.e2e;

import java.math.BigDecimal;
import java.util.HashMap;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration-test")
public abstract class AbstractE2ETest {

    protected static final String BCRYPT_TEST_PASSWORD = "$2a$10$tawrHKPHutNwZPe/Sm1AEOiUqB5mfOznW5PWvzcM1zozEyfowdlOu";
    protected static final String TEST_PASSWORD = "XhqUH*&c0qggXp9S#xef";

    @LocalServerPort
    private int port;

    @Autowired
    protected JdbcTemplate jdbc;

    protected WebDriver driver;
    protected String baseUrl;

    @BeforeEach
    void setupDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        boolean headless = "true".equalsIgnoreCase(System.getProperty("headless", "false"));
        if (headless) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--no-sandbox", "--disable-dev-shm-usage", "--window-size=1280,800",
                "--disable-features=PasswordLeakDetection", "--no-first-run");
        HashMap<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        prefs.put("profile.password_manager_leak_detection", false);
        options.setExperimentalOption("prefs", prefs);
        driver = new ChromeDriver(options);
        baseUrl = "http://localhost:" + port;
        jdbc.execute("TRUNCATE TABLE transactions, investments, accounts, users RESTART IDENTITY CASCADE");
        jdbc.execute("ALTER SEQUENCE account_number_seq RESTART WITH 100");
    }

    @AfterEach
    void teardownDriver() {
        if (driver != null) {
            driver.quit();
        }
    }

    protected long insertUser(String name, String email, String passwordHash) {
        jdbc.update("INSERT INTO users (name, email, password_hash) VALUES (?, ?, ?)", name, email, passwordHash);
        return jdbc.queryForObject("SELECT id FROM users WHERE email = ?", Long.class, email);
    }

    protected long insertAccount(String number, BigDecimal balance, long userId) {
        jdbc.update("INSERT INTO accounts (number, balance, user_id) VALUES (?, ?, ?)", number, balance, userId);
        return jdbc.queryForObject("SELECT id FROM accounts WHERE number = ?", Long.class, number);
    }

    protected void insertInvestment(long userId, BigDecimal amount) {
        jdbc.update("INSERT INTO investments (user_id, amount) VALUES (?, ?)", userId, amount);
    }

    protected long seedDefaultUser() {
        long userId = insertUser("Joao Silva", "joao@email.com", BCRYPT_TEST_PASSWORD);
        insertAccount("C00001", new BigDecimal("500.00"), userId);
        return userId;
    }
}
