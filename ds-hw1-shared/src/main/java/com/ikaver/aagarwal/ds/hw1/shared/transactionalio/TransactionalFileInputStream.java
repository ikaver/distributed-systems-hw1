package com.ikaver.aagarwal.ds.hw1.shared.transactionalio;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

/**
 * Class that allows migratable processes to read files. It will maintain all 
 * the information required in order to continue performing operations on the file, 
 * even if the process is transferred to another node.
 */
public class TransactionalFileInputStream extends InputStream implements
    Serializable {

  private static final long serialVersionUID = -294089603420345539L;

  private File file;
  private int offset;

  public TransactionalFileInputStream(String file) {
    this.file = new File(file);
    this.offset = 0;
  }

  @Override
  public int read() throws IOException {
    FileInputStream stream = new FileInputStream(this.file);
    stream.skip(this.offset);
    int output = stream.read();
    ++this.offset;
    stream.close();
    return output;
  }

}
