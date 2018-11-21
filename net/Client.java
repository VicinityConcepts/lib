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

package com.vicinityconcepts.net;

import com.vicinityconcepts.util.Log;
import com.vicinityconcepts.util.Service;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class Client extends Service {
	private static final String ERROR_SEND = "Encountered an error while sending data.";
	private static final String ERROR_RECEIVE = "Encountered an error while receiving data.";
	private static final String ERROR_RECEIVE_CLASS = "Received an unrecognized object type.";
	private static final String ERROR_CLOSE = "Encountered an error while closing.";
	private static final String ERROR_QUEUE_CAPACITY = "Failed to queue received object.";
	private static final String WARN_QUEUE_EMPTY = "Tried to remove object but queue was empty.";
	private static final String WARN_INPUT_RESET_FAILED = "Could not recover input stream.";
	private static final String WARN_RESTART_INTERRUPTED = "Restart was interrupted and may not have completed properly.";
	private static final String INFO_INPUT_RESET_SUCCESS = "Input stream recovered.";

	private final Socket socket;
	private final Object listener;
	private final Consumer<Client> onDisconnect;
	private final ObjectInputStream input;
	private final ObjectOutputStream output;
	private final Queue<Object> queue;

	public Client(String host, int port) throws IOException {
		this(new Socket(InetAddress.getByName(host), port), null, null);
	}

	public Client(Socket socket, Object listener, Consumer<Client> onDisconnect) throws IOException {
		this.socket = socket;
		if (listener != null) this.listener = listener;
		else this.listener = this;
		this.onDisconnect = onDisconnect;
		output = new ObjectOutputStream(socket.getOutputStream());
		input = new ObjectInputStream(socket.getInputStream());
		queue = new ConcurrentLinkedQueue<>();
	}

	@Override
	protected void run() {
		try {
			queue.add(input.readObject());
			synchronized (listener) {
				listener.notifyAll();
			}
		} catch (SocketException e) {
			stop();
		} catch (EOFException e) {
			// If we reached the end of the stream, try
			// to reset it. If not, disconnect the client.
			if (!attemptInputReset()) stop();
		} catch (IllegalStateException e) {
			Log.error(ERROR_QUEUE_CAPACITY, e);
		} catch (ClassNotFoundException e) {
			Log.error(ERROR_RECEIVE_CLASS, e);
		} catch (IOException e) {
			Log.error(ERROR_RECEIVE, e);
		}
	}

	@Override
	public void stop() {
		try {
			super.stop();
			if (onDisconnect != null) onDisconnect.accept(this);
			input.close();
			output.close();
			socket.close();
		} catch (IOException e) {
			Log.error(ERROR_CLOSE, e);
		}
	}

	private boolean attemptInputReset() {
		try {
			input.reset();
			Log.info(INFO_INPUT_RESET_SUCCESS);
			return true;
		} catch (IOException e) {
			Log.warn(WARN_INPUT_RESET_FAILED);
			return false;
		}
	}

	public boolean hasData() {
		return !queue.isEmpty();
	}

	public void send(Object data) {
		try {
			output.writeObject(data);
			output.flush();
		} catch (IOException e) {
			Log.error(ERROR_SEND, e);
		}
	}

	public Object receive() {
		try {
			return queue.remove();
		} catch (NoSuchElementException e) {
			Log.warn(WARN_QUEUE_EMPTY);
			return null;
		}
	}
}
