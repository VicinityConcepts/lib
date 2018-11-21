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

package com.vicinityconcepts.json;

public class JSONToken {
	private static final String ERROR_GET_CHAR = "Tried to get non-char value as char from JSON token: %s";

	public enum Type {
		BOUNDARY,
		OPERATOR,
		DELIMITER,
		LITERAL,
		NULL
	}

	private final Type type;
	private final Object value;

	public JSONToken(Type type, Object value) {
		if (type == null) type = Type.NULL;
		this.type = type;
		this.value = value;
	}

	public Type getType() {
		return type;
	}

	public Object getValue() {
		return value;
	}

	public char getChar() throws JSONException {
		if (!(value instanceof String)) throw new JSONException(String.format(ERROR_GET_CHAR, value));
		String strValue = (String) value;
		if (strValue.length() != 1) throw new JSONException(String.format(ERROR_GET_CHAR, strValue));
		return strValue.charAt(0);
	}

	public boolean equals(JSONToken token) {
		boolean typeMatch = this.type == token.type;
		if (value == null) return typeMatch && token.value == null;
		return typeMatch && value.equals(token.value);
	}

	public boolean equalsType(JSONToken.Type type) {
		return this.type == type;
	}

	public boolean equalsValue(String value) {
		if (this.value == null) return value == null;
		return this.value.equals(value);
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}
}
