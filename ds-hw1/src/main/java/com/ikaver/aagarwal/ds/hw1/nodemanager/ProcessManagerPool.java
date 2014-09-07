package com.ikaver.aagarwal.ds.hw1.nodemanager;

import java.util.HashMap;
import java.util.Set;

public class ProcessManagerPool {

  private HashMap<String, String> availableConnections;
  
  public ProcessManagerPool() {
    this.availableConnections = new HashMap<String, String>();
  }
  
  public int size() {
    return availableConnections.size();
  }
  
  public void add(String id, String connection) {
    this.availableConnections.put(id, connection);
  }
  
  public String connectionForId(String id) {
    return this.availableConnections.get(id);
  }
  
  public Set<String> availableNodes() {
    return this.availableConnections.keySet();
  }
  
  public void remove(String id) {
    this.availableConnections.remove(id);
  }
}
