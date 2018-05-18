package com.wysiwyg.ot;

import java.lang.Thread;
import java.util.AbstractQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.wysiwyg.structs.Mutation;
import com.wysiwyg.operations.Operation;
import com.wysiwyg.operations.OperationImpl;
import com.wysiwyg.structs.Opcode;

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

}