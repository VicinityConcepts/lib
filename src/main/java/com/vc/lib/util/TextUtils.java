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

package com.vc.lib.util;

import java.util.Scanner;

/**
 * Utilities for performing various text-related operations.
 *
 * @author Ryan Palmer
 */
public final class TextUtils {
	private static final int SOURCE_SNIPPET_PADDING = 5;
	private static final String SOURCE_SNIPPET_FORMAT = "%s\n%s%s";
	private static final String SOURCE_SNIPPET_LINE_FORMAT = "%s | %s\n";
	private static final String SOURCE_SNIPPET_TAB_REPLACEMENT = "    ";
	private static final String TAB = "\t";
	private static final char SOURCE_SNIPPET_BORDER_CHAR = '=';

	/**
	 * This class cannot be constructed.
	 */
	private TextUtils() {
	}

	/**
	 * Left-pad a string to the specified width, using the specified character to fill
	 * the extra space.
	 *
	 * <p>
	 * For example, if you pass the string "Hello" with a width of 10 and 'Z' as
	 * the pad character, the result will be "ZZZZZHello".
	 * </p>
	 *
	 * @param text    The text to apply padding to.
	 * @param width   The width of the resulting string, which must be greater than
	 *                or equal to the length of the original string.
	 * @param padChar The character to fill the extra space with.
	 * @return the resulting padded text.
	 */
	public static String padTextLeft(String text, int width, char padChar) {
		int amount = width - text.length();
		if (amount <= 0) return text;
		return repeatChar(padChar, amount) + text;
	}

	/**
	 * Right-pad a string to the specified width, using the specified character to fill
	 * the extra space.
	 *
	 * <p>
	 * For example, if you pass the string "Hello" with a width of 10 and 'Z' as
	 * the pad character, the result will be "HelloZZZZZ".
	 * </p>
	 *
	 * @param text    The text to apply padding to.
	 * @param width   The width of the resulting string, which must be greater than
	 *                or equal to the length of the original string.
	 * @param padChar The character to fill the extra space with.
	 * @return the resulting padded text.
	 */
	public static String padTextRight(String text, int width, char padChar) {
		int amount = width - text.length();
		if (amount <= 0) return text;
		return text + repeatChar(padChar, amount);
	}

	/**
	 * Convert a snippet of source code into a log-friendly string, converting tabs to
	 * spaces, adding line numbers, and surrounding the snippet with border separators.
	 *
	 * @param source The source code snippet to format.
	 * @return the formatted result.
	 */
	public static String formatSourceCodeSnippet(String source) {
		source = source.replace(TAB, SOURCE_SNIPPET_TAB_REPLACEMENT); // Convert tabs to spaces
		StringBuilder sb = new StringBuilder();
		Scanner sc = new Scanner(source);

		int width = 0, count = 1;
		while (sc.hasNextLine()) {
			String lineNumber = Integer.toString(count++);
			String line = String.format(SOURCE_SNIPPET_LINE_FORMAT, padTextLeft(lineNumber, SOURCE_SNIPPET_PADDING, ' '), sc.nextLine());
			if (line.length() > width) width = line.length();
			sb.append(line);
		}

		String border = repeatChar(SOURCE_SNIPPET_BORDER_CHAR, width);
		return String.format(SOURCE_SNIPPET_FORMAT, border, sb.toString(), border);
	}

	/**
	 * Create a string consisting of the same character repeated a specified number of times.
	 *
	 * @param c     The character to repeat.
	 * @param count The number of times to repeat the character.
	 * @return the string containing the repeated character.
	 */
	private static String repeatChar(char c, int count) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < count; i++) sb.append(c);
		return sb.toString();
	}
}
