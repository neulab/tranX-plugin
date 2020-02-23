package edu.cmu.tranx;

import com.jayway.jsonpath.JsonPath;
import io.mikael.urlbuilder.UrlBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;


public class BingSearchClient {

    final static HttpClient client = HttpClient.newHttpClient();
    public static InputStream getDecodedInputStream(
            HttpResponse<InputStream> httpResponse) {
        String encoding = determineContentEncoding(httpResponse);
        try {
            switch (encoding) {
                case "":
                    return httpResponse.body();
                case "gzip":
                    return new GZIPInputStream(httpResponse.body());
                default:
                    throw new UnsupportedOperationException(
                            "Unexpected Content-Encoding: " + encoding);
            }
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

    public static String determineContentEncoding(
            HttpResponse<?> httpResponse) {
        return httpResponse.headers().firstValue("Content-Encoding").orElse("");
    }

    public static String inputStreamToString(InputStream is) throws IOException {
        InputStreamReader isReader = new InputStreamReader(is);
        BufferedReader reader = new BufferedReader(isReader);
        StringBuilder sb = new StringBuilder();
        String str;
        while((str = reader.readLine())!= null){
            sb.append(str);
        }
        return sb.toString();
    }

    public static String integerJoin(List<Integer> arr, String separator) {
        if (null == arr || 0 == arr.size()) return "";
        StringBuilder sb = new StringBuilder(256);
        sb.append(arr.get(0).toString());
        for (int i = 1; i < arr.size(); i++) sb.append(separator).append(arr.get(i).toString());
        return sb.toString();
    }


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
