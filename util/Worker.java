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

package com.vicinityconcepts.lib.util;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A worker is a service that picks up and executes jobs using
 * a queue.
 *
 * @author Ryan Palmer
 */
public class Worker extends Service {
	/**
	 * Constants
	 */
	private static final String STARTED_JOB = "%s started: %s";
	private static final String FINISHED_JOB = "%s finished: %s";
	private static final String KILLED_JOB = "%s killed: %s";

	/**
	 * Jobs will be picked up from this queue for execution.
	 */
	private final ConcurrentLinkedQueue<Job> queue;

	/**
	 * The current job that has been picked up from the queue.
	 */
	private Job current;

	/**
	 * Construct a worker with an identifying name.
	 */
	public Worker() {
		queue = new ConcurrentLinkedQueue<>();
	}

	/**
	 * This logic runs repeatedly until the worker is stopped.
	 */
	@Override
	protected void run() {
		if (pickUpNewJob()) {
			Log.info(String.format(STARTED_JOB, getName(), current.getName()));
			current.run();
			Log.info(String.format(FINISHED_JOB, getName(), current.getName()));
			clearCurrentJob();
		} else waitForQueueActivity();
	}

	/**
	 * Shut down this worker, stopping the current job.
	 */
	public void kill() {
		super.stop();
		killCurrentJob();
	}

	/**
	 * Adds a job to this worker's queue. Queued jobs are picked up and executed
	 * in the order in which they were received.
	 *
	 * @param job The job to add to the queue
	 */
	public void assign(Job job) {
		synchronized (queue) {
			queue.offer(job);
			queue.notifyAll();
		}
	}

	/**
	 * Take the next job out of the queue. Synchronized to ensure thread safety
	 * of the current job object.
	 *
	 * @return true if a job was picked up, false if not
	 */
	private synchronized boolean pickUpNewJob() {
		current = queue.poll();
		return current != null;
	}

	/**
	 * Clears the current job. Synchronized to ensure thread safety
	 * of the current job object.
	 */
	private synchronized void clearCurrentJob() {
		current = null;
	}

	/**
	 * Kill the current job if one exists. Synchronized to ensure thread safety
	 * of the current job object.
	 */
	private synchronized void killCurrentJob() {
		if (current != null) {
			current.kill();
			Log.info(String.format(KILLED_JOB, getName(), current.getName()));
		}
	}

	/**
	 * Suspend thread execution until a new job has been queued.
	 */
	private void waitForQueueActivity() {
		synchronized (queue) {
			try {
				queue.wait();
			} catch (InterruptedException ignore) {
				// Interruption will occur when the worker is stopped.
			}
		}
	}

	/**
	 * Get the number of jobs in this worker's queue.
	 */
	public int getQueueSize() {
		return queue.size();
	}
}
