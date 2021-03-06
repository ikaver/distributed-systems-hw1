package com.ikaver.aagarwal.ds.hw1.shared.helpers;

/**
 * Helper functions for array operations.
 */
public class ArrayAdditions {
  
  public static boolean contains(Object [] array, Object elem) {
    if(array == null) return false;
    for(int i = 0; i < array.length; ++i) {
      if(array[i].equals(elem)) return true;
    }
    return false;
  }
  
}
