package com.wysiwyg.structs;

public class Mutation {
    public Opcode opcode;
    public String documentId;
    public int pos;
    public String payload;
    public String uid;
    public int version;
    public int indexInMutationHistory;

    public final static Mutation IDENTITY = new Mutation(Opcode.IDENTITY);

    public Mutation(Opcode op) {
        opcode = op;
    }

    public Mutation(Opcode op, String id, int p, String c, String uid, int v) {
        opcode = op;
        documentId = id;
        pos = p;
        payload = c;
        this.uid = uid;
        version = v;
    }
}