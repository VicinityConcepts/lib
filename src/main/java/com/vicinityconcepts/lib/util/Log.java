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

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * This utility class serves as a complete logging system.
 *
 * <p>Applications can log messages through this system which will be
 * timestamped and written to a text file during runtime.
 * </p>
 *
 * @author Ryan Palmer
 */
public final class Log {
	/**
	 * Constants
	 */
	private static final PrintStream DEFAULT_OUTPUT = System.out;
	private static final int DEFAULT_LOG_WRITE_INTERVAL = 1000;
	private static final String DEFAULT_FILE_NAME = "runtime.log";
	private static final String STACK_TRACE_FORMAT = "\n%s.%s(%s:%s)";
	private static final String STACK_TRACE_MESSAGE_FORMAT = "MESSAGE: %s";
	private static final String DEFAULT_ERROR_MESSAGE = "An error occurred.";
	private static final String ERROR_WRITE_FAILED = "Failed to write logs to file.";
	private static final String ERROR_CLOSE_INTERRUPTED = "Interrupted while waiting for log service to finish. Service may not have completed properly.";
	/**
	 * The singleton log instance
	 */
	private static final Logger LOGGER = new InternalLogger();
	/**
	 * The service that will handle reading from the queue and writing log entries
	 * to the log file.
	 */
	private static final Service LOG_SERVICE = new LogService();
	/**
	 * Incoming log messages will be placed in this queue, and the log service
	 * will read them in order and write them to the log file.
	 */
	private static final ConcurrentLinkedQueue<String> LOG_BUFFER = new ConcurrentLinkedQueue<>();
	/**
	 * The output stream to which log messages will be written in addition to
	 * the log file.
	 */
	private static volatile PrintStream output = DEFAULT_OUTPUT;
	/**
	 * If echo is enabled, log messages will be written to the specified output
	 * stream in addition to the log file.
	 */
	private static volatile boolean echo = true;
	/**
	 * The name of the log file to which log entries will be written.
	 */
	private static volatile String logFileName = DEFAULT_FILE_NAME;
	/**
	 * How often queued log messages will be written to the log file.
	 */
	private static volatile int logWriteInterval = DEFAULT_LOG_WRITE_INTERVAL;
	/**
	 * Used to prevent log from being started or stopped multiple times.
	 */
	private static volatile boolean running = false;
	/**
	 * The writer used to write the log file.
	 */
	private static FileWriter logFileWriter;

	/**
	 * Instances cannot be constructed outside of this class.
	 */
	private Log() {
	}

	/**
	 * Start reading from the queue and writing entries to the log file.
	 *
	 * @throws IOException if an I/O error occurs while constructing the file writer.
	 */
	public static void start() throws IOException {
		if (running) return;
		running = true;
		constructFileWriter();
		LOG_SERVICE.start();
	}

	/**
	 * Stop reading from the queue and writing entries to the log file.
	 *
	 * @throws IOException if an I/O error occurs while closing the file writer.
	 */
	public static void stop() throws IOException {
		if (!running) return;
		try {
			LOG_SERVICE.stop();
			LOG_SERVICE.join();
		} catch (InterruptedException e) {
			Log.error(ERROR_CLOSE_INTERRUPTED);
		} finally {
			running = false;
			checkLogBufferAndWriteNewEntries();
			if (logFileWriter != null) logFileWriter.close();
		}
	}

	/**
	 * Read all currently queued log entries until the queue is empty, and write
	 * them to the log file all at once.
	 */
	private static void checkLogBufferAndWriteNewEntries() {
		StringBuilder sb = new StringBuilder();
		while (!LOG_BUFFER.isEmpty()) sb.append(LOG_BUFFER.remove()).append('\n');
		writeToLogFile(sb.toString());
	}

	/**
	 * Generic log function that writes arbitrary text to the log file.
	 *
	 * @param level   The level of this log message.
	 * @param message The message to log.
	 * @param t       Error or exception to log with the message. Can be {@code null} if
	 *                there is no error or exception to log.
	 */
	private static void log(Level level, String message, Throwable t) {
		try {
			LOGGER.log(new InternalLogEntry(level, message, Caller.infer(Log.class.getName()), t));
		} catch (CallerNotFoundException e) {
			try {
				// Fall back on rewind if inferring is unsuccessful. Try stepping
				// back two times, once for this method and once for whichever specific
				// logging method (info, warn, error) called this one.
				LOGGER.log(new InternalLogEntry(level, message, Caller.rewind(2), t));
			} catch (CallerNotFoundException e2) {
				// If all else fails, just give up and log without caller info
				LOGGER.log(new InternalLogEntry(level, message, Caller.unknown(), t));
			}
		}
	}

	/**
	 * Log an informative message to the log file.
	 *
	 * @param message The message to log.
	 */
	public static void info(String message) {
		log(Level.INFO, message, null);
	}

	/**
	 * Log a warning message to the log file.
	 *
	 * @param message The message to log.
	 */
	public static void warn(String message) {
		log(Level.WARNING, message, null);
	}

	/**
	 * Log an error message.
	 *
	 * @param message The message to log.
	 */
	public static void error(String message) {
		error(message, null);
	}

	/**
	 * Log an error or exception with the default message.
	 *
	 * @param t The error or exception to display.
	 */
	public static void error(Throwable t) {
		error(DEFAULT_ERROR_MESSAGE, t);
	}

	/**
	 * Log an error message, and include the name and stack trace of the
	 * associated exception or error.
	 *
	 * @param message The message to log.
	 * @param t       The error or exception to display with the message.
	 */
	public static void error(String message, Throwable t) {
		log(Level.SEVERE, message, t);
	}

