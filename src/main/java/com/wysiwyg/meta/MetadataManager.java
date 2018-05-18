package com.wysiwyg.meta;

import java.util.List;

import com.wysiwyg.structs.Document;

public interface MetadataManager {
    public List<Document> listDocument();
    public Document getDocument(String name);
    public void addDocument(Document document);
}