package com.wysiwyg.structs;

import java.util.LinkedList;
import java.util.Queue;

public class Mutation {
    public Opcode opcode;
    public String documentId;
    public int pos;
    public String payload;
    public String uid;
    public int version;
    public int indexInMutationHistory;
    public SyncInfo syncInfo;

    public final static Mutation IDENTITY = new Mutation(Opcode.IDENTITY);

    public Mutation(Opcode op) {
        opcode = op;
    }

    public Mutation(Opcode op, String id, int p, String c, String uuid, int v) {
        opcode = op;
        documentId = id;
        pos = p;
        payload = c;
        uid = uuid;
        version = v;
        syncInfo = new SyncInfo();
    }

    // construct setOrder message 
    public Mutation(int uid, LinkedList<Integer> order) {
        syncInfo = new SyncInfo(uid, order);
    }
}
