package org.mango.mangobot.manager.crawler;

import jakarta.annotation.Resource;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.mango.mangobot.knowledgeLibrary.service.TextProcessingService;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SearchByBrowser {

    @Resource
    private TextProcessingService textProcessingService;

    public String searchBing(String query) throws Exception {
        String url = "https://mzh.moegirl.org.cn/" + query; // 要抓取的网页地址
        try {
            // 发送HTTP请求，获取网页内容
            Document document = Jsoup.connect(url).get();
            // 提取网页的标题
            String title = document.title();
            System.out.println("标题：" + title);
            // 提取网页的正文内容
            Element contentElement = document.body();
            String content = contentElement.text();
            System.out.println("正文：" + content);
            return content;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
