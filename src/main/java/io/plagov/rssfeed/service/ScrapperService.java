package io.plagov.rssfeed.service;

import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ScrapperService {

    public String scrape(String url) throws IOException {
        var document = Jsoup
                .connect(url)
                .userAgent("Mozilla")
                .get();

        var body = document.selectFirst("article");
        if (body == null) {
            body = document.selectFirst("main");
        }

        if (body == null) {
            body = document.body();
        }

        return body.text();
    }
}
