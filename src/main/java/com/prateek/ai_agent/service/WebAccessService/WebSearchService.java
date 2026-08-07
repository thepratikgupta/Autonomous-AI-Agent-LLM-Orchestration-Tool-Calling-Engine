package com.prateek.ai_agent.service.WebAccessService;

import com.prateek.ai_agent.entity.Other.SearchResult;
import lombok.AllArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class WebSearchService {

private static final HttpClient CLIENT  = HttpClient.newHttpClient();

    public List<SearchResult> search(String query) {

        try {

            String url = "https://html.duckduckgo.com/html/?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8);

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .header(
                                    "User-Agent",
                                    "Mozilla/5.0"
                            )
                            .header(
                                    "Accept",
                                    "text/html"
                            )
                            .GET()
                            .build();

            HttpResponse<String> response = CLIENT
                    .send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("DuckDuckGo status: " + response.statusCode());

            Document doc = Jsoup.parse(response.body());

            Elements results = doc.select(".result");

            int rank = 1;
            List<SearchResult> list = new ArrayList<>();

            for (Element result : results) {

                Element link = result.selectFirst("a.result__a");

                //JUST FOR DEBUGGING START
                System.out.println(link.attr("href"));
                System.out.println(link.absUrl("href"));
                //JUST FOR DEBUGGING END

                Element snippet = result.selectFirst(".result__snippet");

                if (link != null) {

                    String actualUrl =
                            extractRealUrl(link.attr("href"));
                    //JUST FOR DEBUGGING START
                    System.out.println("TITLE: " + link.text());
                    System.out.println("RAW URL: " + link.attr("href"));
                    System.out.println("REAL URL: " + actualUrl);
                    //JUST FOR DEBUGGING END
                    list.add(
                            new SearchResult(
                                    rank++,
                                    link.text(),
                                    actualUrl,
                                    snippet != null
                                            ? snippet.text()
                                            : ""
                            )
                    );
                }
                if (list.size() >= 5) {
                    break;
                }
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }
    private String extractRealUrl(String href) {

        try {

            if (href.startsWith("//")) {
                href = "https:" + href;
            }
            if (href.startsWith("/")) {
                href = "https://duckduckgo.com" + href;
            }

            URI uri = URI.create(href);
            String query = uri.getQuery();

            if (query == null) {
                return href;
            }

            for (String part : query.split("&")) {

                if (part.startsWith("uddg=")) {
                    return URLDecoder.decode(
                            part.substring(5),
                            StandardCharsets.UTF_8
                    );
                }
            }

            return href;

        } catch (Exception e) {
            return href;
        }
    }
}
