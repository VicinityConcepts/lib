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

import com.vicinityconcepts.cmd.Terminal;
import com.vicinityconcepts.cmd.TerminalCommand;
import com.vicinityconcepts.net.*;
import com.vicinityconcepts.util.Log;

import java.io.IOException;

public class VCTalkServer extends Server implements VCTalkConstants {
	private static final String LOG_CLIENT_CONNECTED = "New client connected to server.";
	private static final String LOG_CLIENT_DISCONNECTED = "Client disconnected from server.";
	private static final String LOG_CLIENT_MANUALLY_DISCONNECTED = "Server terminated connection with client.";
	private static final Terminal CMD = new Terminal();

	public static void main(String[] args) {
		try {
			String host = CMD.requestString("Enter host address");
			new VCTalkServer(host);
		} catch (IOException e) {
			String msg = "Failed to construct server.";
			Log.error(msg, e);
			CMD.println(msg);
			CMD.stop();
		}
	}

	private VCTalkServer(String host) throws IOException {
		super(host, PORT, RequestProcessorJob.class);
		CMD.attach(this);
		CMD.addOrReplaceCommand(new TerminalCommand("list", this::listClients, "List the connected clients."));
		CMD.addOrReplaceCommand(new TerminalCommand("purge", this::disconnectAllClients, "Disconnect all clients."));
		start();
		Log.info("VCTalk Server started (" + host + ")");
	}

	@Override
	public void addClient(Client client) {
		super.addClient(client);
		Log.info(LOG_CLIENT_CONNECTED);
	}

	@Override
	public void onClientDisconnect(Client client) {
		super.onClientDisconnect(client);
		Log.info(LOG_CLIENT_DISCONNECTED);
	}

	@Override
	public void manuallyDisconnectClient(Client client) {
		super.manuallyDisconnectClient(client);
		Log.info(LOG_CLIENT_MANUALLY_DISCONNECTED);
	}

	@Override
	public void disconnectAllClients() {
		if (getClients().length == 0) CMD.println("No clients to disconnect.");
		else super.disconnectAllClients();
	}

	private void listClients() {
		CMD.println("Connected clients: " + getClients().length);
	}

	@Override
	public void stop() {
		Log.info("Server shutting down.");
		super.stop();
		try {
			Log.stop();
			Log.info("Server shut down successfully.");
		} catch (IOException e) {
			Log.error("Failed to shut down logger.", e);
		}
	}
}
