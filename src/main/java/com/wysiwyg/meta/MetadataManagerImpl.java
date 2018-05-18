package com.wysiwyg.meta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.wysiwyg.structs.Document;

public class MetadataManagerImpl implements MetadataManager {
    protected Map<String, Document> documentMap;

    public MetadataManagerImpl() {
        documentMap = new ConcurrentHashMap<String, Document>();
    }

    @Override
    public List<Document> listDocument() {
        return new ArrayList<Document>(documentMap.values());
    }

    @Override
    public Document getDocument(String name) {
        return documentMap.get(name);
    }

    @Override
    public void addDocument(Document document) {
        documentMap.put(document.getDocumentId(), document);
    }    
}