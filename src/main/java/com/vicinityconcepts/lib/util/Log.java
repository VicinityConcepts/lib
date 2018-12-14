package com.vicinityconcepts.lib.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

public final class Log {
	private static final String PROPERTIES_FILE = "logging.properties";
	private static final String CONFIG_MAIN_CLASS = "main.class";
	private static final Logger LOGGER = initialize();

	private Log() {
	}

	private static Logger initialize() {
		try (InputStream in = Log.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
			if (in == null) throw new FileNotFoundException();
			Properties config = new Properties();
			config.load(in);
			Class main = Class.forName(config.getProperty(CONFIG_MAIN_CLASS));
			Logger logger = LogManager.getLogger(main);
			return logger;
		} catch (FileNotFoundException e) {
			System.err.println("Failed to locate '" + PROPERTIES_FILE + "' file in classpath. Defaults will be used.");
			return LogManager.getRootLogger();
		} catch (Exception e) {
			System.err.println("Encountered an error while loading properties. Defaults will be used.");
			e.printStackTrace();
			return LogManager.getRootLogger();
		}
	}

	public static void info(String message) {
		LOGGER.info(message);
	}

	public static void warn(String message) {
		LOGGER.warn(message);
	}

	public static void error(String message) {
		LOGGER.error(message);
	}

	public static void error(String message, Throwable e) {
		LOGGER.error(message, e);
	}
}
