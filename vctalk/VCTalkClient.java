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
import java.net.UnknownHostException;

public class VCTalkClient extends Client implements VCTalkConstants {
	private static final String TALK_COMMAND = "t";
	private static final Terminal CMD = new Terminal();

	private final String username;

	public static void main(String[] args) {
		String host = null;
		try {
			host = CMD.requestString("Enter host address");
			new VCTalkClient(host);
		} catch (UnknownHostException e) {
			String msg = "Unknown host: " + host;
			CMD.println(msg);
			Log.error(msg);
		} catch (IOException e) {
			Log.error(e);
		}
	}

	private VCTalkClient(String host) throws IOException {
		super(host, PORT);
		Log.info("Successfully connected to host: " + host);

		String attemptedUsername;
		do attemptedUsername = CMD.requestString("Enter username");
		while (!isValidUsername(attemptedUsername));
		username = attemptedUsername;

		CMD.addOrReplaceCommand(new TerminalCommand(TALK_COMMAND, this::onTalk));
		CMD.attach(this);
		start();
		Log.info("Welcome, " + username + "!");
	}

	@Override
	protected void run() {
		super.run();
		if (hasData()) CMD.println(receiveMessage());
	}

	@Override
	public void stop() {
		Log.info("Client shutting down.");
		super.stop();
	}

	private boolean isValidUsername(String username) {
		if (username == null || username.length() == 0) return false;
		send("<username>" + username + "</username>");
		return true;
	}

	private void onTalk() {
		String message = CMD.requestString("Enter your message");
		sendMessage(message);
	}

	private void sendMessage(String message) {
		send("{\"type\":\"SEND\", \"payload\":\"[]\"}");
	}

	private String receiveMessage() {
		return (String) receive();
	}
}