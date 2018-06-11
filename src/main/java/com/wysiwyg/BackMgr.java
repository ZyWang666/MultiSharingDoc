package com.wysiwyg;

import java.util.LinkedList;
import java.util.Queue;
import java.util.List;
import java.util.ArrayList;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileReader;
import java.lang.Integer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.URL;
import java.net.MalformedURLException;

import com.wysiwyg.structs.*;
import com.wysiwyg.ABCAST.*;
import com.wysiwyg.ot.OperationalTransformation;


public class BackMgr {
  private static final String _CONFIG_FILE = "CONFIG";
  private static final String _MYIP_URL = "http://checkip.amazonaws.com/";

  public String[] serverIPs;
  public int[] serverPorts;

  private ABCAST _abcast;
  

  boolean isMyIP(String ip) {
    String myip;

    try {
      URL url = new URL(_MYIP_URL);
      BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
      myip = br.readLine();
    } catch (MalformedURLException e) {
      System.out.println(e);
      return false;
    } catch (IOException e) {
      System.out.println(e);
      return false;
    }

    System.out.println("My IP address is " + myip);
    return (myip.compareTo(ip) == 0);
  }


  public BackMgr(OperationalTransformation ot) {
    List<String> lines = new ArrayList<String>();
    int myIndex = -1;

    try {
      BufferedReader reader = new BufferedReader(new FileReader(_CONFIG_FILE));

      String line = null;
      while ((line = reader.readLine()) != null) {
        lines.add(line);
      }

      reader.close();
    } catch (IOException e) {
      System.out.println("Error reading config file! " + e);
      return;
    }
      
    assert lines.size()>0 : "No Servers Config!";

    serverIPs = new String[lines.size()];
    serverPorts = new int[lines.size()];

    for (int i=0; i<lines.size(); i++) {
      String[] ip_ports = lines.get(i).split(":");
      serverIPs[i] = ip_ports[0];
      serverPorts[i] = Integer.valueOf(ip_ports[1]);

      if (isMyIP(serverIPs[i]) == true) {
        myIndex = i;
      }
    }

    assert myIndex != -1 : "My IP not found!";    
    
    _abcast = new ABCAST(myIndex, serverIPs, serverPorts, msg -> ot.enqueueMutation(msg));
  }

  
  public void bcast(Mutation msg) {
    _abcast.bcast(msg);
  }
}
