package com.ikaver.aagarwal.ds.hw1.nodemanager.main;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.ikaver.aagarwal.ds.hw1.nodemanager.NodeManagerController;

public class App 
{
  
  public static void main( String[] args )
  {
//    Injector injector = Guice.createInjector(new TestUserInputModule());        
    Injector injector = Guice.createInjector(new NodeManagerModule());
    NodeManagerController controller 
      = injector.getInstance(NodeManagerController.class);
    controller.readInput();
  }

}
