package com.ikaver.aagarwal.ds.hw1.shared.transactionalio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Serializable;


public class TransactionalFileOutputStream extends FileOutputStream implements Serializable{

  /**
   * 
   */
  private static final long serialVersionUID = 6197467219240327863L;
  
  public TransactionalFileOutputStream(File file) throws FileNotFoundException {
    super(file);
    // TODO Auto-generated constructor stub
  }

}
