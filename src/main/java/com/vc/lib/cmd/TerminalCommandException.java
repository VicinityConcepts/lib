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

package com.vc.lib.cmd;

/**
 * Used for errors that occur when configuring and executing {@link Terminal} commands.
 *
 * @author Ryan Palmer
 */
public class TerminalCommandException extends Exception {

	/**
	 * Construct a new {@link TerminalCommandException} with the specified message.
	 *
	 * @param message The message to display with the exception.
	 */
	public TerminalCommandException(String message) {
		super(message);
	}
}
