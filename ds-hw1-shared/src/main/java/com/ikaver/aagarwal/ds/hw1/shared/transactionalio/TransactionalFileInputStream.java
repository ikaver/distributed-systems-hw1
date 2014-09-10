package com.ikaver.aagarwal.ds.hw1.shared.transactionalio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;


public class TransactionalFileInputStream extends FileInputStream implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = -294089603420345539L;

  public TransactionalFileInputStream(File file) throws FileNotFoundException {
    super(file);
    // TODO Auto-generated constructor stub
  }

}
