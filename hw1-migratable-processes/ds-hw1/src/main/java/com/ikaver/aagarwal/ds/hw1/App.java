package com.ikaver.aagarwal.ds.hw1;

import com.google.inject.Guice;
import com.google.inject.Injector;

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
