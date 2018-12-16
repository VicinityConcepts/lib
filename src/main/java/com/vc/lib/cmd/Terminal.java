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

import com.vc.lib.util.InputReader;
import com.vc.lib.util.Procedure;
import com.vc.lib.util.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Utility for configuring and processing console commands to perform pre-defined actions.
 *
 * <p>
 * This utility processes command line input to execute pre-defined actions based on
 * configured commands, as well as provides a simple interface for getting input/output
 * from/to the user.
 * </p>
 *
 * <p>
 * The class can provide basic input/output functionality without any additional configuration.
 * However, command-action mappings must be explicitly defined after construction. By default,
 * in order to prevent an unstoppable terminal, the 'exit' command is already defined and mapped,
 * but it is possible to override this with a custom command or action.
 * </p>
 *
 * <p>
 * This class will continuously check for user input and interpret command as they are received.
 * Thread safety is enforced by the mutual exclusion object specified in the constructor. This is
 * to ensure no commands can be executed until the previous command execution is complete.
 * </p>
 *
 * @author Ryan Palmer
 */
public class Terminal extends Service {
	protected static final Logger LOG = LogManager.getLogger();
	private static final InputStream DEFAULT_INPUT = System.in;
	private static final OutputStream DEFAULT_OUTPUT = System.out;

	private static final String DEFAULT_HELP_COMMAND = "help";
	private static final String DEFAULT_HELP_DESCRIPTION = "Display this help page.";
	private static final String DEFAULT_EXIT_COMMAND = "exit";
	private static final String DEFAULT_EXIT_DESCRIPTION = "Stop the active procedure and close the terminal.";

	private static final String GET_INPUT_FORMAT = "%s: ";
	private static final String NEW_SERVICE = "New procedure attached to terminal: %s";
	private static final String SERVICE_STOPPED = "Terminal stopped attached procedure: %s";
	private static final String ADD_OR_REPLACE_FAILED = "An error occurred while adding command to the registry.";

	private static final String ERROR_UNRECOGNIZED_COMMAND = "Command not recognized: %s";
	private static final String ERROR_DUPLICATE_COMMAND = "Command '%s' is already assigned.";
	private static final String ERROR_NO_SUCH_COMMAND = "Command '%s' does not exist.";

	private static final int LOOP_RATE = 1000 / 30; // Process input 30 times per second

	/**
	 * The input stream from which to read input data.
	 */
	private final InputReader input;

	/**
	 * The output stream to which to write output data.
	 */
	private final PrintStream output;

	/**
	 * This registry stores all configured commands.
	 */
	private final TerminalCommandRegistry registry;

	/**
	 * A lock will be acquired on this object for the duration of any command execution.
	 */
	private final Object commandMutex;

	/**
	 * The procedure attached to this terminal. When the terminal exits, the attached
	 * procedure will be stopped.
	 */
	private Procedure procedure;

	/**
	 * Construct an interactive terminal with the standard input and output.
	 *
	 * <p>
	 * The terminal will immediately begin receiving input when constructed.
	 * </p>
	 */
	public Terminal() {
		this(DEFAULT_INPUT, DEFAULT_OUTPUT);
	}

	/**
	 * Construct an interactive terminal.
	 *
	 * <p>
	 * The terminal will immediately begin receiving input when constructed.
	 * </p>
	 *
	 * @param input  Stream from which to read command input
	 * @param output Stream to write standard output to
	 */
	public Terminal(InputStream input, OutputStream output) {
		commandMutex = new Object();
		this.input = new InputReader(input);
		this.output = new PrintStream(output, true);

		// Define the default exit and help commands and construct registry
		TerminalCommand help = new TerminalCommand(DEFAULT_HELP_COMMAND, this::help, DEFAULT_HELP_DESCRIPTION);
		TerminalCommand exit = new TerminalCommand(DEFAULT_EXIT_COMMAND, this::stop, DEFAULT_EXIT_DESCRIPTION);
		registry = new TerminalCommandRegistry(help, exit);

		// Start receiving input
		setLoopRate(LOOP_RATE);
		start();
	}

