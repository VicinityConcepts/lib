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

/**
 * A procedure is anything that can be started and stopped. It also
 * has a name to identify itself.
 *
 * @author Ryan Palmer
 */
public interface Procedure {
	/**
	 * Start the procedure.
	 */
	boolean start();

	/**
	 * Stop the procedure.
	 */
	boolean stop();

	/**
	 * Get the identifying name of the procedure, which defaults to
	 * the name of the implementing class.
	 */
	default String getName() {
		return this.getClass().getSimpleName();
	}
}