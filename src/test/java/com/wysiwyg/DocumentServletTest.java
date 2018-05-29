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

public class DocumentServletTest {
    private static final String UID             = "user";
    private static final String DOCUMENT_NAME   = "test";
    private static final String DOCUMENT_DATA   = "testData";
    private static final String URL             = "http://localhost:8080/documents/d";


    @Test
    public void getDocumentTest() {
        MetadataServletTest metadataServletTest = new MetadataServletTest();
        metadataServletTest.addDocumentTest();
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(URL+"?documentId="+DOCUMENT_NAME);
    
        try {
            CloseableHttpResponse response1 = client.execute(httpGet);
            Assert.assertTrue(response1.getStatusLine().getStatusCode() == 200);
            HttpEntity entity1 = response1.getEntity();
            String bodyAsString = EntityUtils.toString(entity1);
            Assert.assertTrue(bodyAsString.contains(new StringBuffer(DOCUMENT_NAME)));
            // and ensure it is fully consumed (this is how stream is released.
            EntityUtils.consume(entity1);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }
}
