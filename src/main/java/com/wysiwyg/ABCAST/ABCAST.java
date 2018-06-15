package com.wysiwyg.ABCAST;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.*;

import com.wysiwyg.structs.*;
import com.wysiwyg.CBCAST.CBCAST;


public class ABCAST extends CBCAST {
  public static final int UID_NODE = 100000;
  public boolean hasToken;                    // node with token to set total order?
  public int uid;                             // unique id
  
  private LinkedList<Integer> _uidOrder;      // needed for assembling order based on received message.
  private Queue<Mutation> _abcastWaitList;    // messages to be delivered based on abcast total order.
  private Consumer<Mutation> _sink_fn;


  public ABCAST(int peerIndex, String[] allServers, int[] ports, Consumer<Mutation> cb) {
    super(peerIndex, allServers, ports, cb);
    uid = peerIndex * UID_NODE;
    hasToken = (uid == 0)? true : false;

    _uidOrder = null;
    _abcastWaitList = new LinkedList<Mutation>();
    _sink_fn = cb;
  }

 
  @Override
  public synchronized 
  void bcast(Mutation msg) {
    uid++;
    System.out.println("ABCAST::bcast uid = " + uid);

    // prepare ABCAST portion of info.
    msg.syncInfo.uid = uid;
    msg.syncInfo.setOrderInd = false;
//    msg.syncInfo.deliverable = hasToken;

    super.bcast(msg);

    // send setOrder
    // setOrder message only sent when the msg is loopbacked.
/*
    if (hasToken) {
      // prepare setOrder message and then send via normal CBCAST.
      LinkedList<Integer> txUID = new LinkedList<Integer>();
      txUID.add(uid);
      uid++;
      Mutation orderMsg = new Mutation(uid, txUID);
      super.bcast(orderMsg);    // broadcast setOrder message
    }
*/
  }


  @Override
  public synchronized 
  void onReceive(Mutation msg) {
    // Logging
    if (msg.syncInfo.setOrderInd == false) {
      System.out.println("ABCAST::onReceive uid = "+msg.syncInfo.uid+" size = "+msg.payload.length()+" opcode: "+msg.opcode);
    } else {
      System.out.println("ABCAST::onReceive uid = "+msg.syncInfo.uid+" setOrderInd: "+msg.syncInfo.setOrderInd);
    }


    boolean ret;

    if (hasToken) {
/*
      1. initially, setOrder message broadcast does not involve local queue, just completely handled in the
         message itself.
      2. need to have permanent queuing up of uid on the server? no, only needed for each time new 
         message is received and then order list to be regenerated and sent and then cleared up again
         when the next message is received.   Should send the regenerated order only once..... reference
         sends too many times?  problem is then how to detect beginning and end, can't do recursion on
         this onReceive.....  idea: pass in order queue to CBCAST?
*/
      // message must be received from non-token peers.
      if (delayMessage(msg)) {
        ret = super.onReceive(msg, null, false, null);
        System.out.println("token delay: "+ret);
      } else {
        _uidOrder = new LinkedList<Integer>();   
        // CBCAST will deliver msg for the node with token, otherwise, ABCAST handled for non-token node.
        // token node does not truly need to use setOrder message, it just needs to create and send
        // to pears.  But the setOrder message needs to sink through CBCAST to update timeVectors and
        // it can then be discarded there after timeVectors update and ordering.
        ret = super.onReceive(msg, _uidOrder, true, null);
        System.out.println("token delay: "+ret);

        // setOrder message shall still be received and broadcast via CBCAST here, to make sure 
        // timeVectors are all good.  But this should be the only place to send setOrder message and
        // it should only be sent if the received messages which finally have time-expired and ready
        // to be consumed.  setOrder message should not only depend on current directly received msg
        // but on if there are messages available to be consumed which setOrder messages themselves
        // should not be counted as consumable.
        if (_uidOrder.size() > 0) {
          uid++;
          Mutation orderMsg = new Mutation(uid, _uidOrder);
          super.bcast(orderMsg);    // broadcast setOrder message
        }
      }

      return;
    }

    // for non-token nodes
    if (super.onReceive(msg, null, false, _abcastWaitList) == false) {
      System.out.println("non-token delay: false");

      for (Mutation order_msg : _abcastWaitList) {
        // find all the setOrder message delivered
        if (order_msg.syncInfo.setOrderInd == true) {
          for (Integer orderid : order_msg.syncInfo.uidOrder) {
            // would there be cases that need to deliver non-token message which is not in order list? 
            // cannot be, if there is such message, it means it should be tracked by later setorder msg.
            // deliverable flag probably not used since causal waiting list already takes care of buffering
            // undeliverable including setOrder message.
            // it should only need to take care of concurrent ABCAST reordering here.
            boolean isfound = false;

            for (Mutation m : _abcastWaitList) {
              // use setOrder message position as search stopper.
              if ((m.syncInfo.setOrderInd == true) && (m.syncInfo.uid == order_msg.syncInfo.uid)) {
                break;
              }

              if ((m.syncInfo.setOrderInd == false) && (m.syncInfo.uid == orderid.intValue())) {
                isfound = true;

                // deliver m to OPT
                _sink_fn.accept(m);
                _abcastWaitList.remove(m);
                break;
              }
            }

            if (isfound == false) {
              System.out.println("Assertion error! uid not found in delivered setOrder.");
            }
            assert(isfound);
          }

          _abcastWaitList.remove(order_msg);
        }
      }   // end for
    }   // end if onReceive for non-token
    else {
      System.out.println("non-token delay: true");
    }
  }  // end function onReceive
}

