package edu.cmu.tranx;

import io.mikael.urlbuilder.UrlBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;


public class BingSearchClient {

    final static HttpClient client = HttpClient.newHttpClient();

    public static List<String> getQuestionIDs(String buf) throws Exception {
        URI uri = UrlBuilder.empty()
                .withScheme("http")
                .withHost("www.bing.com")
                .withPath("/search")
                .addParameter("setmkt", "en-US")
                .addParameter("setlang", "en-us")
                .addParameter("fdpriority", "premium")
                .addParameter("q", "Python " + buf + " site:stackoverflow.com")
                .toUri();

        HttpRequest request = HttpRequest.newBuilder(uri).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Document bingSearchDoc = Jsoup.parse(response.body());
        Elements links = bingSearchDoc.select("li.b_algo > h2 > a");
        List<String> qIds = new ArrayList<>();
        for (Element e: links) {
            qIds.add(e.attr("href").split("/")[4]);
        }
        return qIds;
    }

}
