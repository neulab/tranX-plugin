package edu.cmu.tranx;

/**
 * Created by williamqian on 7/6/17.
 */



import com.google.gson.Gson;
import jdk.nashorn.internal.parser.JSONParser;
import org.json.JSONObject;

import java.io.BufferedReader;
//import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpClient{
    public static class response {
        int id;
        String query="hi";
        double score;

    }

    //public static response[] sendData(String buf, String username, String password=null){
    public static response[] sendData(String buf) {
        try{
            // System.out.print(username+password);
            JSONObject query=new JSONObject();
            query.put("query",buf);
            // query.put("username",username);
            // query.put("passwd",password);

            URL url=new URL("http://tardigrade.andrew.cmu.edu:40300/api/request");
            HttpURLConnection httpcon=(HttpURLConnection)url.openConnection();
            httpcon.setDoOutput(true);
            httpcon.setDoInput(true);
            httpcon.setRequestMethod("POST");
            httpcon.setRequestProperty("Content-Type","application/json");

            OutputStream output=httpcon.getOutputStream();

            output.write(query.toString().getBytes("UTF-8"));
            output.flush();
            output.close();
            httpcon.disconnect();

            StringBuilder sb= new StringBuilder();
            //int HttpResult=httpcon.getResponseCode();
            //if(HttpResult==HttpURLConnection.HTTP_OK) {


            BufferedReader br= new BufferedReader( new InputStreamReader(httpcon.getInputStream(),
                                                    "UTF-8"));
            String line=null;
            while((line=br.readLine())!=null){
                sb.append(line);
            }
            sb.deleteCharAt(0);
            sb.deleteCharAt(sb.lastIndexOf("]"));
            //System.out.print("{"+sb.toString()+"}");
            String[] hold=(sb.toString()).split("},");
            //System.out.print(sb.toString());

            for(int i=0;i<4;i++) {
                hold[i]=hold[i]+"}";

            }
            //System.out.print(hold[0]+"\n");
            //System.out.print(hold[4]);
            br.close();
            Gson gson=new Gson();

            response[] options=new response[5];
            for(int i=0;i<5;i++) {
                options[i] = gson.fromJson(hold[i],response.class);

            }
            //System.out.print(options[0].query);
            return options;
           // }
           // else{
           //     System.out.println(httpcon.getResponseMessage());
          //      return "Error could not return string";
           // }
        }
        catch(Exception e){
            System.out.print("There was an error");
            response[] sample=new response[5];
            for(int i=0;i<5;i++){
                /*response a=new response();
                a.id=0;
                a.query="Server is not running currently";
                a.score=0;
                //sample[i]=
                sample[i]=a;
                System.out.print("check2");*/
                //sample[i].query="hello world";
                //sample[i].score=0;
            }
            return sample;

        }

        //return sample;

    }

}
