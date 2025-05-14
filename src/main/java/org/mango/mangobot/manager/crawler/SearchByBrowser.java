package org.mango.mangobot.manager.crawler;

import jakarta.annotation.Resource;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.mango.mangobot.knowledgeLibrary.service.TextProcessingService;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SearchByBrowser {

    @Resource
    private TextProcessingService textProcessingService;

    public String searchBing(String query) throws IOException {
        String url = "https://mzh.moegirl.org.cn/" + query; // 要抓取的网页地址

        // 发送HTTP请求，获取网页内容
        Document document = Jsoup.connect(url).get();
        Elements contentParagraphs = document.select("p");
        StringBuilder contentBuilder = new StringBuilder();
        for (Element p : contentParagraphs) {
            String text = p.text().replaceAll("<sup>.*?</sup>", "").trim();
            if (!text.isEmpty()) {
                contentBuilder.append(text).append(" ");
            }
        }
        String mainContent = contentBuilder.toString().trim();
        return mainContent;

    }
}
