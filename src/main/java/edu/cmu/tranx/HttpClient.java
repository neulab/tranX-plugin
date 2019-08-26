package edu.cmu.tranx;

/**
 * Created by williamqian on 7/6/17.
 */


import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

//import java.io.IOException;

public class HttpClient{
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

    public static Response sendData(String buf) throws Exception {

        String encoded_buf = URLEncoder.encode(buf, "UTF-8");
        URL url = new URL("http://moto.clab.cs.cmu.edu:8082/parse/conala/"+encoded_buf);
        HttpURLConnection httpcon = (HttpURLConnection)url.openConnection();
        httpcon.setDoOutput(true);
        httpcon.setDoInput(true);
        httpcon.setRequestMethod("GET");
        httpcon.setRequestProperty("Content-Type","application/json");

        StringBuilder sb= new StringBuilder();
        BufferedReader br= new BufferedReader( new InputStreamReader(httpcon.getInputStream(),
                                                "UTF-8"));
        String line=null;
        while((line=br.readLine())!=null){
            sb.append(line+'\n');
        }
        // sb.deleteCharAt(0);
        // sb.deleteCharAt(sb.lastIndexOf("]"));
        // //System.out.print("{"+sb.toString()+"}");
        // String[] hold=(sb.toString()).split("},");
        // //System.out.print(sb.toString());

        // for(int i=0;i<4;i++) {
        //     hold[i]=hold[i]+"}";
        // }
        // //System.out.print(hold[0]+"\n");
        // //System.out.print(hold[4]);
        // br.close();
        Gson gson=new Gson();
        String jsonString = sb.toString();
        System.out.println(jsonString);
        Response resp = gson.fromJson(jsonString, Response.class);

        // response[] options=new response[5];
        // for(int i=0;i<5;i++) {
        //     options[i] = gson.fromJson(hold[i],response.class);
        // }

        //System.out.print(options[0].query);
        return resp;

    }

}
