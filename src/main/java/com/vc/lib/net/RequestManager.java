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

package com.vc.lib.net;

import com.vc.lib.util.Job;
import com.vc.lib.util.Service;
import com.vc.lib.util.WorkerPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;

public class RequestManager extends Service {
	private static final Logger LOG = LogManager.getLogger();
	private static final int WORKERS = 8;
	private static final String ERROR_CREATE_JOB = "Encountered an error while generating request processor job.";

	private final WorkerPool workers;
	private final Server server;
	private final Class<? extends RequestProcessorJob> requestProcessor;

	public RequestManager(Server server, Class<? extends RequestProcessorJob> requestProcessor) {
		workers = new WorkerPool(WORKERS);
		this.server = server;
		this.requestProcessor = requestProcessor;
	}

	@Override
	public boolean start() {
		super.start();
		return workers.start();
	}

	@Override
	public boolean stop() {
		super.stop();
		return workers.stop();
	}

	@Override
	protected void run() {
		if (hasIncomingRequests()) processRequests();
		else {
			synchronized (server) {
				try {
					server.wait();
				} catch (InterruptedException ignore) {
				}
			}
		}
	}

	private boolean hasIncomingRequests() {
		for (Client client : server.getClients()) if (client.hasData()) return true;
		return false;
	}

	private void processRequests() {
		for (Client client : server.getClients()) {
			if (client.hasData()) {
				Job processorJob = getProcessorInstance(client, (String) client.receive());
				if (processorJob != null) workers.assign(processorJob);
			}
		}
	}

	private RequestProcessorJob getProcessorInstance(Client sender, String request) {
		try {
			return requestProcessor.getDeclaredConstructor(Client.class, String.class).newInstance(sender, request);
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
			LOG.error(ERROR_CREATE_JOB, e);
			return null;
		}
	}
}
