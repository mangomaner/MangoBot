package org.mango.mangobot.manager.crawler;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import org.springframework.stereotype.Component;

import javax.naming.Context;
import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@Component
public class PlaywrightBrowser {

    private static Browser browser;
    private static BrowserContext context;
    private static Page page;

    static {
        browser = Playwright.create().chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
        context = browser.newContext();
        page = context.newPage();
        page.navigate("https://www.baidu.com");
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

    public Page searchBaidu(String query) {
        page.navigate("https://baidu.com");
        page.locator("#kw").fill(query);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("百度一下")).click();
        return page;
    }

    public String searchBaiduBaike(String query) {
        Locator locator = page.locator("text=百度百科");
        String baikeResult;
        if(locator.count() > 0) {
            Page page1 = page.waitForPopup(() -> {
                locator.first().click();
            });

            page1.waitForLoadState(LoadState.NETWORKIDLE);
            System.out.println("title = " + page1.title());
            Locator locatorBaikeResult = page1.locator(".mainContent_TWv4s");
            baikeResult = locatorBaikeResult.innerText();
            page.close();
            page = page1;
        } else {
            page.navigate("https://baike.baidu.com/item/" + query);
            Locator locatorBaikeResult = page.locator(".mainContent_TWv4s");
            baikeResult = locatorBaikeResult.innerText();
        }
        return baikeResult;
    }

}
