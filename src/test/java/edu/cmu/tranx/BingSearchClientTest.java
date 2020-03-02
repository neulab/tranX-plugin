package edu.cmu.tranx;

import org.junit.Test;

import java.util.List;

public class BingSearchClientTest {

    @Test
    public void sendData() throws Exception {
        List<String> res = BingSearchClient.getQuestionIDs("reverse list");
        System.out.println(res);
        System.out.println("I'm currently running this test");
    }
}