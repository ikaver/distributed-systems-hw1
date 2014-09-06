package com.ikaver.aagarwal.ds.hw1.main;

import java.io.InputStream;

import com.google.inject.AbstractModule;
import com.ikaver.aagarwal.ds.hw1.nodemanager.NodeManagerMock;
import com.ikaver.aagarwal.ds.hw1.shared.INodeManager;

public class TestUserInputModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(INodeManager.class).to(NodeManagerMock.class);
    bind(InputStream.class).toInstance(System.in);
  }
 
}
