package com.wysiwyg;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.*;
import java.lang.StringBuilder;
import java.util.Arrays;

import org.junit.*;
import org.apache.http.impl.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.*;
import org.apache.http.util.*;
import org.apache.http.entity.StringEntity;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;


public class ClientTest {
    private static final int CLIENT_AMOUNT = 10;
    private static final String FILE_NAME = "testFile";
    List<Boolean> ackList = new ArrayList<Boolean>();
    List<String> userList = new ArrayList<String>();
    List<Integer> verList = new ArrayList<Integer>();
    List<Data> outstandingOp = new ArrayList<Data>();
    List<List<Data>> bufferedOps = new ArrayList<List<Data>>();
    List<StringBuilder> texts = new ArrayList<StringBuilder>();

    public void init() {
        String url = "http://localhost:8080/users";
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = null;
        CloseableHttpResponse response1 = null;
        for (int i = 0; i < CLIENT_AMOUNT; i++) {
            ackList.add(Boolean.TRUE);
            userList.add(String.format("user%d", i));
            verList.add(new Integer(0));
            outstandingOp.add(null);
            bufferedOps.add(new ArrayList<Data>());
            texts.add(new StringBuilder(""));

            httpPost = new HttpPost(url+"?user="+userList.get(i));
            try {
                response1 = client.execute(httpPost);
                Assert.assertTrue(response1.getStatusLine().getStatusCode() == 200);
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
        }
        // System.out.println(verList);
        url = "http://localhost:8080/documents";
        httpPost = new HttpPost(url+"?name="+FILE_NAME);
        try {
            response1 = client.execute(httpPost);
            Assert.assertTrue(response1.getStatusLine().getStatusCode() == 200);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    // same payload at same pos at almost the same time
    public void post(String payload, int pos) {
        for (int i = 0; i < CLIENT_AMOUNT; i++) {
            String op = "INSERT";
            if (payload.equals("Backspace")) {
                payload = "";
                op = "DELETE";
            } else if(payload.length() != 1) {
                return;
            }

            if (op.equals("INSERT")) {
                texts.set(i, texts.get(i).insert(pos-1, payload));
            } else if (op.equals("DELETE")) {
                texts.set(i, texts.get(i).delete(pos-1, pos));
            } else if (op.equals("IDENTITY")) {
                continue;
            }

            Data data = new Data(FILE_NAME, 
                                pos-1, 
                                payload, 
                                op, 
                                userList.get(i),
                                verList.get(i).intValue());
            Gson gson = new Gson();

            if (ackList.get(i).booleanValue()) {
                ackList.set(i, Boolean.FALSE);
                outstandingOp.set(i, data);

                String url = "http://localhost:8080/documents/op";
                CloseableHttpClient client = HttpClients.createDefault();
                CloseableHttpResponse response1 = null;
                HttpPost httpPost = new HttpPost(url);
                try {
                    StringEntity params = new StringEntity(gson.toJson(outstandingOp.get(i)));
                    httpPost.addHeader("Content-Type", "application/json");
                    httpPost.setEntity(params);
                    response1 = client.execute(httpPost);
                    Assert.assertTrue(response1.getStatusLine().getStatusCode() == 200);
                } catch (IOException e) {
                    Assert.fail(e.getMessage());
                }
            } else {
                bufferedOps.get(i).add(data);
            }
        }
    }

    public void autoUpdate() {
        String url = "http://localhost:8080/documents/op";
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet httpGet= null;
        CloseableHttpResponse response1 = null;
        for (int i = 0; i < CLIENT_AMOUNT; i++) {
            httpGet = new HttpGet(url + "?documentId=" + FILE_NAME + "&version=" + verList.get(i).toString());
            try {
                response1 = client.execute(httpGet);
                HttpEntity entity1 = response1.getEntity();
                String data = EntityUtils.toString(entity1);
                Assert.assertTrue(response1.getStatusLine().getStatusCode() == 200);
                _autoupdate(data, i);
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }

        }
    }


    public void _autoupdate(String raw, int userIndex) {
        boolean receiveAck = false;
        Gson gson = new Gson();
        Type listType = new TypeToken<List<JsonObject>>(){}.getType();
        List<JsonObject> datas = gson.fromJson(raw, listType);
        for(int i = 0; i < datas.size(); i++) {
            JsonObject json = datas.get(i);
            Data data = new Data(json.get("documentId").getAsString(),
                                json.get("pos").getAsInt(),
                                json.get("payload").getAsString(),
                                json.get("opcode").getAsString(),
                                json.get("uid").getAsString(),
                                json.get("version").getAsInt());
            String op = data.opcode;
            int pos = data.pos;
            String payload = data.payload;
            String uid = data.uid;
            int ver = data.version;

            int curVer = verList.get(userIndex).intValue();
            verList.set(userIndex, new Integer(curVer+1));

            if (uid.equals(userList.get(userIndex))) {
                receiveAck = true;
            } else {
                Data outstanding = outstandingOp.get(userIndex);
                List<Data> bufferedOp = bufferedOps.get(userIndex);
                List<Data> transformL = transform(outstanding, data);

                Data outstandingT = transformL.get(0);
                Data operationT = transformL.get(1);

                Tuple transformLL = transformMultiple(bufferedOp, operationT);
                bufferedOp = transformLL.first;
                Data operationTT = transformLL.second;

                outstandingOp.set(userIndex, outstandingT);
                bufferedOps.set(userIndex, bufferedOp);

                if (operationTT.opcode.equals("DELETE")) {
                    texts.set(userIndex, texts.get(userIndex).delete(operationTT.pos, operationTT.pos+1));
                } else if (operationTT.opcode.equals("INSERT")){
                    int newPos = Math.max(0, Math.min(operationTT.pos, texts.get(userIndex).length()));
                    // System.out.println(newPos + ", " + payload.charAt(0));
                    texts.set(userIndex, texts.get(userIndex).insert(newPos, payload.charAt(0)));
                } else if (operationTT.opcode.equals("IDENTITY")) {
                    continue;
                }
            }

            if (receiveAck) {
                List<Data> bufferedOp = bufferedOps.get(userIndex);
                if (bufferedOp.size() == 0) {
                    ackList.set(userIndex, Boolean.TRUE);
                    outstandingOp.set(userIndex, null);
                    bufferedOps.set(userIndex, new ArrayList<Data>());
                } else {
                    data = bufferedOps.get(userIndex).get(0);
                    data.version = verList.get(userIndex).intValue();
                    outstandingOp.set(userIndex, data);
                    // POST
                    String url = "http://localhost:8080/documents/op";
                    CloseableHttpClient client = HttpClients.createDefault();
                    HttpPost httpPost = new HttpPost(url);
                    try {
                        StringEntity params = new StringEntity(gson.toJson(outstandingOp.get(userIndex)));
                        httpPost.addHeader("Content-Type", "application/json");
                        httpPost.setEntity(params);
                        CloseableHttpResponse response1 = client.execute(httpPost);
                        Assert.assertTrue(response1.getStatusLine().getStatusCode() == 200);
                    } catch (IOException e) {
                        Assert.fail(e.getMessage());
                    }

                    List<Data> bufferTmp = bufferedOps.get(userIndex);
                    bufferTmp.remove(0);
                    bufferedOps.set(userIndex, bufferTmp);
                }
            }
        }
    }

    public List<Data> tii(Data p, Data q) {
        if (p.pos < q.pos || (p.pos == q.pos && p.uid.compareTo(q.uid) > 0)) {
            return Arrays.asList(
                new Data(p.documentId, 
                        p.pos, 
                        p.payload,
                        p.opcode,
                        p.uid,
                        p.version),
                new Data(q.documentId, 
                        q.pos+1, 
                        q.payload,
                        q.opcode,
                        q.uid,
                        q.version)
            );
        } else {
            return Arrays.asList(
                new Data(p.documentId, 
                        p.pos+1, 
                        p.payload,
                        p.opcode,
                        p.uid,
                        p.version),
                new Data(q.documentId, 
                        q.pos, 
                        q.payload,
                        q.opcode,
                        q.uid,
                        q.version)
            );
        }
    }

    public List<Data> tid(Data p, Data q) {
        if (p.pos <= q.pos) {
            return Arrays.asList(
                new Data(p.documentId, 
                        p.pos, 
                        p.payload,
                        p.opcode,
                        p.uid,
                        p.version),
                new Data(q.documentId, 
                        q.pos+1, 
                        q.payload,
                        q.opcode,
                        q.uid,
                        q.version)
            );
        } else {
            return Arrays.asList(
                new Data(p.documentId, 
                        p.pos-1, 
                        p.payload,
                        p.opcode,
                        p.uid,
                        p.version),
                new Data(q.documentId, 
                        q.pos, 
                        q.payload,
                        q.opcode,
                        q.uid,
                        q.version)
            );
        }
    }

    public List<Data> tdi(Data p, Data q) {
        if (p.pos < q.pos) {
            return Arrays.asList(
                new Data(p.documentId, 
                        p.pos, 
                        p.payload,
                        p.opcode,
                        p.uid,
                        p.version),
                new Data(q.documentId, 
                        q.pos-1, 
                        q.payload,
                        q.opcode,
                        q.uid,
                        q.version)
            );
        } else {
            return Arrays.asList(
                new Data(p.documentId, 
                        p.pos+1, 
                        p.payload,
                        p.opcode,
                        p.uid,
                        p.version),
                new Data(q.documentId, 
                        q.pos, 
                        q.payload,
                        q.opcode,
                        q.uid,
                        q.version)
            );
        }
    }

    public List<Data> tdd(Data p, Data q) {
        if (p.pos < q.pos) {
            return Arrays.asList(
                new Data(p.documentId, 
                        p.pos, 
                        p.payload,
                        p.opcode,
                        p.uid,
                        p.version),
                new Data(q.documentId, 
                        q.pos-1, 
                        p.payload,
                        p.opcode,
                        p.uid,
                        p.version)
            );
        } else if (p.pos > q.pos) {
            return Arrays.asList(
                new Data(p.documentId, 
                        p.pos-1, 
                        p.payload,
                        p.opcode,
                        p.uid,
                        p.version),
                new Data(q.documentId, 
                        q.pos, 
                        q.payload,
                        q.opcode,
                        q.uid,
                        q.version)
            );
        } else {
            return Arrays.asList(
                new Data("IDENTITY"),
                new Data("IDENTITY")
            );
        }
    }

    public List<Data> transform(Data p, Data q) {
        if (p == null || q == null) {
            return Arrays.asList(p, q);
        } 
        if (p.opcode.equals("INSERT") && q.opcode.equals("INSERT")) {
            return tii(p, q);
        } else if (p.opcode.equals("INSERT") && q.opcode.equals("DELETE")) {
            return tid(p, q);
        } else if (p.opcode.equals("DELETE") && q.opcode.equals("INSERT")) {
            return tdi(p, q);
        } else if (p.opcode.equals("DELETE") && q.opcode.equals("DELETE")) {
            return tdd(p, q);
        }
        return Arrays.asList(p, q);
    }

    public Tuple transformMultiple(List<Data> ps, Data q) {
        List<Data> ret = new ArrayList<Data>();
        if (q == null || ps.size() == 0) {
            return new Tuple(ps,q);
        }
        for (int i = 0; i < ps.size(); i++) {
            List<Data> transformL = transform(ps.get(i), q);
            ret.add(transformL.get(0));
            q = transformL.get(1);
        }
        return new Tuple(ret, q);
    }

    class Data {
        String documentId;
        int pos;
        String payload;
        String opcode;
        String uid;
        int version;

        public Data(String documentId,
                    int pos,
                    String payload,
                    String opcode,
                    String uid,
                    int version) {
            this.documentId = documentId;
            this.pos = pos;
            this.payload = payload;
            this.opcode = opcode;
            this.uid = uid;
            this.version = version;
        }

        public Data(String opcode) {
            this.opcode = opcode;
        }
    }

    class Tuple {
        List<Data> first;
        Data second;

        public Tuple(List<Data> first, Data second) {
            this.first = first;
            this.second = second;
        }
    }

    @Test
    public void integrationTest() {
        final ClientTest clientTest = new ClientTest();
        clientTest.init();
        int numberOfChar = 10;
        for (int i = 1; i <= numberOfChar; i++) {
            clientTest.post(String.format("%d", i), 1);
        }

        new Thread(){
            @Override
            public void run() {
                while (true){
                    clientTest.autoUpdate();
                }
            }
        }.start();

        try {
            Thread.sleep(5000);
        } catch (Exception e) {

        }
        for (int i = 0; i < CLIENT_AMOUNT-1; i++) {
            // System.out.println(clientTest.texts.get(i));
            // System.out.println(clientTest.texts.get(i+1));
            // System.out.println("-------");
            Assert.assertTrue(clientTest.texts.get(i).toString().equals(clientTest.texts.get(i+1).toString()));
        }
    }
}
