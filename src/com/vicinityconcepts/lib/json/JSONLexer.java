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

package com.vicinityconcepts.lib.json;

import com.vicinityconcepts.lib.util.NegatingStack;

import java.util.*;

public class JSONLexer implements JSONConstants {
	private static final String CHAR_DELIM = "";
	private static final String REGEX_TOKEN_DELIM = "[\\[\\]{}:,]";
	private static final String REGEX_BOUNDARY = "[\\[\\]{}]";
	private static final String REGEX_OPERATOR = ":";
	private static final String REGEX_DELIMITER = ",";
	private static final String REGEX_LITERAL = "(\"[^\"\n]*\"|-?\\d*\\.?\\d*([eE][-+]?\\d+)?|(true|false)|null)";
	private static final String REGEX_WHITESPACE = "\\s";
	private static final String REGEX_QUOTE = "\"";
	private static final String REGEX_LITERAL_NULL = "^null$";
	private static final String REGEX_LITERAL_BOOL = "^(true|false)$";
	private static final String REGEX_LITERAL_NUM = "^-?\\d*\\.?\\d*([eE][-+]?\\d+)?$";
	private static final String ERROR_SANITIZE = "JSON must start and end with either " + ARRAY_BOUNDARY.left + " array " + ARRAY_BOUNDARY.right + " boundaries or " + OBJECT_BOUNDARY.left + " object " + OBJECT_BOUNDARY.right + " boundaries.";
	private static final String ERROR_TOKEN = "Unexpected token: %s";
	private static final String ERROR_FORMATTING = "Improper formatting.";
	private static final String ERROR_TYPE_DEDUCTION = "Unable to deduce type of non-string literal: %s";
	private static final String ERROR_SNIP_NULL = "Cannot call snip() before calling next().";
	private static final String ERROR_SNIP_BOUNDARY = "Must call snip() on an opening boundary token.";
	private static final String ERROR_SNIP_NO_MATCHING_BOUNDARY = "Failed to find matching boundary when snipping JSON tokens from lexer.";

	private final Queue<JSONToken> tokens;
	private final ListIterator<JSONToken> it;
	private JSONToken current = null;

	public JSONLexer(String source) throws JSONException {
		tokens = new LinkedList<>();
		tokenize(sanitize(source));
		it = ((LinkedList<JSONToken>) tokens).listIterator();
	}

	private JSONLexer(LinkedList<JSONToken> tokens) {
		this.tokens = tokens;
		it = ((LinkedList<JSONToken>) this.tokens).listIterator();
	}

	private static NegatingStack<Character> getBoundaryNegatingStack() {
		NegatingStack<Character> stack = new NegatingStack<>();
		stack.addNegationRule(ARRAY_BOUNDARY);
		stack.addNegationRule(OBJECT_BOUNDARY);
		return stack;
	}

	private static JSONToken getStringLiteralToken(String literal) throws NoSuchElementException {
		if (literal.length() < 2 || literal.charAt(0) != '"' || literal.charAt(literal.length() - 1) != '"')
			throw new NoSuchElementException(); // Will be caught like an unexpected token
		return new JSONToken(JSONToken.Type.LITERAL, literal.substring(1, literal.length() - 1));
	}

	private static JSONToken getTypedLiteralToken(String literal) throws JSONException {
		if (literal.matches(REGEX_LITERAL_NULL))
			return new JSONToken(JSONToken.Type.LITERAL, null);
		else if (literal.matches(REGEX_LITERAL_BOOL))
			return new JSONToken(JSONToken.Type.LITERAL, Boolean.parseBoolean(literal));
		else if (literal.matches(REGEX_LITERAL_NUM)) {
			try {
				try {
					// First try to parse a whole number value
					return new JSONToken(JSONToken.Type.LITERAL, Long.parseLong(literal));
				} catch (NumberFormatException e) {
					// If that fails try to parse a decimal or E notation
					return new JSONToken(JSONToken.Type.LITERAL, Double.parseDouble(literal));
				}
			} catch (NumberFormatException e) {
				throw new JSONException(String.format(ERROR_TYPE_DEDUCTION, literal));
			}
		} else throw new JSONException(String.format(ERROR_TYPE_DEDUCTION, literal));
	}

