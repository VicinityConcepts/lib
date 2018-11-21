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

package com.vicinityconcepts.util;

/**
 * A service is an ongoing processes that performs the same task
 * repeatedly in a loop until explicitly instructed to stop. Services
 * can be started again after they have been stopped.
 *
 * @author Ryan Palmer
 */
public abstract class Service implements Procedure {
	/**
	 * Constants
	 */
	private static final String SERVICE_STARTED = "Service starting up: %s";
	private static final String SERVICE_STOPPED = "Service shutting down: %s";
	private static final String ERROR_INVALID_LOOP_RATE = "Specified loop rate (%d) is lower than the minimum required loop rate (%d).";
	private static final int DEFAULT_LOOP_RATE = 1000 / 10; // Default 10 loops per second
	private static final int MINIMUM_LOOP_RATE = 1000 / 60; // No more than 60 loops per second

	/**
	 * Used to execute the core logic of this class.
	 */
	private Thread thread;

	/**
	 * The name that will be assigned to this service's thread when started.
	 */
	private volatile String name;

	/**
	 * The amount of time, in milliseconds, that the thread will wait between loops.
	 */
	private volatile int loopRate = DEFAULT_LOOP_RATE;

	/**
	 * This flag controls the main execution loop.
	 */
	private volatile boolean running = false;

	/**
	 * This flag is set to false when the thread starts and remains false until
	 * the thread has finished completely.
	 */
	private volatile boolean finished = true;

	/**
	 * Construct a service.
	 */
	public Service() {
		name = this.getClass().getSimpleName();
	}

	/**
	 * Initiate execution of this service. Does nothing if this service is
	 * already running.
	 */
	@Override
	public void start() {
		if (!finished) return;
		finished = false;
		running = true;

		thread = new Thread(this::run0, name);
		thread.start();

		Log.info(String.format(SERVICE_STARTED, getName()));
	}

	/**
	 * Initiate shutdown of this service. The service will still finish the
	 * current iteration of the main execution loop. Does nothing if this
	 * service has already been stopped
	 */
	@Override
	public void stop() {
		if (!running) return;
		running = false;
		thread.interrupt();
		Log.info(String.format(SERVICE_STOPPED, getName()));
	}

	/**
	 * Restart this service, waiting for it to fully shut down and then starting
	 * it again. This is a blocking method that will join the service's thread
	 * until it completes.
	 *
	 * @throws InterruptedException
	 */
	public void restart() throws InterruptedException {
		stop();
		join();
		start();
	}

	/**
	 * Wait for this service to completely finish execution. Returns immediately
	 * if the service has not started yet.
	 *
	 * @throws InterruptedException
	 */
	public void join() throws InterruptedException {
		if (thread.isAlive()) thread.join();
	}

	/**
	 * Wait for this service to completely finish execution, but give up
	 * after a specified duration. Returns immediately if the service has
	 * not started yet.
	 *
	 * @param timeout Amount of time to wait for this service to finish
	 * @throws InterruptedException
	 */
	public void join(long timeout) throws InterruptedException {
		if (thread.isAlive()) thread.join(timeout);
	}

	/**
	 * Called after execution is started. This method drives the lifecycle
	 * of the service.
	 */
	private void run0() {
		while (running) {
			run();
			try {
				Thread.sleep(loopRate);
			} catch (InterruptedException ignore) {
				// We don't care about interrupts
			}
		}
		finished = true;
	}

	/**
	 * Called repeatedly in a loop until the service is stopped. The thread
	 * will sleep for the amount of time specified by the loop rate
	 * between each call to this method. However, this is merely to prevent
	 * loops that would otherwise run too fast. It would be very wise to
	 * allow the thread to suspend execution until it has meaningful work
	 * to do in your implementation of this method.
	 */
	protected abstract void run();

	/**
	 * Set the amount of time that the thread will wait between loops.
	 * Use extreme caution when setting this to a low value. Low loop
	 * rates can cause a significant resource burden, especially when
	 * a lot of services are running at the same time. Loop rates of
	 * more than 60 loops per second are not allowed, so this value
	 * cannot be less than 1000 / 60.
	 *
	 * @param rate Number of milliseconds to wait
	 * @throws IllegalArgumentException if the specified loop rate is
	 *                                  below the minimum requirement.
	 */
	protected void setLoopRate(int rate) throws IllegalArgumentException {
		if (rate < MINIMUM_LOOP_RATE)
			throw new IllegalArgumentException(String.format(ERROR_INVALID_LOOP_RATE, rate, MINIMUM_LOOP_RATE));
		else loopRate = rate;
	}

	/**
	 * Check if this service is running. Even if false, the service may
	 * still be in the process of executing its final iteration of the
	 * execution loop. To see if the service has finished completely,
	 * use isFinished().
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * Check if this thread has stopped running and finished all exit
	 * activities. Useful if you need to be sure cleanup is done.
	 */
	public boolean isFinished() {
		return finished;
	}

	/**
	 * Get the name of this service's thread.
	 */
	@Override
	public final String getName() {
		return name;
	}

	/**
	 * Set the name of this service's thread. Service must be restarted
	 * for new name to take effect.
	 */
	public final void setName(String name) {
		this.name = name;
	}
}
