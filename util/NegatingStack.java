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

package com.vicinityconcepts.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class NegatingStack<T> extends Stack<T> {
	private final Map<T, T> map;

	public NegatingStack() {
		super();
		map = new HashMap<>();
	}

	public void addNegationRule(T value, T negatedBy) {
		map.put(value, negatedBy);
	}

	public void addNegationRule(Tuple<T> tuple) {
		addNegationRule(tuple.left, tuple.right);
	}

	@Override
	public T push(T value) {
		if (isEmpty()) return super.push(value);
		if (map.containsKey(peek()) && map.get(peek()).equals(value)) return pop();
		return super.push(value);
	}
}
