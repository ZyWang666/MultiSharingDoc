package com.wysiwyg.operations;

import com.wysiwyg.meta.MetadataManager;
import com.wysiwyg.meta.MetadataManagerImpl;
import com.wysiwyg.structs.Document;
import com.wysiwyg.structs.Mutation;

public class OperationImpl implements Operation {
    protected MetadataManager metadataManager;

    public OperationImpl() {
        metadataManager = new MetadataManagerImpl();
    }

    @Override
    public boolean insert(Mutation mutation) {
        Document document = metadataManager.getDocument(mutation.documentId);
        int pos = Math.min(mutation.pos, document.documentRope.toString().length());
        pos = Math.max(0, mutation.pos);
        document.documentRope = document.documentRope.insert(pos, new StringBuffer(mutation.payload));
        document.ver += 1;
        metadataManager.addDocument(document);
        return true;
    }

    @Override
    public boolean delete(Mutation mutation) {
        Document document = metadataManager.getDocument(mutation.documentId);
        int pos = Math.min(mutation.pos, document.documentRope.toString().length());
        pos = Math.max(0, mutation.pos);
        document.documentRope = document.documentRope.delete(pos, pos+1);
        System.out.printf("delete, pos is %d, length is %d, content is: %s\n", 
            mutation.pos, document.documentRope.toString().length(), document.documentRope.toString());
        document.ver += 1;
        metadataManager.addDocument(document);
        return true;
    }
}