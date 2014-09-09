package com.ikaver.aagarwal.ds.hw1.shared.helpers;

public class MathHelper {
  
  public static int randomIntInRange(int min, int max) {
    return (int) (min + (max-min)*Math.random());
  }

}
