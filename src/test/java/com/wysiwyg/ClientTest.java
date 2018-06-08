package com.wysiwyg;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.*;

import org.junit.*;
import org.apache.http.impl.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.*;
import org.apache.http.util.*;
import org.json.simple.JSONObject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


public class ClientTest {
  private static final int testSize = 100;
  private static final String fileName = "testFile";
  private boolean[] ackArr = new boolean[testSize];
  private String[] userArr = new String[testSize];
  private String[] userFileArr = new String[testSize];
  private String[] bufferedOps = new String[testSize];
  private String[] outstandingOp = new String[testSize];
  private int[] verArr = new int[testSize];


  public void init()
  {
    String url = "http://localhost:8080/users";
    CloseableHttpClient client = HttpClients.createDefault();
    HttpPost httpPost = null;
    CloseableHttpResponse response1 = null;
    for(int i = 0; i < testSize; i++)
    {
      ackArr[i] = true;
      userArr[i] = "user"+i;
      verArr[i] = 0;
      httpPost = new HttpPost(url+"?user="+userArr[i]);
      try {
          response1 = client.execute(httpPost);
          //Assert.assertTrue(response1.getStatusLine().getStatusCode() == 200);
      } catch (IOException e) {
          //Assert.fail(e.getMessage());
      }
    }

    url = "http://localhost:8080/documents";
    httpPost = new HttpPost(URL+"?name="+fileName);
    try {
        response1 = client.execute(httpPost);
        //Assert.assertTrue(response1.getStatusLine().getStatusCode() == 200);
    } catch (IOException e) {
        //Assert.fail(e.getMessage());
    }
  }

  //all users post same payload at same pos at almost the same time
  public void post(String payload, int pos)
  {
    for(int i = 0; i < testSize; i++)
    {
      String op = "INSERT";
      if(payload == "Backspace")
      {
        payload = "";
        op = "DELETE";
      }
      else if(payload.length() != 1) {
        return;
      }

      JSONObject obj = new JSONObject();

      obj.put("documentId", fileName);
      obj.put("pos", pos);
      obj.put("payload", payload);
      obj.put("opcode", op);
      obj.put("uid", userArr[i]);
      obj.put("version", verArr[i]);

      if(ackArr[i])
      {
        ackArr[i] = false;

        StringWriter out = new StringWriter();
        obj.writeJSONString(out);
        String jsonText = out.toString();
        outstandingOp[i] = jsonText;

        String url = "http://localhost:8080/documents/op";
        CloseableHttpClient client = HttpClients.createDefault();
        CloseableHttpResponse response1 = null;
        HttpPost httpPost = new HttpPost(url);
        StringEntity params =new StringEntity(jsonText);
        httpPost.addHeader("content-type", "application/json");
        httpPost.setEntity(params);
        try {
            response1 = client.execute(httpPost);
            //Assert.assertTrue(response1.getStatusLine().getStatusCode() == 200);
        } catch (IOException e) {
            //Assert.fail(e.getMessage());
        }
      }
      else
      {
        //TODO: don't know if we have to create a Data class
        Gson g = new Gson();
        Data d = g.fromJson(bufferedOps[i], Data.class);
        d.push(obj);
      }
    }
  }

  public void autoUpdate()
  {
    String url = "http://localhost:8080/documents/op";
    CloseableHttpClient client = HttpClients.createDefault();
    HttpGet httpGet= null;
    CloseableHttpResponse response1 = null;
    for(int i = 0; i < testSize; i++)
    {
      httpGet = new HttpGet(url+"?documentId="+ fileName + "&version=" + verArr[i]);
      try {
          //TODO: not sure if it is the way to get op in java
          response1 = client.execute(httpGet);
          HttpEntity entity1 = response1.getEntity();
          String data = EntityUtils.toString(entity1);
          _autoupdate(data, i);
          //Assert.assertTrue(response1.getStatusLine().getStatusCode() == 200);
      } catch (IOException e) {
          //Assert.fail(e.getMessage());
      }

    }
  }


  public void _autoupdate(String data, int userIndex)
  {
      boolean receiveAck = false;
      Gson g = new Gson();
      //TODO: not sure if it is the correct way to use json in java
      Data ret = g.fromJson(data, Data.class);
      for(int i = 0; i < ret.length(); i++)
      {
        String op = ret[i].opcode;
        int pos = ret[i].pos;
        String payload = ret[i].payload;
        String uid = ret[i].uid;
        int ver = ret[i].version;

        int curVer = verArr[userIndex];
        verArr[userIndex] = curVer+1;

        if(uid.equals(userArr[userIndex]))
        {
          receiveAck = true;
        }
        else
        {
            //TODO
        }

        if(receiveAck)
        {
          //TODO
        }
      }


  }

  //TODO
  public void tii()
  {

  }

  //TODO
  public void tid()
  {

  }

  //TODO
  public void tdi()
  {

  }

  //TODO
  public void tdd()
  {

  }

  //TODO
  public void transform()
  {

  }

  //TODO
  public void transformMultiple()
  {

  }
}
