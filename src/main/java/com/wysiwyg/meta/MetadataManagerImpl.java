package com.wysiwyg.meta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.wysiwyg.structs.Document;
import com.wysiwyg.structs.Mutation;

public class MetadataManagerImpl implements MetadataManager {

    public MetadataManagerImpl() {
    }

    @Override
    public List<String> listUser() {
        return new ArrayList<String>(UserMapSingleton.getUserMapInstance().keySet());
    }

    @Override
    public boolean addUser(String user) {
        return UserMapSingleton.addUser(user);
    }

    @Override
    public List<Document> listDocument() {
        return new ArrayList<Document>(DocumentMapSingleton.getDocumentMapInstance().values());
    }

    @Override
    public Document getDocument(String name) {
        return DocumentMapSingleton.getDocumentMapInstance().get(name);
    }

    @Override
    public void addDocument(Document document) {
        DocumentMapSingleton.putDocumentMap(document.documentId, document);
    }

    @Override
    public List<Mutation> addMutation(String documentId, Mutation mutation) {
        return MutationMapSingleton.addMutation(documentId, mutation);
    }

    @Override
    public List<Mutation> getMutationHistory(String documentId) {
        return MutationMapSingleton.getMutationHistory(documentId);
    }
}