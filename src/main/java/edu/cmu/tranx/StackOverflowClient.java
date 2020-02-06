package edu.cmu.tranx;

import com.jayway.jsonpath.JsonPath;
import com.intellij.openapi.diagnostic.Logger;
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


public class StackOverflowClient {

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
                .withScheme("https")
                .withHost("api.stackexchange.com")
                .withPath("/2.2/search")
                .addParameter("order", "desc")
                .addParameter("sort", "relevance")
                .addParameter("intitle", buf)
                .addParameter("tagged", "python")
                .addParameter("site", "stackoverflow")
                .toUri();
        HttpRequest request = HttpRequest.newBuilder(uri).build();
        InputStream response = getDecodedInputStream(client.send(request, HttpResponse.BodyHandlers.ofInputStream()));
        String jsonString = inputStreamToString(response);
        // get search result qids
        List<Integer> qids = JsonPath.read(jsonString, "$.items[*].question_id");
        // get selected answers
        List<Integer> selectedAids = JsonPath.read(jsonString, "$.items[*].accepted_answer_id");
        String joinedAids = integerJoin(selectedAids, ";");
        System.out.println(joinedAids);

        uri = UrlBuilder.empty()
                .withScheme("https")
                .withHost("api.stackexchange.com")
                .withPath("/2.2/answers/" + joinedAids)
                .addParameter("sort", "votes")
                .addParameter("order", "desc")
                .addParameter("site", "stackoverflow")
                .addParameter("filter", "withbody")
                .toUri();
        request = HttpRequest.newBuilder(uri).build();
        response = getDecodedInputStream(client.send(request, HttpResponse.BodyHandlers.ofInputStream()));
        jsonString = inputStreamToString(response);

        List<String> answerBodies = JsonPath.read(jsonString, "$.items[*].body");

        Response res = new Response();
        res.hypotheses = new ArrayList<>();
        for (String body : answerBodies) {
            Document doc = Jsoup.parseBodyFragment(body);
            Elements codeSnippets = doc.select("pre > code");
            for (Element e: codeSnippets) {
                Hypothesis hyp = new Hypothesis();
                hyp.value = e.text();
                hyp.id = 0;
                res.hypotheses.add(hyp);
            }
        }
        return res;
    }

}
