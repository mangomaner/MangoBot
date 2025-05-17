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

    public String searchBaidu(String query) {
        page.navigate("https://www.baidu.com/s?ie=utf-8&f=8&rsv_bp=1&rsv_idx=1&tn=baidu&wd=" + query);
//        page.locator("#kw").fill(query);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("百度一下")).first().click();
        Locator locatorSearchResult = page.locator("#content_left");
        if(locatorSearchResult.count() == 0)
            return "";
        return page.locator("#content_left").innerText();
    }

    public String searchBing(String query){
        page.navigate("https://cn.bing.com/search?q=" + query);
        Locator locatorMostValueResult = page.locator(".b_viewport").first();
        Locator locatorResult = page.locator("#b_results");
        String result = locatorResult.innerText();
        if(locatorMostValueResult.count() == 0)
            result += locatorMostValueResult.innerText();
        result += "\n" + locatorMostValueResult.innerText();
        return result;
    }

    public String searchBaiduBaike(String query) {
        try {
            // 查看当前搜索界面是否有百度百科
            Locator locator = page.locator("text=- 百度百科");
            String baikeResult;
            if (locator.count() > 0) {
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
                // 没有则直接搜索
                page.navigate("https://baike.baidu.com/item/" + query);
                Locator locatorBaikeResult = page.locator(".mainContent_TWv4s");
                if(locatorBaikeResult.count() == 0)
                    return null;
                baikeResult = locatorBaikeResult.innerText();
            }
            return baikeResult;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // todo
    public String kuaidongBaike(String query) {
        page.navigate("https://www.baike.com/search?keyword=" + query);
        return null;
    }

    // 风控严格，请谨慎调用
    public String searchMengniangBaike(String query) {
        page.navigate("https://mzh.moegirl.org.cn/" + query);
        Locator locator = page.locator(".mw-content-text");
        if(locator.count() == 0) return "";
        return locator.innerText();
    }

}
