package edu.cmu.tranx;

import com.jayway.jsonpath.JsonPath;
import io.mikael.urlbuilder.UrlBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;


public class StackOverflowClient extends Client {

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
        while ((str = reader.readLine()) != null) {
            sb.append(str);
        }
        return sb.toString();
    }


    public static Response sendData(String buf) throws IOException, InterruptedException {
        List<String> questionIds = BingSearchClient.getQuestionIDs(buf);
        String joinedQids = String.join(";", questionIds);

        URI uri = UrlBuilder.empty()
                .withScheme("https")
                .withHost("api.stackexchange.com")
                .withPath("/2.2/questions/" + joinedQids + "/answers")
                .addParameter("order", "desc")
                .addParameter("sort", "votes")
                .addParameter("pagesize", "10")
                .addParameter("filter", "withbody")
                .addParameter("site", "stackoverflow")
                .toUri();

        System.out.println(uri);
        HttpRequest request = HttpRequest.newBuilder(uri).build();
        InputStream response = getDecodedInputStream(client.send(request, HttpResponse.BodyHandlers.ofInputStream()));
        String jsonString = inputStreamToString(response);

        List<String> answerBodies = JsonPath.read(jsonString, "$.items[*].body");

        Response res = new Response();
        res.hypotheses = new ArrayList<>();
        for (String body : answerBodies) {
            Document doc = Jsoup.parseBodyFragment(body);
            Elements codeSnippets = doc.select("pre > code");
            for (Element e : codeSnippets) {
                Hypothesis hyp = new Hypothesis();
                hyp.value = e.text();
                hyp.id = 0;
                res.hypotheses.add(hyp);
            }
        }
        return res;
    }

    public static List<Hypothesis> getCandidates(String query) {
        try {
            return sendData(query).hypotheses;
        } catch (IOException | InterruptedException e) {
            return null;
        }
    }

}
