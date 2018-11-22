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

package com.vicinityconcepts.lib.cmd;

/**
 * An object that describes a terminal command including the key
 * used to invoke it, the action it will perform, and other
 * configurations.
 *
 * @author Ryan Palmer
 * @c
 */
public class TerminalCommand {
	/**
	 * String constants
	 */
	private static final String STRING_FORMAT = "%s: %s";
	private static final String NO_DESCRIPTION = "No description.";

	/**
	 * The key by which this command will be invoked.
	 */
	private final String key;

	/**
	 * The action that will be performed when this command is invoked.
	 */
	private final Runnable action;

	/**
	 * A brief description of this command.
	 */
	private final String description;

	/**
	 * Construct a new terminal command with no description.
	 *
	 * @param key    The key by which this command will be invoked
	 * @param action The action that this command will perform
	 */
	public TerminalCommand(String key, Runnable action) {
		this(key, action, null);
	}

	/**
	 * Construct a new terminal command.
	 *
	 * @param key         The key by which this command will be invoked
	 * @param action      The action that this command will perform
	 * @param description A brief description of this command
	 */
	public TerminalCommand(String key, Runnable action, String description) {
		this.key = key;
		this.action = action;
		this.description = description;
	}

	/**
	 * @return this command's key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @return this command's action
	 */
	public Runnable getAction() {
		return action;
	}

	/**
	 * @return this command's description
	 */
	public String getDescription() {
		return (description != null) ? description : NO_DESCRIPTION;
	}

	/**
	 * Execute this command's action. If the specified action is null,
	 * nothing will happen.
	 */
	public void execute() {
		if (action != null) action.run();
	}

	/**
	 * Get a human-readable description of this command suitable for help
	 * pages or simple documentation.
	 */
	@Override
	public String toString() {
		return String.format(STRING_FORMAT, key, getDescription());
	}
}
