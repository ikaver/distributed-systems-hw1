package com.ikaver.aagarwal.ds.hw1.nodemanager.main;

import java.io.InputStream;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.ikaver.aagarwal.ds.hw1.nodemanager.NodeManagerMockImpl;
import com.ikaver.aagarwal.ds.hw1.shared.INodeManager;

public class TestUserInputModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(INodeManager.class).to(NodeManagerMockImpl.class);
    bind(InputStream.class).annotatedWith(Names.named("ControllerInput"))
      .toInstance(System.in);
  }
 
}
