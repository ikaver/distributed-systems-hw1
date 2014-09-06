package com.ikaver.aagarwal.ds.hw1.helpers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import com.ikaver.aagarwal.ds.hw1.shared.IMigratableProcess;

/**
 * Test class for understanding how reflection works.
 * 
 * @author ankit
 * 
 *         TODO(ankit): Make sure that this is not compiled with the final
 *         project.
 */
public class ReflectionTest {

	public static void main(String args[]) throws ClassNotFoundException,
			NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {

		// TODO(ankit): Handle this exception more gracefully.
		Class<IMigratableProcess> process = ((Class<IMigratableProcess>) Class
				.forName(args[0]));

		Constructor<IMigratableProcess> constructor = process.getConstructor(
				int.class, String[].class);
		// Creates an instance of the calls and calls the corresponding methods.
		IMigratableProcess newMigratableProcess = constructor.newInstance(1,
				Arrays.copyOfRange(args, 1, args.length));

		newMigratableProcess.run();
		newMigratableProcess.suspend();
	}
}
