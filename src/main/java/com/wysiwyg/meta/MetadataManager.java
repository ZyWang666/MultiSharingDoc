package com.wysiwyg.meta;

import java.util.List;

import com.wysiwyg.structs.Document;

public interface MetadataManager {
    public List<String> listUser();
    public boolean addUser(String user);
    public List<Document> listDocument();
    public Document getDocument(String name);
    public void addDocument(Document document);
}