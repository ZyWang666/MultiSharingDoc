package com.wysiwyg.meta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.wysiwyg.structs.Document;

public class DocumentMapSingleton {
    protected static Map<String, Document> documentMap;

    private DocumentMapSingleton() {}

    public static Map<String, Document> getDocumentMapInstance() {
        if (documentMap == null) {
            synchronized (DocumentMapSingleton.class) {
                if (documentMap == null) {
                    documentMap = new ConcurrentHashMap<String, Document>();
                }
            }
        }
        return documentMap;
    }

    public static synchronized Document putDocumentMap(String s, Document d) {
        if (documentMap == null) {
            documentMap = new ConcurrentHashMap<String, Document>();
        }
        return documentMap.put(s, d);
    }
}