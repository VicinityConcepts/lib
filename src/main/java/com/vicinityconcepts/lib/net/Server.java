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

import com.vicinityconcepts.lib.util.Procedure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;

public class Server implements Procedure {
	protected static final Logger LOG = LogManager.getLogger();
	private static final String ERROR_SET_TIMEOUT = "Server encountered an error while setting connection timeout.";
	private static final int DEFAULT_CONNECTION_TIMEOUT = 1000;

	private final ServerSocket socket;
	private final Collection<Client> clients;
	private final ClientManager clientManager;
	private final RequestManager requestManager;

	public Server(ServerSocket socket, Class<? extends RequestProcessorJob> requestProcessor) {
		this.socket = socket;
		setConnectionTimeout(DEFAULT_CONNECTION_TIMEOUT);
		clients = new ArrayList<>();
		clientManager = new ClientManager(this);
		requestManager = new RequestManager(this, requestProcessor);
	}

	public Server(String host, int port, Class<? extends RequestProcessorJob> requestProcessor) throws IOException {
		this(new ServerSocket(port, 0, InetAddress.getByName(host)), requestProcessor);
	}

	@Override
	public void start() {
		clientManager.start();
		requestManager.start();
	}

	@Override
	public void stop() {
		requestManager.stop();
		clientManager.stop();
	}

	public Client accept() throws IOException {
		return new Client(socket.accept(), this, this::onClientDisconnect);
	}

	public void addClient(Client client) {
		synchronized (clients) {
			clients.add(client);
			client.start();
		}
	}

	public void manuallyDisconnectClient(Client client) {
		client.stop();
	}

	protected void onClientDisconnect(Client client) {
		synchronized (clients) {
			clients.remove(client);
		}
	}

	public Client[] getClients() {
		synchronized (clients) {
			Client[] clientArray = new Client[clients.size()];
			return clients.toArray(clientArray);
		}
	}

	public void disconnectAllClients() {
		synchronized (clients) {
			for (Client client : getClients()) manuallyDisconnectClient(client);
		}
	}

	public void setConnectionTimeout(int timeout) {
		try {
			socket.setSoTimeout(timeout);
		} catch (SocketException e) {
			LOG.error(ERROR_SET_TIMEOUT, e);
		}
	}
}
