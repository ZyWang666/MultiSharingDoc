package com.wysiwyg.meta;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.wysiwyg.structs.Mutation;

public class MutationMapSingleton {
    protected static Map<String, List<Mutation>> mutationMap;

    private MutationMapSingleton() {}

    public static Map<String, List<Mutation>> getMutationMapInstance() {
        if (mutationMap == null) {
            synchronized (MutationMapSingleton.class) {
                if (mutationMap == null) {
                    mutationMap = new ConcurrentHashMap<String, List<Mutation>>();
                }
            }
        }
        return mutationMap;
    }

    public static synchronized List<Mutation> addMutation(String documentId, Mutation mutation) {
        // if (mutationMap == null) {
        //     mutationMap = new ConcurrentHashMap<String, List<Mutation>>();
        // }
        getMutationMapInstance();
        if (!mutationMap.containsKey(documentId)) {
            mutationMap.put(documentId, new ArrayList<Mutation>());
        }
        List<Mutation> mutationHistory = mutationMap.get(documentId);
        mutationHistory.add(mutation);
        // System.out.println(mutationHistory.size());
        if (mutationHistory.size() == 10 * 20) {
            System.out.println(System.nanoTime());
        }
        return mutationMap.put(documentId, mutationHistory);
    }

    public static synchronized List<Mutation> getMutationHistory(String documentId) {
        getMutationMapInstance();
        if (!mutationMap.containsKey(documentId)) {
            return new ArrayList<Mutation>();
        }

        return mutationMap.get(documentId);
    }
}
