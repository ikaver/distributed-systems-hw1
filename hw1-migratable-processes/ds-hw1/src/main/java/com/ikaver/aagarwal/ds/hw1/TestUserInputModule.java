package com.ikaver.aagarwal.ds.hw1;

import java.io.InputStream;

import com.google.inject.AbstractModule;

public class TestUserInputModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(INodeManager.class).to(NodeManagerMock.class);
    bind(InputStream.class).toInstance(System.in);
  }
 
}
