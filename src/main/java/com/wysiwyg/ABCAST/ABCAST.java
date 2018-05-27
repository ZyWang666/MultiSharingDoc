package ABCAST;

import java.util.LinkedList;
import java.util.Queue;

import com.wysiwyg.structs;
import com.wysiwyg.CBCAST;


public class Constants {
  public static final int UID_NODE = 100000;
}

public class ABCAST extends CBCAST {
  public boolean hasToken;                    // node with token to set total order?
  public int uid;                             // unique id
  
  private LinkedList<Integer> _uidOrder;      // needed for assembling order based on received message.
  private Queue<Mutation> _abcastWaitList;    // messages to be delivered based on abcast total order.


  public ABCAST(int peerIndex, int peerCount) {
    super(peerIndex, peerCount);
    uid = peerIndex * UID_NODE;
    hasToken = (uid == 0)? true : false;

    _uidOrder = null;
    _abcastWaitList = new LinkedList<Mutation>();
  }

 
  public synchronized 
  void bcast(Mutation msg) {
    uid++;

    // prepare ABCAST portion of info.
    msg.syncInfo.uid = uid;
    msg.syncInfo.setOrderInd = false;
//    msg.syncInfo.deliverable = hasToken;

    this.CBCAST.bcast(msg);

    // send setOrder
    if (hasToken) {
      // prepare setOrder message and then send via normal CBCAST.
      LinkedList<Integer> txUID = new LinkedList<Integer>();
      txUID.add(uid);
      uid++;
      Mutation orderMsg = new Mutation(uid, txUID);
      this.CBCAST.bcast(orderMsg);    // broadcast setOrder message
    }
  }


  public synchronized 
  void onReceive(Mutation msg) {
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
        this.CBCAST.onReceive(msg, null, false, null);
      } else {
        _uidOrder = new LinkedList<Integer>();   
        this.CBCAST.onReceive(msg, _uidOrder, true, null);

        // send setOrder message
        uid++;
        Mutation orderMsg = new Mutation(uid, _uidOrder);
        this.CBCAST.bcast(orderMsg);    // broadcast setOrder message
      }

      return;
    }

    // for non-token nodes
    if (this.CBCAST.onReceive(msg, null, false, _abcastWaitList) == false) {
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

                // TODO: deliver m to OPT
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
  }  // end function onReceive
}

