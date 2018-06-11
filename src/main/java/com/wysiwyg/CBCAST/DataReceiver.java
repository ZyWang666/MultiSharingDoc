package com.wysiwyg.CBCAST;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.*;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

import com.wysiwyg.structs.*;


// one thread for each incoming connection
public class DataReceiver extends Thread {
  private Socket _recvSocket;
  private CBCAST _dest;

  public DataReceiver(Socket s, CBCAST dest) {
    _recvSocket = s;
    _dest = dest;
  }

  public void run() {
    // incoming connection established, read data from channel
    try {
      DataInputStream inPipe = new DataInputStream(_recvSocket.getInputStream());

      while (true) {
        int dataSize = inPipe.readInt();

        byte[] dataRead = new byte[dataSize];
        inPipe.readFully(dataRead);

        Mutation msg = (Mutation)(Utils.deserialize(dataRead));
        _dest.onReceive(msg);    // should be calling override function in ABCAST.
      }
    } catch (Exception e) {
      System.out.println("Data read pipe exception: ");
      System.out.println(e);
    }
  }
}