	/**
	 * Add a new command-action mapping to the controls.
	 *
	 * @param command The command to be added
	 * @throws TerminalCommandException if command already exists
	 */
	public void addCommand(TerminalCommand command) throws TerminalCommandException {
		if (registry.contains(command))
			throw new TerminalCommandException(String.format(ERROR_DUPLICATE_COMMAND, command.getKey()));
		addOrReplaceCommand(command);
	}

	/**
	 * Replace an existing command-action mapping with the same key.
	 *
	 * @param command The command to be replaced
	 * @throws TerminalCommandException if command does not exist
	 */
	public void replaceCommand(TerminalCommand command) throws TerminalCommandException {
		if (!registry.contains(command))
			throw new TerminalCommandException(String.format(ERROR_NO_SUCH_COMMAND, command.getKey()));
		addOrReplaceCommand(command);
	}

	/**
	 * Put a command-action mapping in the controls.
	 *
	 * <p>
	 * Will replace the current mapping if the command exists already
	 * </p>
	 *
	 * @param command The command to be added
	 */
	public void addOrReplaceCommand(TerminalCommand command) {
		try {
			registry.put(command);
		} catch (TerminalCommandException e) {
			LOG.error(ADD_OR_REPLACE_FAILED, e);
		}
	}

	/**
	 * Check if there's a line of input, and if so, return it.
	 *
	 * @return a line of input.
	 */
	private String getLine() {
		synchronized (input) {
			if (input.hasNext()) return input.nextLine();
			else return null;
		}
	}

	/**
	 * Wait for a line of text input and return it.
	 *
	 * @return a line of input.
	 */
	private String waitForLine() {
		synchronized (input) {
			return input.nextLine();
		}
	}

	/**
	 * Get a line of text input from the user.
	 *
	 * @param message Message to display to the user.
	 * @return the line entered by the user.
	 */
	public String requestString(String message) {
		output.print(String.format(GET_INPUT_FORMAT, message));
		return waitForLine();
	}

	/**
	 * Prints a line of arbitrary text to the terminal's output stream.
	 *
	 * @param text The text to print.
	 */
	public void println(String text) {
		output.println(text);
	}

	/**
	 * Get an integer from the user.
	 *
	 * @param message Message to display to the user.
	 * @return the number entered by the user.
	 * @throws NumberFormatException if the user did not enter a valid number.
	 */
	public int requestInt(String message) throws NumberFormatException {
		return Integer.parseInt(requestString(message));
	}

	/**
	 * Print summary of command registry.
	 */
	public void help() {
		output.println(registry.toString());
	}

	/**
	 * Attach a procedure to this terminal which will be stopped when it exits.
	 *
	 * <p>
	 * If another procedure is currently attached, it will be stopped.
	 * </p>
	 *
	 * @param procedure The procedure to attach
	 */
	public void attach(Procedure procedure) {
		stopAttachedService();
		this.procedure = procedure;
		LOG.info(String.format(NEW_SERVICE, this.procedure.getName()));
	}

	/**
	 * If a procedure is attached to this terminal, stop it.
	 */
	private void stopAttachedService() {
		if (this.procedure != null) {
			this.procedure.stop();
			LOG.info(String.format(SERVICE_STOPPED, this.procedure.getName()));
		}
	}

	/**
	 * Continuously check for user input and attempt to execute a command based on that input.
	 */
	@Override
	protected void run() {
		String line = getLine();
		if (line != null) {
			try {
				synchronized (commandMutex) {
					registry.execute(line);
				}
			} catch (TerminalCommandException e) {
				output.println(String.format(ERROR_UNRECOGNIZED_COMMAND, line));
			}
		}
	}

	/**
	 * After execution stops, we need to close the procedure.
	 *
	 * <p>
	 * This will NOT close the input or output streams, so it is the responsibility of the
	 * implementer to close these streams when finished with the terminal. If no streams
	 * were explicitly passed to the terminal, it will use the default System streams which
	 * probably should not be closed anyway.
	 * </p>
	 */
	@Override
	public void stop() {
		super.stop();
		stopAttachedService();
	}
}
