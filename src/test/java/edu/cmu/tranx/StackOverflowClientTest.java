package edu.cmu.tranx;

import org.junit.Test;

import java.util.ArrayList;

public class StackOverflowClientTest {

    @Test
    public void sendData() throws Exception {
        Response res = StackOverflowClient.sendData("reverse list");
        System.out.println("I'm currently running this test");
    }
}