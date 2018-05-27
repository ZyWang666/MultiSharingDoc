package com.wysiwyg.structs;

public class DocumentOutput {
    public String documentId;
    public int ver;
    public String document;

    public DocumentOutput(String documentId, String document, int ver) {
        this.documentId = documentId;
        this.document = document;
        this.ver = ver;
    }
}