package edu.cmu.tranx;


import com.google.gson.Gson;
import io.mikael.urlbuilder.UrlBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;


public class UploadHttpClient {

    final static HttpClient client = HttpClient.newHttpClient();

    public static boolean sendQueryData(String query, String userId, int selectedIndex, ArrayList<Hypothesis> options, String currentDocument) {
        QueryData queryData = new QueryData();
        queryData.candidates = options;
        queryData.userId = userId;
        queryData.query = query;
        queryData.selectedIndex = selectedIndex;
        queryData.eventType = "query";
        queryData.document = currentDocument;

        return sendData(queryData);
    }

    public static boolean sendEditData(String finalModifiedCode, String userId, String currentDocument) {
        EditData editData = new EditData();
        editData.finalModifiedCode = finalModifiedCode;
        editData.userId = userId;
        editData.eventType = "edit";
        editData.document = currentDocument;

        return sendData(editData);
    }

    private static boolean sendData(Object data) {
        Gson gson = new Gson();
        String jsonString = gson.toJson(data);
        System.out.println(jsonString);
        URI uri = UrlBuilder.empty()
                .withScheme("http")
                .withHost("moto.clab.cs.cmu.edu")
                .withPort(8081)
                .withPath("/upload")
                .toUri();
        HttpRequest request = HttpRequest.newBuilder().
                POST(HttpRequest.BodyPublishers.ofString(jsonString)).
                uri(uri).
                header("Content-Type", "application/json").
                build();
        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body().equals("success");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

}
