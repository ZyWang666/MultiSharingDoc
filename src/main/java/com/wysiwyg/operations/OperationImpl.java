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
        document.documentRope = document.documentRope.insert(
                                Math.min(mutation.pos, document.documentRope.toString().length()), 
                                new StringBuffer(mutation.payload));
        document.version += 1;
        metadataManager.addDocument(document);
        return true;
    }

    @Override
    public boolean delete(Mutation mutation) {
        Document document = metadataManager.getDocument(mutation.documentId);
        document.documentRope = document.documentRope.delete(
                                Math.min(mutation.pos, document.documentRope.toString().length()),
                                1);
        document.version += 1;
        metadataManager.addDocument(document);
        return true;
    }
}