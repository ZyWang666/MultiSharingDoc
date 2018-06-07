package com.wysiwyg.meta;

import java.util.List;

import com.wysiwyg.structs.Document;
import com.wysiwyg.structs.Mutation;

public interface MetadataManager {
    public List<String> listUser();
    public boolean addUser(String user);
    public List<Document> listDocument();
    public Document getDocument(String name);
    public void addDocument(Document document);
    public List<Mutation> getMutationHistory(String documentId);
    public List<Mutation> addMutation(String documentId, Mutation mutation);
    public void await() throws InterruptedException;
    public void broadcast();
}