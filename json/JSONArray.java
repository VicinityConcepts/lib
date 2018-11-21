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

import java.util.ArrayList;
import java.util.List;

public class JSONArray implements JSONConstants {
	private static final String FORMAT_ARRAY = "[%s]";
	private static final String FORMAT_ELEMENT = "%s,";
	private static final String ERROR_TYPE_MISMATCH = "Value of element %d is not a %s.";

	private final List<Object> values;

	public JSONArray() {
		values = new ArrayList<>();
	}

	public void add(Object value) throws JSONException {
		if (value != null) JSONConstants.ensureTypeAllowed(value);
		values.add(value);
	}

	public Object get(int index) throws IndexOutOfBoundsException {
		return values.get(index);
	}

	public JSONObject getObject(int index) throws IndexOutOfBoundsException, JSONException {
		return get(index, JSONObject.class);
	}

	public boolean getBoolean(int index) throws IndexOutOfBoundsException, JSONException {
		try {
			return get(index, Boolean.class);
		} catch (NullPointerException e) {
			throw new JSONException(String.format(ERROR_TYPE_MISMATCH, index, Boolean.class.getSimpleName()));
		}
	}

	public long getLong(int index) throws IndexOutOfBoundsException, JSONException {
		try {
			return get(index, Long.class);
		} catch (NullPointerException e) {
			throw new JSONException(String.format(ERROR_TYPE_MISMATCH, index, Long.class.getSimpleName()));
		}
	}

	public double getDouble(int index) throws IndexOutOfBoundsException, JSONException {
		try {
			return get(index, Double.class);
		} catch (NullPointerException e) {
			throw new JSONException(String.format(ERROR_TYPE_MISMATCH, index, Double.class.getSimpleName()));
		}
	}

	public String getString(int index) throws IndexOutOfBoundsException, JSONException {
		return get(index, String.class);
	}

	public JSONArray getArray(int index) throws IndexOutOfBoundsException, JSONException {
		return get(index, JSONArray.class);
	}

	private <T> T get(int index, Class<T> type) throws IndexOutOfBoundsException, JSONException {
		try {
			return type.cast(get(index));
		} catch (ClassCastException e) {
			throw new JSONException(String.format(ERROR_TYPE_MISMATCH, index, type.getSimpleName()));
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Object obj : values) {
			if (obj instanceof String) obj = String.format(FORMAT_STRING, obj);
			sb.append(String.format(FORMAT_ELEMENT, obj));
		}

		String result = sb.toString();
		if (result.length() > 0) result = result.substring(0, result.length() - 1);
		return String.format(FORMAT_ARRAY, result);
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof JSONArray)) return super.equals(other);
		JSONArray otherArr = (JSONArray) other;

		try {
			for (int i = 0; i < values.size(); i++) if (!get(i).equals(otherArr.get(i))) return false;
			return true;
		} catch (IndexOutOfBoundsException e) {
			return false;
		}
	}
}
