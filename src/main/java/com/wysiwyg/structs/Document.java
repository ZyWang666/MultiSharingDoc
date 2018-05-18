package com.wysiwyg.structs;

import org.ahmadsoft.ropes.Rope;

public class Document {
    protected String documentId;
    protected SyncInfo syncInfo;
    protected Rope document;

    public Document(String documentId) {
        this.documentId = documentId;
    }

    public String getDocumentId() {
        return documentId;
    }
}