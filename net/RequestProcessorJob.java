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

package com.vicinityconcepts.lib.net;

import com.vicinityconcepts.lib.util.Job;
import com.vicinityconcepts.lib.util.Log;

public abstract class RequestProcessorJob extends Job {
	private static final String ERROR_KILL = "Cannot kill request processor while it is processing.";

	private final Client requestor;
	private final Object request;

	protected RequestProcessorJob(Client requestor, Object request) {
		this.requestor = requestor;
		this.request = request;
	}

	@Override
	public final void run() {
		requestor.send(process(request));
	}

	@Override
	public void kill() {
		Log.error(ERROR_KILL);
	}

	protected abstract Object process(Object request);
}
