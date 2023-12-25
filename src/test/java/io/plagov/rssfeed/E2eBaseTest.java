package io.plagov.rssfeed;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.BeforeEach;

public abstract class E2eBaseTest {

    public Playwright playwright;
    public Browser browser;
    public Page page;

    @BeforeEach
    void setUpPlaywright() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
        page = browser.newPage();
    }
}
