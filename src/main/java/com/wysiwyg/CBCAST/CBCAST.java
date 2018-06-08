package com.wysiwyg.CBCAST;

import java.util.LinkedList;
import java.util.Queue;

import com.wysiwyg.structs.*;


public class CBCAST {
    public int id;
    public TimeVector timeVector;

    private Queue<Mutation> _waitList;

    // TODO
    // senders
    // receiver

    public CBCAST(int peerIndex, int peerCount) {
        id = peerIndex;
        timeVector = new TimeVector(peerCount);
        _waitList = new LinkedList<Mutation>();

        // TODO
        // prepare receiver listening on port 
        // prepare senders initialization and ready channels to broadcast message
    }

    public synchronized void bcast(Mutation msg) {
        timeVector.VT[id] = timeVector.VT[id] + 1;       // TODO: take care of roll-over?
        msg.syncInfo.srcIndex = id;
        msg.syncInfo.timeVector.update(timeVector);

        // TODO: Broadcasting to all addresses, need to distinguish between message type.
    }

    protected synchronized boolean delayMessage(Mutation msg) {
        int si = msg.syncInfo.srcIndex;

        // should this be > ?, older version: != VT[si]+1
        // older version probably more correct because only to reflect changes once.
        if (msg.syncInfo.timeVector.VT[si] != timeVector.VT[si] + 1) {
            return true;
        }

        for (int i=0; i<timeVector.peerCount; i++) {
            if (i != si) {
                if (msg.syncInfo.timeVector.VT[i] > timeVector.VT[i]) {
                    return true;
                }
            }
        }

        return false;
    }

    public synchronized boolean onReceive(Mutation msg, LinkedList<Integer> order, boolean recOrder, Queue<Mutation> output) {
        boolean delayed;

        if (delayMessage(msg)) {
            _waitList.add(msg);
            delayed = true;
        } else {
            if (output == null) {
            // TODO: deliver messasge to local app., to OT.  Should apply effect first, otherwise, reverse 
            // order.
            } else {
                output.add(msg);
            }

            if (recOrder == true) {
                order.add(msg.syncInfo.uid);              // set order
            }
            timeVector.merge(timeVector, msg.syncInfo.timeVector);

            for (Mutation msgWait : _waitList) {
                if (!delayMessage(msgWait)) {
                    _waitList.remove(msgWait);  // this needs to be before, otherwise, forever loop?
                                              // actually won't, because has to have one entry equal to VT[si]+1.
                    onReceive(msgWait, order, recOrder, output);  // recursion to find eligible delivery with updated time vector.
                }
            }

            delayed = false;
        }

        return delayed;
    }
}