	/**
	 * Add arbitrary text to the log buffer, replacing newlines with the
	 * newline replacement string.
	 *
	 * @param text Text to add to the buffer.
	 */
	private static void addToLogBuffer(String text) {
		LOG_BUFFER.offer(text);
	}

	/**
	 * Write arbitrary text to the log file.
	 *
	 * @param text Text to write.
	 */
	private static void writeToLogFile(String text) {
		try {
			logFileWriter.write(text);
		} catch (IOException e) {
			error(ERROR_WRITE_FAILED, e);
		}
	}

	/**
	 * Write arbitrary text to the output stream specified for this logger, as long
	 * as the stream is non-null and echo is enabled.
	 *
	 * @param text Text to write.
	 */
	private static void echo(String text) {
		if (echo && output != null) output.println(text);
	}

	/**
	 * Enable or disable echo, which will write log entries to the currently specified
	 * output stream in addition to the log file.
	 *
	 * @param enabled Enable/disable echo.
	 */
	public static void setEchoEnabled(boolean enabled) {
		echo = enabled;
	}

	/**
	 * Set the output stream to which log messages will be written in addition to
	 * the log file.
	 *
	 * <p>
	 * When calling any of the logging methods, the log entry will
	 * be both printed to the output stream and written to the log file. You can
	 * also set this to null to disable printing to the output stream. The log file
	 * will still be written to, however.
	 * </p>
	 *
	 * @param stream The output stream to write to.
	 */
	public static void setOutputStream(PrintStream stream) {
		output = stream;
	}

	/**
	 * Set a new name for the log file to which log entries will be written.
	 *
	 * <p>
	 * Log service must be restarted for changes to take effect. (Ex: {@code "log.txt"})
	 * </p>
	 *
	 * @param name New name for the log file
	 */
	public static void setLogFileName(String name) {
		logFileName = name;
	}

	/**
	 * Set how often queued log messages will be written to the log file.
	 *
	 * @param interval The delay in milliseconds between writes.
	 */
	public static void setLogWriteInterval(int interval) {
		logWriteInterval = interval;
	}

	/**
	 * Reconstruct the file writer with the current log file name.
	 *
	 * @throws IOException if an I/O error occurs while constructing the file writer.
	 */
	private static void constructFileWriter() throws IOException {
		logFileWriter = new FileWriter(logFileName, true);
	}

	/**
	 * Get a log-friendly formatted stack trace from an error or exception.
	 *
	 * @param t The error or exception to get the stack trace of.
	 * @return a log-friendly stack trace.
	 */
	public static String getStackTrace(Throwable t) {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format(STACK_TRACE_MESSAGE_FORMAT, t));
		for (StackTraceElement e : t.getStackTrace())
			sb.append(String.format(STACK_TRACE_FORMAT, e.getClassName(), e.getMethodName(), e.getFileName(), e.getLineNumber()));
		return sb.toString();
	}

	/**
	 * The internal logger class definition
	 */
	private static class InternalLogger extends Logger {
		private static final String INTERNAL_LOGGER_NAME = "com.vicinityconcepts";
		private static final String INFO_FORMAT = "%s | %s | %s";
		private static final String ERROR_FORMAT = "%s | %s | %s | Source: %s | %s";
		private static final String NEWLINE_REPLACEMENT = "\n   ";

		/**
		 * Constructor that does not specify a resource bundle, meaning the logging
		 * does not require localization.
		 */
		private InternalLogger() {
			super(INTERNAL_LOGGER_NAME, null);
		}

		/**
		 * All logging activity eventually reaches this method, which builds the resulting
		 * log entry based on the received log entry object.
		 *
		 * @param record The received log entry.
		 */
		@Override
		public void log(LogRecord record) {
			InternalLogEntry e = (InternalLogEntry) record;
			Level level = e.getLevel();
			String line;

			// Format the log entry based on level
			if (level == Level.WARNING || level == Level.SEVERE) {
				line = String.format(ERROR_FORMAT, e.getTimestamp(), e.getThreadName(), e.getLevel(), e.getCaller(), e.getMessage());
				Throwable t = e.getThrowable();
				if (t != null) line += "\n" + getStackTrace(t);
			} else line = String.format(INFO_FORMAT, e.getTimestamp(), e.getThreadName(), e.getMessage());
			line = line.replace("\n", NEWLINE_REPLACEMENT); // Apply indentation to newlines

			// Echo the line and add to the buffer
			echo(line);
			addToLogBuffer(line);
		}
	}

	/**
	 * Subclass of LogRecord which is passed to the internal logger, adding some
	 * additional features such as a caller object, timestamp, and thread name.
	 */
	private static class InternalLogEntry extends LogRecord {
		private final Caller caller;
		private final Throwable throwable;
		private final String timestamp;

		public InternalLogEntry(Level level, String msg, Caller caller, Throwable throwable) {
			super(level, msg);
			this.caller = caller;
			this.throwable = throwable;
			timestamp = TimeUtils.getTimestamp();
		}

		@Override
		public String getSourceClassName() {
			return caller.getClassName();
		}

		@Override
		public String getSourceMethodName() {
			return caller.getMethodName();
		}

		public String getThreadName() {
			return caller.getThreadName();
		}

		public Caller getCaller() {
			return caller;
		}

		public Throwable getThrowable() {
			return throwable;
		}

		public String getTimestamp() {
			return timestamp;
		}
	}

	/**
	 * This service will run on a dedicated thread, reading buffered log entries
	 * from the queue and writing them to the log file.
	 */
	private static class LogService extends Service {
		@Override
		protected void run() {
			try {
				Thread.sleep(logWriteInterval);
			} catch (InterruptedException ignore) {
				// We don't care about interruptions
			} finally {
				checkLogBufferAndWriteNewEntries();
			}
		}
	}
}
