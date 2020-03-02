package edu.cmu.tranx;

import java.net.http.HttpClient;
import java.time.Duration;

public class Client {
    final static HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

}
