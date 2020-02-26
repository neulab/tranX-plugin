package edu.cmu.tranx;

import com.jayway.jsonpath.JsonPath;
import io.mikael.urlbuilder.UrlBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;


public class BingSearchClient {

    final static HttpClient client = HttpClient.newHttpClient();


    public static Response sendData(String buf) throws Exception {
        URI uri = UrlBuilder.empty()
                .withScheme("http")
                .withHost("moto.clab.cs.cmu.edu")
                .withPort(8082)
                .withPath("/")
                .addParameter("q", "Python " + buf + " site:stackoverflow.com")
                .addParameter("c", "5")
                .toUri();

        HttpRequest request = HttpRequest.newBuilder(uri).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<String> codeSnippets = JsonPath.read(response.body(), "$.ResultItems[*].CodeSnippets[*]");
        Response res = new Response();
        res.hypotheses = new ArrayList<>();
        for (String snippet : codeSnippets) {
            Hypothesis hyp = new Hypothesis();
            hyp.value = snippet;
            hyp.id = 0;
            res.hypotheses.add(hyp);
        }
        return res;
    }

}
