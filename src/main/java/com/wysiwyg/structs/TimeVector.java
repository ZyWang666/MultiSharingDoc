package com.wysiwyg.structs;

import java.util.ArrayList;

public class TimeVector {
  public int VT[];
  public int peerCount;

  public TimeVector(int peerCount) {
    this.peerCount = peerCount;
    if (peerCount > 0) {
      VT = new int[peerCount];
    }

    for (int i=0; i<peerCount; i++) {
      VT[i] = 0;
    }
  }

  public TimeVector(TimeVector vt) {
    this.peerCount = vt.peerCount;
    VT = new int[peerCount];

    for (int i=0; i<peerCount; i++) {
      VT[i] = vt.VT[i];
    }
  }

  public void update(TimeVector vt) {
    if (this.peerCount == 0) {
      VT = new int[vt.peerCount];
      this.peerCount = vt.peerCount;
    }

    if (this.peerCount != vt.peerCount) {
      System.out.println("Incompatible VT size " + this.peerCount + ", " + vt.peerCount);
      return;
    }

    for (int i=0; i<vt.peerCount; i++) {
      VT[i] = vt.VT[i];
    }
  }

  public void merge(TimeVector u, TimeVector v) {
    int i;

    // assume peerCount is already max. of |u| and |v|
    int min = (u.peerCount <= v.peerCount)? u.peerCount : v.peerCount;

    for (i=0; i<min; i++) {
      VT[i] = (u.VT[i] < v.VT[i])? v.VT[i] : u.VT[i];
    }

    for (; i<u.peerCount; i++) {
      VT[i] = u.VT[i];
    }

    for (; i<v.peerCount; i++) {
      VT[i] = v.VT[i];
    }
  }
}

