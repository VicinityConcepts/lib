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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Thread-safe utility for managing and executing terminal commands.
 *
 * <p>
 * Thread safety is enforced by synchronizing all methods to the instance,
 * meaning other threads must suspend execution until the active operation
 * is complete. It's important to be aware that command execution is no
 * exception to this. That means a lock will be held on the instance until
 * the command has completely finished executing, which could potentially
 * be a significant duration.
 * </p>
 *
 * <p>
 * If you wish to avoid holding lengthy locks on the registry instance during
 * command execution, you can obtain the command using the get method first,
 * and then execute it directly and separately. This will only lock the registry
 * instance briefly while you obtain the command reference, and you can then
 * execute it using your own separate synchronization strategy.
 * </p>
 *
 * @author Ryan Palmer
 */
public class TerminalCommandRegistry {
	private static final String ERROR_NO_SUCH_COMMAND = "Command '%s' does not exist.";
	private static final String ERROR_RESERVED_COMMAND = "Command '%s' is reserved and cannot be set.";

	/**
	 * Mapping of keys and commands.
	 *
	 * <p>
	 * Each key in the map should always be equal to the key of the corresponding command object.
	 * </p>
	 */
	private final Map<String, TerminalCommand> commands;

	/**
	 * Set of command keys that cannot be put into or removed from the registry.
	 */
	private final Set<String> reservedCommands;

	/**
	 * Construct a new empty terminal command registry to manage and execute terminal commands.
	 */
	public TerminalCommandRegistry() {
		commands = new HashMap<>();
		reservedCommands = new HashSet<>();
	}

	/**
	 * Construct a new terminal command registry with preset commands.
	 *
	 * <p>
	 * All preset commands will be automatically reserved, which means they
	 * cannot be replaced or removed.
	 * </p>
	 *
	 * @param presets List of commands to add and reserve
	 */
	public TerminalCommandRegistry(TerminalCommand... presets) {
		this();
		for (TerminalCommand command : presets) {
			try {
				put(command);
				reserve(command.getKey());
			} catch (TerminalCommandException ignore) {
				// This would only happen if the same command was passed twice
			}
		}
	}

	/**
	 * Reserve a command.
	 *
	 * <p>
	 * Reserved commands cannot be put into or removed from the registry. If the reserved command
	 * already exists in the registry, this will protect it from being overwritten.
	 * </p>
	 *
	 * @param key Command to reserve
	 */
	public synchronized void reserve(final String key) {
		reservedCommands.add(key);
	}

	/**
	 * Allow a previously reserved command, enabling it to be added to or removed from the registry.
	 *
	 * @param key Command to allow
	 */
	public synchronized void allow(final String key) {
		reservedCommands.remove(key);
	}

	/**
	 * Put a command in the registry.
	 *
	 * @param command Command to add
	 * @throws TerminalCommandException if command is reserved
	 */
	public synchronized void put(final TerminalCommand command) throws TerminalCommandException {
		String key = command.getKey();
		if (reservedCommands.contains(key))
			throw new TerminalCommandException(String.format(ERROR_RESERVED_COMMAND, key));
		commands.put(key, command);
	}

	/**
	 * Check if a command with the same key exists in the registry.
	 *
	 * @param command The command to check for.
	 * @return true if the command exists in the registry.
	 */
	public synchronized boolean contains(final TerminalCommand command) {
		return commands.containsKey(command.getKey());
	}

	/**
	 * Check if a command with this key exists in the registry.
	 *
	 * @param key The key to check for.
	 * @return true if the command exists in the registry.
	 */
	public synchronized boolean contains(final String key) {
		return commands.containsKey(key);
	}

	/**
	 * Remove a command from the registry.
	 *
	 * @param key The command to remove.
	 * @return the command object that was removed.
	 * @throws TerminalCommandException if command is reserved. Reserved commands cannot be overwritten
	 *                                  or removed from the registry.
	 */
	public synchronized TerminalCommand remove(final String key) throws TerminalCommandException {
		if (reservedCommands.contains(key))
			throw new TerminalCommandException(String.format(ERROR_RESERVED_COMMAND, key));
		return commands.remove(key);
	}

	/**
	 * Get a specific command from the registry.
	 *
	 * @param key The command to get.
	 * @return the command object matching the specified key.
	 * @throws TerminalCommandException if command does not exist.
	 */
	public synchronized TerminalCommand get(final String key) throws TerminalCommandException {
		if (!contains(key)) throw new TerminalCommandException(String.format(ERROR_NO_SUCH_COMMAND, key));
		return commands.get(key);
	}

	/**
	 * Attempts to execute the specified command.
	 *
	 * @param key The command to execute
	 * @throws TerminalCommandException if command does not exist
	 */
	public synchronized void execute(final String key) throws TerminalCommandException {
		if (!contains(key)) throw new TerminalCommandException(String.format(ERROR_NO_SUCH_COMMAND, key));
		else commands.get(key).execute();
	}

	/**
	 * @return a full list of human-readable descriptions of all commands.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (TerminalCommand command : commands.values()) sb.append(command).append('\n');
		return sb.toString().trim();
	}
}
