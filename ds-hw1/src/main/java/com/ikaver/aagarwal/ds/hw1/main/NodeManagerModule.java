package com.ikaver.aagarwal.ds.hw1.main;

import java.io.InputStream;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.ikaver.aagarwal.ds.hw1.nodemanager.NodeManagerImpl;
import com.ikaver.aagarwal.ds.hw1.nodemanager.ProcessManagerPool;
import com.ikaver.aagarwal.ds.hw1.nodemanager.ProcessesState;
import com.ikaver.aagarwal.ds.hw1.shared.INodeManager;

public class NodeManagerModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(INodeManager.class).to(NodeManagerImpl.class);
    bind(InputStream.class).annotatedWith(Names.named("ControllerInput"))
      .toInstance(System.in);
    ReadWriteLock poolLock = new ReentrantReadWriteLock();
    ReadWriteLock stateLock = new ReentrantReadWriteLock();
    ProcessManagerPool pool = new ProcessManagerPool();
    pool.add("1", "localhost:2000");
    ProcessesState state = new ProcessesState();
    
    bind(ReadWriteLock.class).annotatedWith(Names.named("NMPoolLock"))
      .toInstance(poolLock);
    bind(ReadWriteLock.class).annotatedWith(Names.named("NMStateLock"))
      .toInstance(stateLock);
    bind(ProcessManagerPool.class).annotatedWith(Names.named("NMPool"))
      .toInstance(pool);
    bind(ProcessesState.class).annotatedWith(Names.named("NMState"))
      .toInstance(state);
  }
}