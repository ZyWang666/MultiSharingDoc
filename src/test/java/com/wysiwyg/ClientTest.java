package com.wysiwyg;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.ArrayList;
import java.lang.Math;
import java.lang.reflect.*;
import java.lang.StringBuilder;
import java.lang.InterruptedException;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.*;

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
    class Client implements Runnable {
        String[] USER_URL;      
        String[] OP_URL;
        // "http://localhost:8080/documents/op";
        static final int NUMBER_OF_CHARS = 2;
        boolean ack;
        String userId;
        int ver;
        Data outstandingOp;
        List<Data> bufferedOps;
        StringBuilder text;
        boolean shouldPost;
        int index;
        /*  To emulate a js single threaded mechanism,
            the state is either in 'receiving' or 'posting',
            but not interleave. 
         */
        Lock stateMachineLock;

        String word;
        /*  Lock itself does not offer waiting queue (lame).
            So use conditionVariable instead to queue things up, 
            otherwise post() will starve.    
         */
        // Condition conditionVariable;

        public Client(String userId, boolean shouldPost, String word, int index) {
            this.ack = true;
            this.userId = userId;
            this.ver = 0;
            this.outstandingOp = null;
            this.bufferedOps = new ArrayList<Data>();
            this.text = new StringBuilder("");
            this.shouldPost = shouldPost;
            this.word = word;
            /* A fair lock */
            this.stateMachineLock = new ReentrantLock(true);
            this.index = index;
            USER_URL = new String[2];
            OP_URL = new String[2];
            USER_URL[0] = "http://localhost:6000/users";
            USER_URL[1] = "http://localhost:5000/users";
            OP_URL[0] = "http://localhost:6000/documents/op";
            OP_URL[1] = "http://localhost:5000/documents/op";
        }

        public Client init() {
            for (int i = 0; i < 2; i++) {
                CloseableHttpClient client = HttpClients.createDefault();
                HttpPost httpPost = new HttpPost(USER_URL[i] + "?user=" + this.userId);
                try {
                    CloseableHttpResponse response1 = client.execute(httpPost);
                    Assert.assertTrue(response1.getStatusLine().getStatusCode() == 200);
                } catch (IOException e) {
                    Assert.fail(e.getMessage());
                }
            }
            return this;
        }

        public void post(String payload, int pos) {
            // System.err.println("Before acquiring the lock");
            // stateMachineLock.lock();
            // System.err.printf("%s posting %s at %d\n", userId, payload, pos);
            String op = "INSERT";
            if (payload.equals("Backspace")) {
                payload = "";
                op = "DELETE";
            } else if(payload.length() != 1) {
                return;
            }

            if (op.equals("INSERT")) {
                text.insert(pos-1, payload);
            } else if (op.equals("DELETE")) {
                text.delete(pos-1, pos);
            } else if (op.equals("IDENTITY")) {
                return;
            }

            Data data = new Data(FILE_NAME, 
                                pos-1, 
                                payload, 
                                op, 
                                userId,
                                ver);
            Gson gson = new Gson();

            if (ack) {
                ack = false;
                outstandingOp = data;

                CloseableHttpClient client = HttpClients.createDefault();
                HttpPost httpPost = new HttpPost(OP_URL[index]);
                try {
                    StringEntity params = new StringEntity(gson.toJson(outstandingOp));
                    httpPost.addHeader("Content-Type", "application/json");
                    httpPost.setEntity(params);
                    CloseableHttpResponse response1 = client.execute(httpPost);
                    Assert.assertTrue(response1.getStatusLine().getStatusCode() == 200);
                } catch (IOException e) {
                    Assert.fail(e.getMessage());
                }
            } else {
                bufferedOps.add(data);
            }
            // stateMachineLock.unlock();
        }

        class ClientAutoUpdate implements Runnable {
            @Override
            public void run() {
                while (true) {
                    this.autoUpdate();
                }
            }

            public void autoUpdate() {
                // stateMachineLock.lock();
                // System.err.println("???");
                CloseableHttpClient client = HttpClients.createDefault();
                HttpGet httpGet = new HttpGet(OP_URL[index] + "?documentId=" + FILE_NAME + "&version=" + new Integer(ver).toString());
                try {
                    CloseableHttpResponse response1 = client.execute(httpGet);
                    HttpEntity entity1 = response1.getEntity();
                    String data = EntityUtils.toString(entity1);
                    // System.out.println(response1.getStatusLine().getStatusCode());
                    // Assert.assertTrue(response1.getStatusLine().getStatusCode() == 200);
                    _autoupdate(data);
                } catch (IOException e) {
                    Assert.fail(e.getMessage());
                }
                // stateMachineLock.unlock();
            }

            public void _autoupdate(String raw) {
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
                    ver += 1;

                    if (data.uid.equals(userId)) {
                        receiveAck = true;
                    } else {
                        List<Data> transformL = transform(outstandingOp, data);

                        Data outstandingT = transformL.get(0);
                        Data operationT = transformL.get(1);

                        Tuple transformLL = transformMultiple(bufferedOps, operationT);
                        bufferedOps = transformLL.first;
                        Data operationTT = transformLL.second;

                        // System.out.printf("%s insert %s at %d pos\n", userId, operationTT.payload, operationTT.pos);
                        if (operationTT.opcode.equals("DELETE")) {
                            text.delete(operationTT.pos, operationTT.pos+1);
                        } else if (operationTT.opcode.equals("INSERT")){
                            text.insert(operationTT.pos, operationTT.payload.charAt(0));
                        } else if (operationTT.opcode.equals("IDENTITY")) {
                            continue;
                        }
                    }

                    if (receiveAck) {
                        if (bufferedOps.size() == 0) {
                            ack = true;
                            outstandingOp = null;
                            bufferedOps = new ArrayList<Data>();
                        } else {
                            data = bufferedOps.get(0);
                            data.version = ver;
                            outstandingOp = data;
                            CloseableHttpClient client = HttpClients.createDefault();
                            HttpPost httpPost = new HttpPost(OP_URL[index]);
                            try {
                                StringEntity params = new StringEntity(gson.toJson(outstandingOp));
                                httpPost.addHeader("Content-Type", "application/json");
                                httpPost.setEntity(params);
                                CloseableHttpResponse response1 = client.execute(httpPost);
                                Assert.assertTrue(response1.getStatusLine().getStatusCode() == 200);
                            } catch (IOException e) {
                                Assert.fail(e.getMessage());
                            }
                            bufferedOps.remove(0);
                        }
                    }
                }
            }
        }
        
        public void run() {
            // System.out.printf("Client %s running\n", this.userId);
            for (int i = 0; i < NUMBER_OF_CHARS; i++) {
                if (shouldPost) {
                    this.post(String.format("%d", i%10), 1);
                    // this.post(this.word, 1);
                }
            }
            new Thread(new ClientAutoUpdate()).start();
        }
    }

    static final int CLIENT_AMOUNT = 2;
    static final String FILE_NAME = "File";
    // static final String DOCUMENT_URL = "http://localhost:5000/documents";

    public ClientTest init() {
        String[] DOCUMENT_URL = new String[2];
        DOCUMENT_URL[0] = "http://localhost:5000/documents";
        DOCUMENT_URL[1] = "http://localhost:6000/documents";
        for (int i = 0; i < 2; i++) {
            HttpPost httpPost = new HttpPost(DOCUMENT_URL[i] + "?name=" + FILE_NAME);
            CloseableHttpClient client = HttpClients.createDefault();
            try {
                CloseableHttpResponse response1 = client.execute(httpPost);
                Assert.assertTrue(response1.getStatusLine().getStatusCode() == 200);
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
        }
        return this;
    }

    public List<Data> tii(Data p, Data q) {
        // if (p.pos < q.pos || (p.pos == q.pos && p.uid.compareTo(q.uid) > 0)) {
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
                        q.payload,
                        q.opcode,
                        q.uid,
                        q.version)
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
        public String documentId;
        public int pos;
        public String payload;
        public String opcode;
        public String uid;
        public int version;

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
        public List<Data> first;
        public Data second;

        public Tuple(List<Data> first, Data second) {
            this.first = first;
            this.second = second;
        }
    }

    @Test
    public void integrationTest() {
        List<Client> clientList = new ArrayList<Client>();
        for (int i = 0; i < CLIENT_AMOUNT; i++) {
            clientList.add(new Client(String.format("user%d", i), true, String.format("%d", i%10), i%2)
                            .init()
                        );
        }
        // clientList.add(new Client(String.format("user%d", CLIENT_AMOUNT-1), false, "9")
                        // .init()
                    // );
        new ClientTest().init();

        for (int i = 0; i < CLIENT_AMOUNT; i++) {
            new Thread(clientList.get(i)).start();
        }

        // try {
        //     Thread.sleep(40000);
        // } catch (Exception e) {

        // }
        long beginTime = System.nanoTime();
        System.out.println(beginTime);
        // System.out.println(beginTime);
        int finishCount = 0;
        while (true) {
            for (int i = 0; i < CLIENT_AMOUNT; i++) {
                // System.out.println(clientList.get(i).text.length());
                if (clientList.get(i).text.length() == CLIENT_AMOUNT * Client.NUMBER_OF_CHARS) {
                    finishCount++;
                }
            }
            // System.out.println(finishCount);
            if (finishCount == CLIENT_AMOUNT) {
                break;
            }
            finishCount = 0;
        }
        // long endTime = System.nanoTime();

        // System.out.println((endTime - beginTime) / Math.pow(10, 9));
        // double totalTime = (endTime - beginTime) / Math.pow(10, 9);
        // try {
        //     // PrintWriter printWriter = new PrintWriter("SingleBackendPerformance.txt");
        //     // printWriter.append(new StringBuffer(String.format("%d %d %f\n", CLIENT_AMOUNT, Client.NUMBER_OF_CHARS, totalTime)));
        //     // printWriter.println(String.format("%d %d %f\n", CLIENT_AMOUNT, Client.NUMBER_OF_CHARS, totalTime));
        //     FileWriter fileWriter = new FileWriter(new File("SingleBackendPerformance.txt"), true);
        //     fileWriter.append(new StringBuffer(String.format("%d %d %f\n", CLIENT_AMOUNT, Client.NUMBER_OF_CHARS, totalTime)));
        //     fileWriter.flush();
        // } catch (IOException e) {
        //     System.err.println("fileNotFound");
        //     try {
        //         new File("SingleBackendPerformance.txt").createNewFile();
        //     } catch (IOException ex) {
        //         System.err.println(ex);
        //     }
        // }

        for (int i = 0; i < CLIENT_AMOUNT-1; i++) {
            // System.out.println(clientList.get(i).text);
            // System.out.println(clientList.get(i+1).text);
            // System.out.println(clientList.get(i).text.toString().equals(clientList.get(i+1).text.toString()));
            // System.out.println("-------");
            Assert.assertTrue(clientList.get(i).text.toString().equals(clientList.get(i+1).text.toString()));
        }
    }
}
