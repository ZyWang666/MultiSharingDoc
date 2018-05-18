package com.wysiwyg.structs;

public class Document {
    protected String documentId;
    protected SyncInfo syncInfo;

    public Document(String documentId) {
        this.documentId = documentId;
    }

    public String getDocumentId() {
        return documentId;
    }
}