	private String sanitize(String source) throws JSONException {
		source = source.replace("\n", "");

		for (int i = 0; i < source.length(); i++) {
			try {
				char c = source.charAt(i);
				if (c == ARRAY_BOUNDARY.left) return source.substring(i, source.lastIndexOf(ARRAY_BOUNDARY.right) + 1);
				else if (c == OBJECT_BOUNDARY.left)
					return source.substring(i, source.lastIndexOf(OBJECT_BOUNDARY.right) + 1);
			} catch (StringIndexOutOfBoundsException e) {
				throw new JSONException(ERROR_SANITIZE);
			}
		}

		throw new JSONException(ERROR_SANITIZE);
	}

	private void tokenize(String source) throws JSONException {
		NegatingStack<Character> boundaries = getBoundaryNegatingStack();
		Scanner sc = new Scanner(source).useDelimiter(CHAR_DELIM);
		while (sc.hasNext()) {
			String next = sc.next();
			if (next.matches(REGEX_BOUNDARY)) {
				boundaries.push(next.charAt(0));
				tokens.add(new JSONToken(JSONToken.Type.BOUNDARY, next));
				if (boundaries.isEmpty()) return; // All boundaries closed
			} else if (next.matches(REGEX_OPERATOR)) {
				tokens.add(new JSONToken(JSONToken.Type.OPERATOR, next));
			} else if (next.matches(REGEX_DELIMITER)) {
				tokens.add(new JSONToken(JSONToken.Type.DELIMITER, next));
			} else if (next.matches(REGEX_QUOTE)) {
				StringBuilder sb = new StringBuilder(next);
				try {
					while (!sb.append(sc.next()).toString().matches(REGEX_LITERAL)) ;
					tokens.add(getStringLiteralToken(sb.toString()));
				} catch (NoSuchElementException e) {
					throw new JSONException(String.format(ERROR_TOKEN, sb.toString()));
				}
			} else if (!next.matches(REGEX_WHITESPACE)) {
				try {
					if (!sc.hasNext(REGEX_TOKEN_DELIM)) next += sc.useDelimiter(REGEX_TOKEN_DELIM).next();
					sc.useDelimiter(CHAR_DELIM); // Reset the delimiter
					if (next.matches(REGEX_LITERAL)) tokens.add(getTypedLiteralToken(next));
					else throw new JSONException(String.format(ERROR_TOKEN, next));
				} catch (NoSuchElementException e) {
					throw new JSONException(ERROR_FORMATTING);
				}
			}
		}

		// If we reach this point, the source must be malformed.
		throw new JSONException(ERROR_FORMATTING);
	}

	public JSONLexer snip() throws JSONException, NoSuchElementException {
		if (current == null) throw new JSONException(ERROR_SNIP_NULL);
		if (!JSONConstants.isLeftBoundary(current)) throw new JSONException(ERROR_SNIP_BOUNDARY);
		LinkedList<JSONToken> snippedTokens = new LinkedList<>();
		NegatingStack<Character> boundaries = getBoundaryNegatingStack();
		boundaries.push(current.getChar());
		snippedTokens.add(current);
		//System.out.println("Snipping: " + current);
		while (hasNext()) {
			JSONToken next = next();
			snippedTokens.add(next);
			//System.out.println("Snipping: " + next);
			if (next.equalsType(JSONToken.Type.BOUNDARY)) {
				boundaries.push(next.getChar());
				//		System.out.println(boundaries);
				if (boundaries.isEmpty()) return new JSONLexer(snippedTokens);
			}
		}

		// If we reach this point, there was no matching boundary.
		throw new JSONException(ERROR_SNIP_NO_MATCHING_BOUNDARY);
	}

	public boolean hasNext() {
		return it.hasNext();
	}

	public JSONToken next() throws NoSuchElementException {
		current = it.next();
		return current;
	}

	public JSONToken peek() throws NoSuchElementException {
		JSONToken next = it.next();
		it.previous();
		return next;
	}

	public JSONToken current() {
		return current;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (JSONToken t : tokens) sb.append(t.getValue());
		return sb.toString();
	}
}
