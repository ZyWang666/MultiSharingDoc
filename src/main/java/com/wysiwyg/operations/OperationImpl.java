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
        document.documentRope.insert(mutation.pos, new StringBuffer(mutation.payload));
        metadataManager.addDocument(document);
        return true;
    }

    @Override
    public boolean delete(Mutation mutation) {
        return false;
    }
}