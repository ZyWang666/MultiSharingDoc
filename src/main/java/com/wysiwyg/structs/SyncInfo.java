package com.wysiwyg.structs;

import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.Queue;

public class SyncInfo {
    protected long version;
    protected Timestamp timeStamp;
    public int srcIndex;
    public TimeVector timeVector;
    public int uid;                     // unique ID of this message
    public LinkedList<Integer> uidOrder; // total order
    public boolean setOrderInd;         // indicates if this is set ordering message.
//    public boolean deliverable;

    SyncInfo() {
      timeVector = new TimeVector(0);
      uidOrder = new LinkedList<Integer>();
      setOrderInd = false;
    }

    // construct setOrder message 
    SyncInfo(int uid, LinkedList<Integer> order) {
      timeVector = new TimeVector(0);
      this.uid = uid;
      uidOrder = order;
      setOrderInd = true;
    }
}
