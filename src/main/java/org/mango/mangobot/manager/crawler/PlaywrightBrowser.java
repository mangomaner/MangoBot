package org.mango.mangobot.manager.crawler;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.springframework.stereotype.Component;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@Component
public class PlaywrightBrowser {

    private static Browser browser;
    private static Page page;

    static {
        browser = Playwright.create().chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
        page = browser.newPage();
    }

    public String test(){
        page.navigate("https://baidu.com");
        page.locator("#kw").fill("四谎结局");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("百度一下")).click();

        Locator locatorSearchResult = page.locator("#content_left");
        String result = locatorSearchResult.innerText();

        page.navigate("https://baike.baidu.com/item/伊蕾娜");
        Locator locatorBaikeResult = page.locator(".mainContent_TWv4s");

        String baikeResult = locatorBaikeResult.innerText();
        return baikeResult;
    }

    public String searchBaidu(String query) {
        page.navigate("https://baidu.com");
        page.locator("#kw").fill(query);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("百度一下")).click();
        Locator locatorSearchResult = page.locator("#content_left");
        return locatorSearchResult.innerText();
    }

    public String searchBaiduBaike(String query) {
        page.navigate("https://baike.baidu.com/item/" + query);
        Locator locatorBaikeResult = page.locator(".mainContent_TWv4s");
        return locatorBaikeResult.innerText();
    }

}
