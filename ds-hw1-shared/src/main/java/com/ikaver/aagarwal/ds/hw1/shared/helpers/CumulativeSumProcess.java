package com.ikaver.aagarwal.ds.hw1.shared.helpers;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.log4j.Logger;

import com.ikaver.aagarwal.ds.hw1.shared.IMigratableProcess;
import com.ikaver.aagarwal.ds.hw1.shared.transactionalio.TransactionalFileInputStream;
import com.ikaver.aagarwal.ds.hw1.shared.transactionalio.TransactionalFileOutputStream;

/**
 * Simple process that calculates the cumulative sum of N numbers and prints
 * the values to the console and to an output file (for testing/demonstration
 * purposes)
 */
public class CumulativeSumProcess implements IMigratableProcess {

	private static final long serialVersionUID = -1212;

	private TransactionalFileInputStream inFile;
	private TransactionalFileOutputStream outFile;
	private static final Logger LOGGER = Logger
			.getLogger(CumulativeSumProcess.class);

	private volatile boolean suspending;
	private int cumulativeSum = 0;
	private int pos = 0;

	public CumulativeSumProcess(String[] args) throws Exception {
		if (args.length != 2) {
			LOGGER.error("usage: CumulativeProcess <inFile> <outFile>");
			throw new Exception("Invalid number of arguments");
		}

		inFile = new TransactionalFileInputStream(args[0]);
		outFile = new TransactionalFileOutputStream(args[1]);
	}

	/**
	 * Calculates the acumulative sum of all the numbers in the given input file.
	 * It is assumed that all numbers contained in the file are integers.
	 */
	public void run() {
		PrintStream out = new PrintStream(outFile);
		DataInputStream in = new DataInputStream(inFile);

		try {
			while (!suspending) {
				String line = in.readLine();
				if (line != null) {
					int n = Integer.valueOf(line);
					cumulativeSum = cumulativeSum + n;
					out.print(String.format(
							"Cumulative Sum:%d till position:%d\n",
							cumulativeSum, pos));
					System.out.println(String.format(
							"Cumulative Sum:%d till position:%d\n",
							cumulativeSum, pos));
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {

					}
					pos++;
				}
			}
		} catch (EOFException e) {

			// eof reached!
		} catch (IOException e) {
			LOGGER.error("CumulativeProcessError: Error" + e);
		}

		suspending = false;
	}

	public void suspend() {
		suspending = true;
		while (suspending)
			;
	}

}
