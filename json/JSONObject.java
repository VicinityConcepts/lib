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

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class JSONObject implements JSONConstants {
	private static final String FORMAT_OBJECT = "{%s}";
	private static final String FORMAT_FIELD = "\"%s\":%s,";
	private static final String ERROR_NO_SUCH_FIELD = "Field does not exist: %s";
	private static final String ERROR_TYPE_MISMATCH = "Value of %s field is not a %s.";

	private final Map<String, Object> fields;

	public JSONObject() {
		fields = new HashMap<>();
	}

	public void put(String key, Object value) throws JSONException {
		if (value != null) JSONConstants.ensureTypeAllowed(value);
		fields.put(key, value);
	}

	public Set<String> getKeys() {
		return fields.keySet();
	}

	public Object get(String key) throws NoSuchElementException {
		if (!fields.containsKey(key)) throw new NoSuchElementException(String.format(ERROR_NO_SUCH_FIELD, key));
		return fields.get(key);
	}

	public JSONObject getObject(String key) throws NoSuchElementException, JSONException {
		return get(key, JSONObject.class);
	}

	public JSONArray getArray(String key) throws NoSuchElementException, JSONException {
		return get(key, JSONArray.class);
	}

	public boolean getBoolean(String key) throws NoSuchElementException, JSONException {
		try {
			return get(key, Boolean.class);
		} catch (NullPointerException e) {
			throw new JSONException(String.format(ERROR_TYPE_MISMATCH, key, Boolean.class.getSimpleName()));
		}
	}

	public long getLong(String key) throws NoSuchElementException, JSONException {
		try {
			return get(key, Long.class);
		} catch (NullPointerException e) {
			throw new JSONException(String.format(ERROR_TYPE_MISMATCH, key, Long.class.getSimpleName()));
		}
	}

	public double getDouble(String key) throws NoSuchElementException, JSONException {
		try {
			return get(key, Double.class);
		} catch (NullPointerException e) {
			throw new JSONException(String.format(ERROR_TYPE_MISMATCH, key, Double.class.getSimpleName()));
		}
	}

	public String getString(String key) throws NoSuchElementException, JSONException {
		return get(key, String.class);
	}

	private <T> T get(String key, Class<T> type) throws NoSuchElementException, JSONException {
		try {
			return type.cast(get(key));
		} catch (ClassCastException e) {
			throw new JSONException(String.format(ERROR_TYPE_MISMATCH, key, type.getSimpleName()));
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (String key : fields.keySet()) {
			Object value = fields.get(key);
			if (value instanceof String) value = String.format(FORMAT_STRING, value);
			sb.append(String.format(FORMAT_FIELD, key, value));
		}

		// Remove the trailing comma if necessary
		String result = sb.toString();
		if (result.length() > 0) result = result.substring(0, result.length() - 1);
		return String.format(FORMAT_OBJECT, result);
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof JSONObject)) return super.equals(other);
		JSONObject otherObj = (JSONObject) other;

		try {
			for (String key : getKeys()) if (!get(key).equals(otherObj.get(key))) return false;
			return true;
		} catch (NoSuchElementException e) {
			return false;
		}
	}
}
