package edu.cmu.tranx;

import org.junit.Test;

public class BingSearchClientTest {

    @Test
    public void sendData() throws Exception {
        Response res = BingSearchClient.sendData("reverse list");
        System.out.println(res);
        System.out.println("I'm currently running this test");
    }
}