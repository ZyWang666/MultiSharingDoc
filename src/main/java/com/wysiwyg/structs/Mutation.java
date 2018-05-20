package com.wysiwyg.structs;

public class Mutation {
    public Opcode opcode;
    public String documentId;
    public int pos;
    public String payload;

    public Mutation(Opcode op, String id, int p, String c) {
        opcode = op;
        documentId = id;
        pos = p;
        payload = c;
    }
}