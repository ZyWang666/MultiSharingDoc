package com.wysiwyg.structs;

public class Mutation {
    protected Opcode opcode;
    protected String documentId;
    protected int pos;
    protected String payload;
    protected SyncInfo syncInfo;

    public Opcode getOpcode() {
        return opcode;
    }
}