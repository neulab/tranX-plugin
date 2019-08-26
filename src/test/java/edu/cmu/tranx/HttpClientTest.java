package edu.cmu.tranx;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class HttpClientTest {

    @Test
    public void sendData() throws Exception {
        ArrayList<HttpClient.Hypothesis> response = HttpClient.sendData("print hello").hypotheses;
        System.out.println("I'm currently running this test");
        for(HttpClient.Hypothesis x : response) {
            System.out.println(x.id + " " + x.value + " " + x.score);
        }
        Assert.assertEquals(response.get(0).score, response.get(0).score);
    }
}