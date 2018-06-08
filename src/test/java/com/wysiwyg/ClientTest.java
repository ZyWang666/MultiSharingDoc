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
  private List<Data> bufferedOps = new ArrayList<Data>();
  private Data[] outstandingOp = new Data[testSize];
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

      Data data = new Data(fileName, pos, payload, op, userArr[i], verArr[i]);
      Gson gson = new Gson();
      String dataString = gson.toJson(data);
      if(ackArr[i])
      {
        ackArr[i] = false;
        outstandingOp[i] = data;

        String url = "http://localhost:8080/documents/op";
        CloseableHttpClient client = HttpClients.createDefault();
        CloseableHttpResponse response1 = null;
        HttpPost httpPost = new HttpPost(url);
        StringEntity params =new StringEntity(dataString);
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
        bufferedops.push(data);
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
          response1 = client.execute(httpGet);
          HttpEntity entity1 = response1.getEntity();
          String dataListString = EntityUtils.toString(entity1);
          Gson gson = new Gson();
          Type listType = new TypeToken<ArrayList<Data>>(){}.getType();
          ArrayList<Data> dataList = gson.fromJson(dataListString, listType);
          _autoupdate(dataList, i);
          //Assert.assertTrue(response1.getStatusLine().getStatusCode() == 200);
      } catch (IOException e) {
          //Assert.fail(e.getMessage());
      }

    }
  }


  public void _autoupdate(ArrayList<Data> dataList, int userIndex)
  {
      boolean receiveAck = false;
      for(int i = 0; i < dataList.size(); i++)
      {
        Data data = dataList.get(i);
        String op = data.opcode;
        int pos = data.pos;
        String payload = data.payload;
        String uid = data.uid;
        int ver = data.version;

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
