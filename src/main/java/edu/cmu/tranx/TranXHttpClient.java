package edu.cmu.tranx;


import com.google.gson.Gson;
import io.mikael.urlbuilder.UrlBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;


public class TranXHttpClient extends Client {

    public static Response sendData(String buf) throws IOException, InterruptedException {
        URI uri = UrlBuilder.empty()
                .withScheme("http")
                .withHost("moto.clab.cs.cmu.edu")
                .withPort(8081)
                .withPath("/parse/conala")
                .addParameter("q", buf)
                .toUri();
        HttpRequest request = HttpRequest.newBuilder(uri).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Gson gson = new Gson();
        String jsonString = response.body();
        return gson.fromJson(jsonString, Response.class);
    }

    public static List<Hypothesis> getCandidates(String query) {
        try {
            return sendData(query).hypotheses;
        } catch (IOException | InterruptedException e) {
            return null;
        }
    }

}
