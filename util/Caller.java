/*
 * Copyright 2018 Vicinity Concepts
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vicinityconcepts.lib.util;

/**
 * Simple data structure to store information about calling code, including
 * class name, method name, and line number.
 *
 * @author Ryan Palmer
 */
public class Caller {
	private static final String STRING_FORMAT = "%s::%s (%s:%d)";
	private static final String CLASS_UNKNOWN = "Unknown Class";
	private static final String METHOD_UNKNOWN = "Unknown Method";
	private static final String FILE_UNKNOWN = "Unknown File";
	private static final int LINE_UNKNOWN = 0;

	private final String threadName;
	private final String className;
	private final String methodName;
	private final String fileName;
	private final int lineNumber;

	/**
	 * This class can only be constructed using the static factory methods.
	 *
	 * @param className  Name of the calling class.
	 * @param methodName Name of the calling method.
	 * @param fileName   Name of the calling file.
	 * @param lineNumber Line number of the call.
	 */
	private Caller(String className, String methodName, String fileName, int lineNumber) {
		threadName = Thread.currentThread().getName();
		this.className = className;
		this.methodName = methodName;
		this.fileName = fileName;
		this.lineNumber = lineNumber;
	}

	/**
	 * Infer the caller by searching the stack trace for the first element
	 * outside of the specified current class name.
	 *
	 * @param currentClass The name of the current class.
	 * @return a new {@link Caller} based on the inferred calling code.
	 * @throws CallerNotFoundException if the calling code could not be inferred.
	 */
	public static Caller infer(String currentClass) throws CallerNotFoundException {
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();

		// We start at two here because the first two elements will always be
		// getStackTrace() and infer() respectively
		for (int i = 2; i < stack.length; i++) {
			StackTraceElement e = stack[i];
			String elementClass = e.getClassName();
			if (!elementClass.equals(currentClass))
				return new Caller(elementClass, e.getMethodName(), e.getFileName(), e.getLineNumber());
		}

		throw new CallerNotFoundException();
	}

	/**
	 * Get the caller by reading a previous stack trace element. Useful as a
	 * fallback if automatically inferring the caller is unsuccessful.
	 *
	 * @param steps Number of times to step backwards through the stack trace.
	 * @return a new {@link Caller} based on the calling code.
	 * @throws CallerNotFoundException if the rewind exceeds the size of the stack trace.
	 */
	public static Caller rewind(int steps) throws CallerNotFoundException {
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();

		try {
			// We add two here because the first two elements will always be
			// getStackTrace() and rewind() respectively
			StackTraceElement e = stack[steps + 2];
			return new Caller(e.getClassName(), e.getMethodName(), e.getFileName(), e.getLineNumber());
		} catch (IndexOutOfBoundsException e) {
			throw new CallerNotFoundException();
		}
	}

	/**
	 * Get a caller without any useful information. Can be used when inferring and rewinding
	 * both fail, but you still need to pass along some kind of caller object.
	 *
	 * @return a new {@link Caller} with default values representing unknown calling code.
	 */
	public static Caller unknown() {
		return new Caller(CLASS_UNKNOWN, METHOD_UNKNOWN, FILE_UNKNOWN, LINE_UNKNOWN);
	}

	/**
	 * @return the name of the calling code's thread.
	 */
	public String getThreadName() {
		return threadName;
	}

	/**
	 * @return the name calling class.
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @return the name calling method.
	 */
	public String getMethodName() {
		return methodName;
	}

	/**
	 * @return the name calling file.
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @return the line number of the calling code.
	 */
	public int getLineNumber() {
		return lineNumber;
	}

	/**
	 * @return a human-readable representation of this caller.
	 */
	public String toString() {
		return String.format(STRING_FORMAT, className, methodName, fileName, lineNumber);
	}
}