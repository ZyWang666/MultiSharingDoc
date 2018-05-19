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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.wysiwyg.structs.Document;

public class DocumentServletTest {
    private static final String DOCUMENT_NAME   = "test";
    private static final String DOCUMENT_DATA   = "testData";
    private static final String URL             = "http://localhost:8080/documents/d";
    @Test
    public void addDocumentTest() {
        MetadataServletTest metadataServletTest = new MetadataServletTest();
        metadataServletTest.addDocumentTest();
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(URL+ "?documentId="+DOCUMENT_NAME
                                            + "&pos=0"
                                            + "&payload="+DOCUMENT_DATA
                                            + "&op=ins");
        try {
            CloseableHttpResponse response1 = client.execute(httpPost);
            Assert.assertTrue(response1.getStatusLine().getStatusCode() == 200);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void getDocumentTest() {
        addDocumentTest();
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(URL+"?documentId="+DOCUMENT_NAME);
    
        try {
            CloseableHttpResponse response1 = client.execute(httpGet);
            Assert.assertTrue(response1.getStatusLine().getStatusCode() == 200);
            HttpEntity entity1 = response1.getEntity();
            String bodyAsString = EntityUtils.toString(entity1);
            System.out.println("bodyAsString " + bodyAsString);
            Assert.assertTrue(bodyAsString.contains(new StringBuffer(DOCUMENT_DATA)));
            // and ensure it is fully consumed (this is how stream is released.
            EntityUtils.consume(entity1);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }
}
