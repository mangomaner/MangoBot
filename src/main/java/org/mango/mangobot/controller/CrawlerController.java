package org.mango.mangobot.controller;

import jakarta.annotation.Resource;
import org.mango.mangobot.manager.crawler.PlaywrightBrowser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/crawler")
public class CrawlerController {

    @Resource
    private PlaywrightBrowser playwrightBrowser;

    @GetMapping("/getTitle")
    public String test() {
        return playwrightBrowser.test();
    }

}
