package com.wysiwyg.structs;

import java.lang.StringBuffer;

import org.ahmadsoft.ropes.Rope;
import org.ahmadsoft.ropes.impl.FlatCharSequenceRope;

public class Document {
    public String documentId;
    public int ver;
    public Rope documentRope;

    public Document(String documentId) {
        this.documentId = documentId;
        documentRope = new FlatCharSequenceRope(new StringBuffer());
        ver = 0;
    }
}