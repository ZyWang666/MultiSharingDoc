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
import org.apache.http.entity.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.wysiwyg.structs.Document;
import com.wysiwyg.structs.Mutation;
import com.wysiwyg.structs.Opcode;

public class MutationServletTest {
    private static final String UID             = "user";
    private static final String DOCUMENT_NAME   = "test";
    private static final String DOCUMENT_DATA   = "testData";
    private static final String URL             = "http://localhost:8080/documents/op";

    // @Test
    // public void insertMutationTest() {
    //     MetadataServletTest metadataServletTest = new MetadataServletTest();
    //     metadataServletTest.addDocumentTest();
    //     CloseableHttpClient client = HttpClients.createDefault();
    //     HttpPost httpPost = new HttpPost(URL);

    //     Mutation mutation = new Mutation(Opcode.INSERT, DOCUMENT_NAME, 0, DOCUMENT_DATA, UID, 0);
    //     Gson gson = new Gson();
    //     String json = gson.toJson(mutation);
    //     try {
    //         StringEntity entity = new StringEntity(json);
    //         httpPost.setEntity(entity);
    //     } catch (Exception e) {
    //         Assert.fail(e.getMessage());
    //     }
     
    //     try {
    //         CloseableHttpResponse response1 = client.execute(httpPost);
    //         Assert.assertTrue(response1.getStatusLine().getStatusCode() == 200);
    //     } catch (IOException e) {
    //         Assert.fail(e.getMessage());
    //     }

    //     HttpGet httpGet = new HttpGet("http://localhost:8080/documents/d?documentId="+DOCUMENT_NAME);
    //     try {
    //         CloseableHttpResponse response1 = client.execute(httpGet);
    //         Assert.assertTrue(response1.getStatusLine().getStatusCode() == 200);
    //         HttpEntity entity1 = response1.getEntity();
    //         String bodyAsString = EntityUtils.toString(entity1);
    //         System.out.println(bodyAsString);
    //         Assert.assertTrue(bodyAsString.contains(new StringBuffer(DOCUMENT_DATA)));
    //         // and ensure it is fully consumed (this is how stream is released.
    //         EntityUtils.consume(entity1);
    //     } catch (IOException e) {
    //         Assert.fail(e.getMessage());
    //     }
    // }

    // @Test
    // public void deleteDocumentTest() {
    //     MetadataServletTest metadataServletTest = new MetadataServletTest();
    //     metadataServletTest.addDocumentTest();
    //     modifyDocumentTest();
    //     CloseableHttpClient client = HttpClients.createDefault();
    //     HttpPost httpPost = new HttpPost(URL);


    //     String json = String.format("{\"documentId\":\"%s\",\"pos\":%d,\"payload\": %s,\"op\": %s, \"ver\":%d, \"uid\":%s}", 
    //         DOCUMENT_NAME, 0, "payload", "del", 3, UID);
    //     try {
    //         StringEntity entity = new StringEntity(json);
    //         httpPost.setEntity(entity);
    //     } catch (Exception e) {
    //         Assert.fail(e.getMessage());
    //     }
     
    //     try {
    //         CloseableHttpResponse response1 = client.execute(httpPost);
    //         Assert.assertTrue(response1.getStatusLine().getStatusCode() == 200);
    //     } catch (IOException e) {
    //         Assert.fail(e.getMessage());
    //     }

    //     HttpGet httpGet = new HttpGet(URL+"?documentId="+DOCUMENT_NAME);
    //     try {
    //         CloseableHttpResponse response1 = client.execute(httpGet);
    //         Assert.assertTrue(response1.getStatusLine().getStatusCode() == 200);
    //         HttpEntity entity1 = response1.getEntity();
    //         String bodyAsString = EntityUtils.toString(entity1);
    //         System.out.println(bodyAsString);
    //         Assert.assertTrue(bodyAsString.contains(new StringBuffer("estData")));
    //         // and ensure it is fully consumed (this is how stream is released.
    //         EntityUtils.consume(entity1);
    //     } catch (IOException e) {
    //         Assert.fail(e.getMessage());
    //     }
    // }

    // @Test
    // public void getDocumentTest() {
    //     modifyDocumentTest();
    //     CloseableHttpClient client = HttpClients.createDefault();
    //     HttpGet httpGet = new HttpGet(URL+"?documentId="+DOCUMENT_NAME);
    
    //     try {
    //         CloseableHttpResponse response1 = client.execute(httpGet);
    //         Assert.assertTrue(response1.getStatusLine().getStatusCode() == 200);
    //         HttpEntity entity1 = response1.getEntity();
    //         String bodyAsString = EntityUtils.toString(entity1);
    //         Assert.assertTrue(bodyAsString.contains(new StringBuffer(DOCUMENT_DATA)));
    //         // and ensure it is fully consumed (this is how stream is released.
    //         EntityUtils.consume(entity1);
    //     } catch (IOException e) {
    //         Assert.fail(e.getMessage());
    //     }
    // }
}
