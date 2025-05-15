package org.mango.mangobot.manager.crawler;

import jakarta.annotation.Resource;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.mango.mangobot.knowledgeLibrary.service.TextProcessingService;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class SearchByBrowser {

    public String searchMengNiang(String query) throws IOException {
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
    public String searchBaidu(String query) throws IOException {
        // 百度搜索URL
        String url = "https://www.baidu.com/s?wd=" + query;

        // 发送HTTP请求，获取网页内容
        Document document = Jsoup.connect(url)
                .timeout(10000)
                .get();

        // 选择所有符合条件的搜索结果项
        Elements searchItems = document.select("div.c-container, div.result-op.c-container.new-pmd");

        StringBuilder resultBuilder = new StringBuilder();

        for (Element result : searchItems) {
            // 获取链接
            String linkUrl = result.attr("mu");
            if (linkUrl.isEmpty()) {
                Element linkElement = result.selectFirst("a[href]");
                if (linkElement != null) {
                    linkUrl = linkElement.absUrl("href");
                }
            }

            // 如果链接为空或不符合要求，则跳过
            if (linkUrl == null || linkUrl.isEmpty() || !linkUrl.startsWith("http")) {
                continue;
            }

            // 获取标题
            Element titleElement = result.selectFirst("h3");
            String title = titleElement != null ? titleElement.text() : "无标题";

            // 解析链接的内容
            String content = fetchAndParsePageContent(linkUrl);

            // 拼接最终结果
            resultBuilder
                    .append("【标题】").append(title).append("\n")
                    .append("【链接】").append(linkUrl).append("\n")
                    .append("------------------------------\n\n")
                    .append(content)
                    .append("\n\n");

            // 示例中只处理前几个链接，可根据需求调整
            if (resultBuilder.length() > 10000) { // 假设限制总长度不超过10000字符
                break;
            }
        }

        if (resultBuilder.length() == 0) {
            return "未找到相关内容。";
        }

        return resultBuilder.toString();
    }
    public String searchBing(String query) throws IOException {
        // Bing搜索URL
        String url = "https://cn.bing.com/search?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8);

        // 发送HTTP请求，获取网页内容
        Document document = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3")
                .timeout(10000)
                .get();

        // 选择所有符合条件的搜索结果项
        Elements searchItems = document.select(".b_algo");

        StringBuilder resultBuilder = new StringBuilder();

        for (Element result : searchItems) {
            // 获取标题
            Element titleElement = result.selectFirst("h2");
            String title = titleElement != null ? titleElement.text() : "无标题";

            // 获取摘要
            Element summaryElement = result.selectFirst("p");
            String summary = summaryElement != null ? summaryElement.text() : "暂无摘要";

            // 获取链接
            Element linkElement = result.selectFirst("a[href]");
            String linkUrl = linkElement != null ? linkElement.absUrl("href") : "未知链接";

            // 如果链接为空或不符合要求，则跳过
            if (linkUrl == null || linkUrl.isEmpty() || !linkUrl.startsWith("http")) {
                continue;
            }

            // 解析链接的内容
            String content = fetchAndParsePageContent(linkUrl);

            // 拼接最终结果
            resultBuilder
                    .append("【标题】").append(title).append("\n")
                    .append("【链接】").append(linkUrl).append("\n")
                    .append("------------------------------\n\n")
                    .append(content)
                    .append("\n\n");

            // 示例中只处理前几个链接，可根据需求调整
            if (resultBuilder.length() > 10000) { // 假设限制总长度不超过10000字符
                break;
            }
        }

        if (resultBuilder.length() == 0) {
            return "未找到相关内容。";
        }

        return resultBuilder.toString();
    }

    // 抓取指定网页的内容并解析
    private String fetchAndParsePageContent(String pageUrl) throws IOException {
        if (pageUrl == null || pageUrl.isEmpty()) {
            return "无效链接。";
        }

        try {
            Document doc = Jsoup.connect(pageUrl)
                    .userAgent("Mozilla/5.0")
                    .timeout(5000)
                    .get();

            // 根据不同的网站，选择合适的选择器来获取主要内容
            StringBuilder contentBuilder = new StringBuilder();
            if (pageUrl.contains("baike.baidu.com")) {
                // 对于百度百科
                Elements paragraphs = doc.select("#main-content p");
                for (Element p : paragraphs) {
                    String text = p.text().trim();
                    if (!text.isEmpty()) {
                        contentBuilder.append(text).append("\n\n");
                    }
                }
            } else if (pageUrl.contains("bilibili.com")) {
                // 对于哔哩哔哩
                Elements paragraphs = doc.select(".article-content p");
                for (Element p : paragraphs) {
                    String text = p.text().trim();
                    if (!text.isEmpty()) {
                        contentBuilder.append(text).append("\n\n");
                    }
                }
            } else {
                // 默认处理方式，适用于大多数网页
                Elements paragraphs = doc.select("p");
                for (Element p : paragraphs) {
                    String text = p.text().trim();
                    if (!text.isEmpty()) {
                        contentBuilder.append(text).append("\n\n");
                    }
                }
            }

            return contentBuilder.toString().trim();
        } catch (IOException e) {
            return "无法加载页面: " + e.getMessage();
        }
    }
}
