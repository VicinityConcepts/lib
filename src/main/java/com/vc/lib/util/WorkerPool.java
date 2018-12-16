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
 * Manages a specified number of workers, ensuring balanced job distribution.
 *
 * @author Ryan Palmer
 */
public class WorkerPool implements Procedure {
	private static final int MINIMUM_SIZE = 2;
	private static final String WORKER_NAME_FORMAT = "Worker-%d";
	private static final String POOL_TOO_SMALL = "Specified worker pool size (%d) does not meet minimum size requirement (%d).";

	/**
	 * Fixed array of workers for distributing jobs.
	 */
	private final Worker[] workers;

	/**
	 * Construct a new worker pool of the specified size.
	 *
	 * @param size Number of workers in this pool.
	 */
	public WorkerPool(int size) {
		if (size < MINIMUM_SIZE) throw new IllegalArgumentException(String.format(POOL_TOO_SMALL, size, MINIMUM_SIZE));
		workers = new Worker[size];
		for (int i = 0; i < size; i++) {
			Worker worker = new Worker();
			worker.setName(String.format(WORKER_NAME_FORMAT, i));
			workers[i] = worker;
		}
	}

	/**
	 * Start all workers in this pool.
	 */
	@Override
	public void start() {
		for (Worker w : workers) w.start();
	}

	/**
	 * Stop all workers in this pool.
	 */
	@Override
	public void stop() {
		for (Worker w : workers) w.stop();
	}

	/**
	 * Kill all workers in this pool, stopping any active jobs.
	 */
	public void kill() {
		for (Worker w : workers) w.kill();
	}

	/**
	 * @return true if any workers in this pool are currently running.
	 */
	public boolean isRunning() {
		for (Worker w : workers) if (w.isRunning()) return true;
		return false;
	}

	/**
	 * @return true if all workers in this pool are finished.
	 */
	public boolean isFinished() {
		for (Worker w : workers) if (!w.isFinished()) return false;
		return true;
	}

	/**
	 * Assign a job to the most suitable worker.
	 *
	 * @param job The job to assign
	 */
	public void assign(Job job) {
		Worker worker = findSuitableWorker();
		worker.assign(job);
	}

	/**
	 * Find the worker with the lowest number of jobs in its queue.
	 *
	 * @return the worker with the fewest queued jobs.
	 */
	private Worker findSuitableWorker() {
		Worker suitable = workers[0];

		for (int i = 1; i < workers.length; i++) {
			Worker current = workers[i];
			if (current.getQueueSize() < suitable.getQueueSize()) {
				suitable = current;
			}
		}

		return suitable;
	}
}
