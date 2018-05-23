package com.wysiwyg.structs;

public class Mutation {
    public Opcode opcode;
    public String documentId;
    public int pos;
    public String payload;
    public String uid;
    public long version;

    public Mutation(Opcode op, String id, int p, String c, String uid, long v) {
        opcode = op;
        documentId = id;
        pos = p;
        payload = c;
        this.uid = uid;
        version = v;
    }
}