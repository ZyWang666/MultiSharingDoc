package com.wysiwyg.ot;

import java.lang.Thread;
import java.util.AbstractQueue;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.wysiwyg.meta.MetadataManager;
import com.wysiwyg.meta.MetadataManagerImpl;
import com.wysiwyg.structs.Mutation;
import com.wysiwyg.operations.Operation;
import com.wysiwyg.operations.OperationImpl;
import com.wysiwyg.structs.Opcode;

public class OperationalTransformation {
    protected MetadataManager metadataManager;
    protected AbstractQueue<Mutation> mutationQueue;
    protected Operation operationInstance;

    public OperationalTransformation(Operation operationInstance) {
        metadataManager = new MetadataManagerImpl();
        mutationQueue = new ConcurrentLinkedQueue<Mutation>();
        this.operationInstance = operationInstance;
        new Thread(new ConsumeMutationQueue()).start();
    }

    public synchronized boolean enqueueMutation(Mutation mutation) {
        mutation.indexInMutationHistory = metadataManager.getMutationHistory(mutation.documentId).size();
        mutation = transform(mutation);
        metadataManager.addMutation(mutation.documentId, mutation);
        mutationQueue.add(mutation);
        return true;
    }

    protected class ConsumeMutationQueue implements Runnable {
        public void run() {
            while (true) {
                if (!mutationQueue.isEmpty()) {
                    Mutation mutation = mutationQueue.poll();
                    if (mutation.opcode.equals(Opcode.INSERT)) {
                        operationInstance.insert(mutation);
                    } else if (mutation.opcode.equals(Opcode.DELETE)) {
                        operationInstance.delete(mutation);
                    } else {
                        // do nothing
                    }
                }
            }
        }
    }

    protected Mutation tii(Mutation p, Mutation q) {
        // need user identifier to break tie
        if (p.pos < q.pos || (p.pos == q.pos && p.uid.compareTo(q.uid) > 0)) {
        // if (p.pos < q.pos) {
            return p;
        } else {
            return new Mutation(p.opcode,
                                p.documentId, 
                                p.pos+1,
                                p.payload,
                                p.uid, 
                                p.version,
                                p.indexInMutationHistory);
        }
    }

    protected Mutation tid(Mutation p, Mutation q) {
        if (p.pos <= q.pos) {
            return p;
        } else {
            return new Mutation(p.opcode,
                                p.documentId, 
                                p.pos-1,
                                p.payload,
                                p.uid,
                                p.version,
                                p.indexInMutationHistory);
        }
    }

    protected Mutation tdi(Mutation p, Mutation q) {
        if (p.pos < q.pos) {
            return p;
        } else {
            return new Mutation(p.opcode,
                                p.documentId, 
                                p.pos+1,
                                p.payload,
                                p.uid,
                                p.version,
                                p.indexInMutationHistory);
        }
    }

    protected Mutation tdd(Mutation p, Mutation q) {
        if (p.pos < q.pos) {
            return p;
        } else if (p.pos > q.pos){
            return new Mutation(p.opcode,
                                p.documentId, 
                                p.pos-1,
                                p.payload,
                                p.uid,
                                p.version,
                                p.indexInMutationHistory);
        } else {
            return Mutation.IDENTITY;
        }
    }

    protected Mutation t(Mutation p, Mutation q) {
        if (p.opcode.equals(Opcode.INSERT) && q.opcode.equals(Opcode.INSERT)) {
            return tii(p, q);
        } else if (p.opcode.equals(Opcode.INSERT) && q.opcode.equals(Opcode.DELETE)) {
            return tid(p, q);
        } else if (p.opcode.equals(Opcode.DELETE) && q.opcode.equals(Opcode.INSERT)) {
            return tdi(p, q);
        } else if (p.opcode.equals(Opcode.DELETE) && q.opcode.equals(Opcode.DELETE)) {
            return tdd(p, q);
        }
        return p;
    }

    protected Mutation transform(Mutation mutation) {
        List<Mutation> mutationHistory = metadataManager.getMutationHistory(mutation.documentId);
        for (int i = mutation.version; i < mutation.indexInMutationHistory; i++) {
            mutation = t(mutation, mutationHistory.get(i));
        }
        return mutation;
    }
}
