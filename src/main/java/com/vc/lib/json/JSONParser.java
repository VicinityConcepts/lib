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

package com.vc.lib.json;

public class JSONParser implements JSONConstants {
	private static final String ERROR_ARRAY_BOUNDARY = "JSON arrays must start and end with " + ARRAY_BOUNDARY.left + " array " + ARRAY_BOUNDARY.right + " boundaries.";
	private static final String ERROR_OBJECT_BOUNDARY = "JSON objects must start and end with " + OBJECT_BOUNDARY.left + " object " + OBJECT_BOUNDARY.right + " boundaries.";
	private static final String ERROR_TOKEN = "Unexpected token: %s";

	private JSONParser() {
	}

	public static JSONArray parseArray(final String source) throws JSONException {
		return parseArray(new JSONLexer(source));
	}

	public static JSONArray parseArray(final JSONLexer lex) throws JSONException {
		JSONToken first = lex.next(); // Must iterate past the first token to prevent infinite recursion
		if (first.getChar() != ARRAY_BOUNDARY.left) throw new JSONException(ERROR_ARRAY_BOUNDARY);

		JSONArray array = new JSONArray();
		while (lex.hasNext()) {
			JSONToken next = lex.next();
			if (JSONConstants.isLeftBoundary(next)) {
				if (next.getChar() == OBJECT_BOUNDARY.left) array.add(parseObject(lex.snip()));
				else if (next.getChar() == ARRAY_BOUNDARY.left) array.add(parseArray(lex.snip()));
			} else if (next.equalsType(JSONToken.Type.LITERAL)) {
				array.add(next.getValue());
			} else throw new JSONException(String.format(ERROR_TOKEN, next));

			// We may have a single comma at the end, or closing boundary
			next = lex.peek();
			if (next.equalsType(JSONToken.Type.DELIMITER) || JSONConstants.isRightBoundary(next)) lex.next();
		}

		return array;
	}

	public static JSONObject parseObject(final String source) throws JSONException {
		return parseObject(new JSONLexer(source));
	}

	public static JSONObject parseObject(final JSONLexer lex) throws JSONException {
		JSONToken first = lex.next(); // Must iterate past the first token to prevent infinite recursion
		if (first.getChar() != OBJECT_BOUNDARY.left) throw new JSONException(ERROR_OBJECT_BOUNDARY);

		JSONObject object = new JSONObject();
		while (lex.hasNext()) {
			// First one should always be a key, followed by the ':' operator
			JSONToken key = lex.next();
			if (!key.equalsType(JSONToken.Type.LITERAL))
				throw new JSONException(String.format(ERROR_TOKEN, key));
			if (!lex.next().equalsType(JSONToken.Type.OPERATOR))
				throw new JSONException(String.format(ERROR_TOKEN, lex.current()));

			// Next should be either an array, an object, or a literal value
			JSONToken value = lex.next();
			if (JSONConstants.isLeftBoundary(value)) {
				if (value.getChar() == OBJECT_BOUNDARY.left)
					object.put(key.toString(), parseObject(lex.snip()));
				else if (value.getChar() == ARRAY_BOUNDARY.left)
					object.put(key.toString(), parseArray(lex.snip()));
			} else {
				if (!value.equalsType(JSONToken.Type.LITERAL))
					throw new JSONException(String.format(ERROR_TOKEN, value));
				object.put(key.toString(), value.getValue());
			}

			// We may have a single comma at the end, or closing boundary
			JSONToken next = lex.peek();
			if (next.equalsType(JSONToken.Type.DELIMITER) || JSONConstants.isRightBoundary(next)) lex.next();
		}

		return object;
	}
}
