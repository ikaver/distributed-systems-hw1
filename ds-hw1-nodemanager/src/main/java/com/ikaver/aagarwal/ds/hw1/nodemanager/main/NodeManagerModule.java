package com.ikaver.aagarwal.ds.hw1.nodemanager.main;

import java.io.InputStream;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.ikaver.aagarwal.ds.hw1.nodemanager.NodeManagerImpl;
import com.ikaver.aagarwal.ds.hw1.nodemanager.SubscribedNodesState;
import com.ikaver.aagarwal.ds.hw1.shared.INodeManager;

public class NodeManagerModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(INodeManager.class).to(NodeManagerImpl.class);
    bind(InputStream.class).annotatedWith(Names.named("ControllerInput"))
      .toInstance(System.in);
    ReadWriteLock stateLock = new ReentrantReadWriteLock();
    SubscribedNodesState state = new SubscribedNodesState();
    state.addNode("1", "localhost:2000");
    state.addNode("2", "localhost:3000");
    
    bind(ReadWriteLock.class).annotatedWith(Names.named("NMStateLock"))
      .toInstance(stateLock);
    bind(SubscribedNodesState.class).annotatedWith(Names.named("NMState"))
      .toInstance(state);
  }
}