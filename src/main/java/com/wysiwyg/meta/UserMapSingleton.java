package com.wysiwyg.meta;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.wysiwyg.structs.Document;

public class UserMapSingleton {
    protected static Map<String, Set<Document>> userMap;

    private UserMapSingleton() {}

    public static Map<String, Set<Document>> getUserMapInstance() {
        if (userMap == null) {
            synchronized (UserMapSingleton.class) {
                if (userMap == null) {
                    userMap = new ConcurrentHashMap<String, Set<Document>>();
                }
            }
        }
        return userMap;
    }

    public static synchronized boolean addUser(String user) {
        if (!userMap.containsKey(user)) {
            return (userMap.put(user, new HashSet<Document>()) != null);
        }
        return false;
    }

    public static synchronized boolean addDocument(String user, Document document) {
        return userMap.get(user).add(document);
    }
}