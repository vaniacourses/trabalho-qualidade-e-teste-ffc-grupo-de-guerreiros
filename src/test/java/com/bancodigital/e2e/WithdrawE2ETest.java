package com.bancodigital.e2e;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openqa.selenium.By;

class WithdrawE2ETest extends AbstractE2ETest {

    private static final String EMAIL = "joao@email.com";

    @BeforeEach
    void seedAndLogin() {
        seedDefaultUser();
        loginAs(EMAIL, TEST_PASSWORD);
    }

    // A parametrizacao reutiliza o mesmo fluxo para cada valor e resultado.
    @ParameterizedTest
    @CsvSource({
            "100, success",
            "600, error",
            "10001, error"
    })
    void withdrawShowsExpectedResult(String amount, String alertType) {
        driver.get(baseUrl + "/withdraw");
        driver.findElement(By.id("amount")).sendKeys(amount);
        driver.findElement(By.xpath("//button[text()='Sacar']")).click();

        waitForAlert(alertType);
        String pageSource = driver.getPageSource();
        assertTrue(pageSource.contains("alert " + alertType),
                "O sistema deve exibir o alerta correspondente ao resultado do saque.");
    }
}
