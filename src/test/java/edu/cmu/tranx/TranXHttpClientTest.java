package edu.cmu.tranx;

import org.junit.Test;

import java.util.ArrayList;

public class TranXHttpClientTest {

    @Test
    public void sendData() throws Exception {
        ArrayList<Hypothesis> response = TranXHttpClient.sendData("print hello").hypotheses;
        System.out.println("I'm currently running this test");
        for(Hypothesis x : response) {
            System.out.println(x.id + " " + x.value + " " + x.score);
        }
    }
}