package edu.cmu.tranx;


import com.google.gson.Gson;
import io.mikael.urlbuilder.UrlBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;


public class UploadHttpClient extends Client {

    public static boolean sendQueryData(String query, String userId, String projectName, int selectedIndex, List<Hypothesis> options, String currentDocument, String hash) {
        QueryData queryData = new QueryData();
        queryData.candidates = options;
        queryData.userId = userId;
        queryData.projectName = projectName;
        queryData.query = query;
        queryData.selectedIndex = selectedIndex;
        queryData.eventType = "query";
        queryData.document = currentDocument;
        queryData.hash = hash;

        return sendData(queryData);
    }

    public static boolean sendEditData(String finalModifiedCode, String userId, String projectName, String currentDocument, String query, String hash) {
        EditData editData = new EditData();
        editData.finalModifiedCode = finalModifiedCode;
        editData.userId = userId;
        editData.projectName = projectName;
        editData.eventType = "edit";
        editData.document = currentDocument;
        editData.hash = hash;
        editData.query = query;

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
