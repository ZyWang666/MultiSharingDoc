package com.wysiwyg.CBCAST;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.*;
import java.util.ArrayList;

import com.wysiwyg.structs.*;


public class CBCAST {
  public int id;
  public TimeVector timeVector;

  private Queue<Mutation> _waitList;
  private Consumer<Mutation> _sink_fn;

  private ArrayList<Sender> _senders;
  private Receiver _receiver;



  public CBCAST(int peerIndex, String[] allServers, int[] ports, Consumer<Mutation> cb) {
    id = peerIndex;
    int peerCount = allServers.length;       // really is all servers count
    timeVector = new TimeVector(peerCount);
    _waitList = new LinkedList<Mutation>();
    _sink_fn = cb;

    _receiver = new Receiver(ports[id], this);
    _receiver.start();

    _senders = new ArrayList<Sender>(peerCount);

    for (int i=0; i<peerCount; i++) {
      if (i != id) {
        _senders.add(i, new Sender(allServers[i], ports[i]));
        _senders.get(i).start();
      }
    }
  }


  // to be overridden by ABCAST 
  public synchronized void bcast(Mutation msg) {
    timeVector.VT[id] = timeVector.VT[id] + 1;       // TBD: take care of roll-over?
    msg.syncInfo.srcIndex = id;
    msg.syncInfo.timeVector.update(timeVector);

    // Broadcasting to all addresses, need to distinguish between message type (ABCAST already does this)
    for (int i=0; i<_senders.size(); i++) {
      if (i != id) {
        _senders.get(i).send(msg);
      }
    }
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
        // deliver messasge to local app., to OT.  Should apply effect first, otherwise, reverse order.
        _sink_fn.accept(msg);
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


  // to be overriden by ABCAST
  public synchronized 
  void onReceive(Mutation msg) {
    onReceive(msg, null, false, null);
  }
}

