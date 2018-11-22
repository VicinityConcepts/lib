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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utilities for performing various time-related operations.
 *
 * @author Ryan Palmer
 */
public final class TimeUtils {
	/**
	 * Constants
	 */
	private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	private static final String TIMESTAMP_FORMAT = "%s.%s";

	/**
	 * This class cannot be constructed
	 */
	private TimeUtils() {
	}

	/**
	 * Get a string representation of the current date and time in the
	 * specified format. (Ex: "yyyy-MM-dd HH:mm:ss")
	 */
	public static String getTimestamp(String format) {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format);
		return String.format(TIMESTAMP_FORMAT, dtf.format(LocalDateTime.now()), TextUtils.padTextLeft(Integer.toString(getCurrentSecondMillis()), 3, '0'));
	}

	/**
	 * Get a string representation of the current date and time in the
	 * default format.
	 */
	public static String getTimestamp() {
		return getTimestamp(DATE_TIME_FORMAT);
	}

	/**
	 * Get the amount of milliseconds elapsed in the current second, which
	 * will be a value between 0 and 999.
	 */
	public static int getCurrentSecondMillis() {
		String ms = Long.toString(System.currentTimeMillis());
		int end = ms.length();
		int start = (end - 3 >= 0) ? end - 3 : 0;
		return Integer.parseInt(ms.substring(start, end));
	}
}
