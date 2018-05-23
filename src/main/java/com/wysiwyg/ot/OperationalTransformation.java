package com.wysiwyg.ot;

import java.lang.Thread;
import java.util.AbstractQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.wysiwyg.structs.Mutation;
import com.wysiwyg.operations.Operation;
import com.wysiwyg.operations.OperationImpl;
import com.wysiwyg.structs.Opcode;
// import org.inferred.freebuilder.FreeBuilder;

public class OperationalTransformation {
    protected AbstractQueue<Mutation> mutationQueue;
    protected Operation operationInstance;

    public OperationalTransformation(Operation operationInstance) {
        mutationQueue = new ConcurrentLinkedQueue<Mutation>();
        this.operationInstance = operationInstance;
        new Thread(new ConsumeMutationQueue()).start();
    }

    public boolean enqueueMutation(Mutation mutation) {
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
                    }
                }
            }
        }
    }

    protected Mutation tii(Mutation p, Mutation q) {
        // need user identifier to break tie
        if (p.pos < q.pos || (p.pos == q.pos && p.uid.compareTo(q.uid) > 0)) {
            return p;
        } else {
            return new Mutation(p.opcode,
                                p.documentId, 
                                p.pos+1,
                                p.payload,
                                p.uid, 
                                p.version);
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
                                p.version);
        }
    }

    protected Mutation tdi(Mutation p, Mutation q) {
        if (p.pos < q.pos) {
            return q;
        } else {
            return new Mutation(q.opcode,
                                q.documentId, 
                                q.pos+1,
                                q.payload,
                                q.uid,
                                p.version);
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
                                p.version);
        } else {
            return p.uid.compareTo(q.uid) > 0 ? p : q;
        }
    }

    protected Mutation transform(Mutation p, Mutation q) {
        if (p.opcode.equals(Opcode.INSERT) && q.opcode.equals(Opcode.INSERT)) {
            tii(p, q);
        } else if (p.opcode.equals(Opcode.INSERT) && q.opcode.equals(Opcode.DELETE)) {
            tid(p, q);
        } else if (p.opcode.equals(Opcode.DELETE) && q.opcode.equals(Opcode.INSERT)) {
            tdi(p, q);
        } else if (p.opcode.equals(Opcode.DELETE) && q.opcode.equals(Opcode.DELETE)) {
            tdd(p, q);
        }
        return p;
    }
}
