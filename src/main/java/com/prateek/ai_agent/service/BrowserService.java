package com.prateek.ai_agent.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import java.io.IOException;

@Service
public class BrowserService {

    private static final int MAX_CONTENT_LENGTH = 8000;

    public String openUrl(String url) {
        if (url == null || url.isBlank()) {
            return "Invalid URL.";
        }

        if (!url.startsWith("http")) {
            return "Invalid URL: " + url;
        }
        try {

            Document doc = Jsoup.connect(url)
                    .userAgent(
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"
                    )
                    .referrer("https://www.google.com")
                    .timeout(15000)
                    .followRedirects(true)
                    .get();
            String title = doc.title();

            doc.select("""
                        script,
                        style,
                        nav,
                        footer,
                        header,
                        aside,
                        noscript,
                        iframe,
                        svg,
                        form,
                        button,
                        ads
                        """).remove();

            String text = clean(doc.body().text());
            boolean truncated = false;
            if (text.length() > MAX_CONTENT_LENGTH) {
                text = text.substring(0, MAX_CONTENT_LENGTH);
                truncated = true;
            }

            return """
                    Page Title: %s
                    URL: %s
                    
                    Extracted Content:
                    
                    %s
                    
                    %s
                    """.formatted(
                            title,
                            url,
                            text,
                            truncated
                                    ? "NOTE: Content was truncated."
                                    : "NOTE: Entire content extracted."
                            );

        } catch (IOException e) {
            return "Failed to fetch page: " + e.getMessage();
        }
    }

    private String clean(String text) {

        return text
                .replaceAll("\\s+", " ")
                .trim();
    }
}
