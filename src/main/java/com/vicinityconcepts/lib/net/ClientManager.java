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

import com.vicinityconcepts.lib.util.Service;

import java.io.IOException;
import java.net.SocketTimeoutException;

public class ClientManager extends Service {
	private static final String ERROR_ACCEPT = "Client manager encountered an error while waiting for new connections.";

	private final Server server;

	public ClientManager(Server server) {
		this.server = server;
	}

	@Override
	protected void run() {
		try {
			server.addClient(server.accept());
		} catch (SocketTimeoutException ignore) {
			// Timeouts are expected
		} catch (IOException e) {
			LOG.error(ERROR_ACCEPT, e);
		}
	}

	@Override
	public void stop() {
		super.stop();
		server.disconnectAllClients();
	}
}
