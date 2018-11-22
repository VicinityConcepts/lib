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

import java.io.*;

/**
 * Simple wrapper class for BufferedReader for getting lines of input.
 *
 * <p>
 * This class was designed to provide an easy way to wait for and return
 * the next line of user input, and to quickly check if the stream has
 * data in it without blocking execution of the calling thread.
 * </p>
 *
 * <p>
 * Exceptions are mostly ignored in this class. If input/output exceptions
 * occur, the reader basically behaves as if it is reading an empty
 * stream and continues operating.
 * </p>
 *
 * @author Ryan Palmer
 */
public class InputReader extends BufferedReader {
	/**
	 * Construct an input reader to read from the specified stream.
	 *
	 * @param input The stream to read from
	 */
	public InputReader(InputStream input) {
		super(new InputStreamReader(input));
	}

	/**
	 * Check if the stream contains any data.
	 *
	 * <p>
	 * This check will NOT block execution of the current thread if there is no data in
	 * the stream. It will return true or false immediately.
	 * </p>
	 *
	 * @return true if the stream contains any data to be read.
	 */
	public boolean hasNext() {
		try {
			return ready();
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * Read and return a line of input from the stream.
	 *
	 * <p>
	 * Execution of the current thread will be blocked until a full line is read.
	 * </p>
	 *
	 * @return the line of input that was read.
	 */
	public String nextLine() {
		try {
			return readLine();
		} catch (IOException e) {
			return null;
		}
	}
}
