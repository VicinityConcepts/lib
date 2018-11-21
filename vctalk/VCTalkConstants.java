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

package com.vicinityconcepts.vctalk;

public interface VCTalkConstants {
	int PORT = 39722;
	String USERNAME_REGEX = "^\\w{3,}$";

	/**
	 * JSON Constants
	 */
	String USERNAME = "user";
	String MESSAGE = "text";
	String VALUE = "value";
	String TYPE = "type";
	String TYPE_MESSAGE = "message";
	String TYPE_USERNAME_VALIDITY_CHECK = "username_validity";
	String TYPE_USERNAME_VALIDITY_RESPONSE = "username_validity_response";
}
