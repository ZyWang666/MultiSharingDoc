package com.wysiwyg.CBCAST;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.*;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;

import com.wysiwyg.structs.*;


// one thread for one listening port
public class Receiver extends Thread {
  enum RecvConnState {
    R_INIT,
    R_LISTEN
  };

  private int _port;
  private ServerSocket _serverSocket;
  private CBCAST _parent;
  private RecvConnState _rState;

  
  public Receiver(int port, CBCAST dest) {
    _parent = dest;
    _port = port;
    _rState = RecvConnState.R_INIT;

    try {
      _serverSocket = new ServerSocket(port);
      _rState = RecvConnState.R_LISTEN;
    } catch (Exception e) {
      System.out.println("Failed to create listening port.");
      System.out.println(e);
    }
  }


  public void run() {
    while (_rState == RecvConnState.R_LISTEN) {
      try {
        Socket recvSocket = _serverSocket.accept();    // listening

        // create new thread for actual data receiver from this receiver socket.
        DataReceiver dataReadChan = new DataReceiver(recvSocket, _parent);
        dataReadChan.start();
      } catch (Exception e) {
        System.out.println("Error to accept connection.");
        System.out.println(e);
      }
    }
  }
}

