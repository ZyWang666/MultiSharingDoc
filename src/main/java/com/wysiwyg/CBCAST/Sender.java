package com.wysiwyg.CBCAST;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.*;
import java.util.concurrent.TimeUnit;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.wysiwyg.structs.*;


// one thread for one peer
public class Sender extends Thread {
  public static final int SLEEP_TIME = 3000;
  public static final int CONN_TIMEOUT = 60000;   // connection timeout
  public static final int TRY_CONN_MAX = 5;
  public static final int QUEUE_WAIT_TIME = 1000; 

  enum SenderConnState { 
    S_INIT, 
    S_CONNECT,
    S_RETRY,
    S_LOST
  };

  private String _ip;
  private int _port;
  private Socket _socket;

  private SenderConnState _isConnected;
  private int _tryconn_cnt;

  private Queue<Mutation> _sList;   // list of messages to be send
  private Lock _qMutex;
  private Condition _qCond;

  // TODO: cRecover_cb()
  // TODO: cLost_cb()


  public Sender(String ip, int port) {
    _ip = ip;
    _port = port;
    _socket = new Socket();
    _isConnected = SenderConnState.S_INIT;
    _tryconn_cnt = TRY_CONN_MAX;

    _sList = new LinkedList<Mutation>();
    _qMutex = new ReentrantLock();
    _qCond = _qMutex.newCondition();
  }


  // Note that Sender never quits after creation
  public void run() {
   // sleep and periodic check of link
   // at the same time, should wakeup if any message to be sent while connected
    while (true) {
      // check and update connection state
      try {
        SocketAddress sockaddr = new InetSocketAddress(InetAddress.getByName(_ip), _port);
        _socket.connect(sockaddr, CONN_TIMEOUT);
        
        synchronized(this) {
          if (_isConnected == SenderConnState.S_LOST) {
            // TODO: recovery, node comes back!!
            // cRecover_cb();
          }

          _isConnected = SenderConnState.S_CONNECT;
          _tryconn_cnt = TRY_CONN_MAX;    // reset connection retry count
        }

        // Connection Successful, send messages
        _qMutex.lock();

          while (true) {
            // send all messages in the queue if any
            try {
              for (Mutation msg : _sList) {
                byte[] msgBytes = Utils.serialize(msg);
                DataOutputStream dos = new DataOutputStream(_socket.getOutputStream());
                dos.writeInt(msgBytes.length);
                dos.write(msgBytes);
              }
            } catch (IOException e) {
              // recheck connection after sleep
              System.out.println("Connection issue while sending message. Try to re-establish.");
              break;
            }

            if (_qCond.await(QUEUE_WAIT_TIME, TimeUnit.MILLISECONDS) == false) {
              // no messages to send, time to sleep.
              break;
            }
          }

        _qMutex.unlock();

      } catch (Exception e) {
        // socket connection timeout
        synchronized(this) {
          if (_isConnected != SenderConnState.S_LOST) {
            _isConnected = SenderConnState.S_RETRY;
            _tryconn_cnt--;

            if (_tryconn_cnt==0) {
              // TODO: This IP is lost.  Callback for fault tolerance....
              // 
              // cLost_cb();

              _isConnected = SenderConnState.S_LOST;
            }
          }   // if previous conn. state is not LOST
        }
      } 

      // Sleep here, wakeup if there is message coming in?
      try {
        Thread.sleep(CONN_TIMEOUT);
      } catch (InterruptedException e) {
        System.out.println("Wake up by new message send request.");
      }
    }
  }

  
  public boolean send(Mutation msg) {
    synchronized(this) {
      if (_isConnected == SenderConnState.S_LOST) {
        return false;
      }
    }

    // only send if in connected state to avoid buffer overflow
    _qMutex.lock();
      _sList.add(msg);
      _qCond.signal();    
    _qMutex.unlock();

    super.interrupt();   // wake up Sender thread if it is sleeping.

    return true;
  }
}

