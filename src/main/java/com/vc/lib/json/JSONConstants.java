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

import com.vc.lib.util.Tuple;

interface JSONConstants {
	Tuple<Character> ARRAY_BOUNDARY = new Tuple<>('[', ']');
	Tuple<Character> OBJECT_BOUNDARY = new Tuple<>('{', '}');
	String FORMAT_STRING = "\"%s\"";
	String ERROR_TYPE_NOT_ALLOWED = "Object type %s not allowed in JSON.";
	Class[] ALLOWED_TYPES = {String.class, Short.class, Integer.class, Long.class, Float.class, Double.class, Boolean.class, JSONObject.class, JSONArray.class};

	static void ensureTypeAllowed(Object value) throws JSONException {
		for (Class c : ALLOWED_TYPES) if (c.isInstance(value)) return;
		throw new JSONException(String.format(ERROR_TYPE_NOT_ALLOWED, value.getClass().getSimpleName()));
	}

	static boolean isLeftBoundary(JSONToken boundary) {
		try {
			return boundary.equalsType(JSONToken.Type.BOUNDARY) && isLeftBoundary(boundary.getChar());
		} catch (JSONException e) {
			return false;
		}
	}

	static boolean isLeftBoundary(char boundary) {
		return boundary == ARRAY_BOUNDARY.left || boundary == OBJECT_BOUNDARY.left;
	}

	static boolean isRightBoundary(JSONToken boundary) {
		try {
			return boundary.equalsType(JSONToken.Type.BOUNDARY) && isRightBoundary(boundary.getChar());
		} catch (JSONException e) {
			return false;
		}
	}

	static boolean isRightBoundary(char boundary) {
		return boundary == ARRAY_BOUNDARY.right || boundary == OBJECT_BOUNDARY.right;
	}
}