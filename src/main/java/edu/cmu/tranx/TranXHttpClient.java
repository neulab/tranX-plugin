package edu.cmu.tranx;


import com.google.gson.Gson;
import io.mikael.urlbuilder.UrlBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;


public class TranXHttpClient {
    public static class Hypothesis {
        int id;
        double score;
        String value;
        String tree_repr;
        ArrayList<String> actions;
    }

    public static class Response {
        ArrayList<Hypothesis> hypotheses;
    }

    final static HttpClient client = HttpClient.newHttpClient();

    public static Response sendData(String buf) throws Exception {
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
        System.out.println(jsonString);

        return gson.fromJson(jsonString, Response.class);
    }

}
