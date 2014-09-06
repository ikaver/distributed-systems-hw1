package com.ikaver.aagarwal.ds.hw1.main;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.ikaver.aagarwal.ds.hw1.nodemanager.NodeManagerController;

public class App 
{
  
  public static void main( String[] args )
  {
    Injector injector = Guice.createInjector(new TestUserInputModule());        
    NodeManagerController controller 
      = injector.getInstance(NodeManagerController.class);
    controller.readInput();
  }

}
