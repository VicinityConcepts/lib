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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A service is an ongoing processes that performs the same task
 * repeatedly in a loop until explicitly instructed to stop. Services
 * can be started again after they have been stopped.
 *
 * @author Ryan Palmer
 */
public abstract class Service implements Procedure {
	private static final Logger LOG = LogManager.getLogger();
	private static final String SERVICE_STARTED = "Service starting up: %s";
	private static final String SERVICE_STOPPED = "Service shutting down: %s";
	private static final String ERROR_INVALID_LOOP_RATE = "Specified loop rate (%d) is lower than the minimum required loop rate (%d).";
	private static final String THREAD_NAME_FORMAT = "%s-service";
	private static final String ANONYMOUS_SERVICE_NAME = "anonymous";
	private static final String SHUTDOWN_HOOK_THREAD_NAME = "service-shutdown-hook";
	private static final String SHUTDOWN_HOOK_ENABLED = "JVM shutdown hook enabled.";
	private static final String SHUTDOWN_HOOK_DISABLED = "JVM shutdown hook disabled. This usually means another process will manage this service's lifecycle.";
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
	 * This flag indicates whether or not the JVM shutdown hook is enabled.
	 */
	private volatile boolean shutdownHookEnabled = true;

	/**
	 * The shutdown hook thread that will be run at JVM shutdown.
	 */
	private final Thread shutdownHook = new Thread(this::stop, SHUTDOWN_HOOK_THREAD_NAME);

	/**
	 * Construct a service.
	 */
	public Service() {
		name = this.getClass().getSimpleName();
		if (name.isEmpty()) name = ANONYMOUS_SERVICE_NAME;
		Runtime.getRuntime().addShutdownHook(shutdownHook);
	}

	/**
	 * Initiate execution of this service. Does nothing if this service is
	 * already running.
	 */
	@Override
	public boolean start() {
		if (!finished) return false;
		finished = false;
		running = true;

		thread = new Thread(this::run0, String.format(THREAD_NAME_FORMAT, name.toLowerCase()));
		thread.start();

		LOG.info(String.format(SERVICE_STARTED, getName()));
		return true;
	}

	/**
	 * Initiate shutdown of this service.
	 *
	 * <p>
	 * The service will still finish the
	 * current iteration of the main execution loop. Does nothing if this
	 * service has already been stopped
	 * </p>
	 */
	@Override
	public boolean stop() {
		if (!running) return false;
		running = false;
		thread.interrupt();
		LOG.info(String.format(SERVICE_STOPPED, getName()));
		return true;
	}

	/**
	 * Restart this service, waiting for it to fully shut down and then starting it again.
	 *
	 * <p>
	 * This is a blocking method that will join the service's thread until it completes.
	 * </p>
	 *
	 * @throws InterruptedException if the thread is interrupted while restarting.
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
	 * @throws InterruptedException if the thread is interrupted while joined.
	 */
	public void join() throws InterruptedException {
		if (thread.isAlive()) thread.join();
	}

	/**
	 * Wait for this service to completely finish execution, but give up
	 * after a specified duration. Returns immediately if the service has
	 * not started yet.
	 *
	 * @param timeout Amount of time to wait for this service to finish.
	 * @throws InterruptedException if the thread is interrupted while joined.
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
	 * Called repeatedly in a loop until the service is stopped.
	 *
	 * <p>
	 * The thread will sleep for the amount of time specified by the loop rate
	 * between each call to this method. However, this is merely to prevent
	 * loops that would otherwise run too fast. It would be very wise to
	 * allow the thread to suspend execution until it has meaningful work
	 * to do in your implementation of this method.
	 * </p>
	 */
	protected abstract void run();

	/**
	 * Set the amount of time that the thread will wait between loops.
	 *
	 * <p>
	 * Use extreme caution when setting this to a low value. Low loop
	 * rates can cause a significant resource burden, especially when
	 * a lot of services are running at the same time. Loop rates of
	 * more than 60 loops per second are not allowed, so this value
	 * cannot be less than 1000 / 60.
	 * </p>
	 *
	 * @param rate Number of milliseconds to wait.
	 * @throws IllegalArgumentException if the specified loop rate is
	 *                                  below the minimum requirement.
	 */
	protected void setLoopRate(int rate) throws IllegalArgumentException {
		if (rate < MINIMUM_LOOP_RATE)
			throw new IllegalArgumentException(String.format(ERROR_INVALID_LOOP_RATE, rate, MINIMUM_LOOP_RATE));
		else loopRate = rate;
	}

	/**
	 * Check if this service is running.
	 *
	 * <p>
	 * Even if false, the service may still be in the process of executing
	 * its final iteration of the execution loop. To see if the service has
	 * finished completely, use isFinished().
	 * </p>
	 *
	 * @return true if the service has not finished running yet.
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * Check if this thread has stopped running and finished all exit activities.
	 *
	 * <p>
	 * Useful if you need to be sure cleanup is done.
	 * </p>
	 *
	 * @return true if the service has completely finished running.
	 */
	public boolean isFinished() {
		return finished;
	}

	/**
	 * @return the name of this service's thread.
	 */
	@Override
	public final String getName() {
		return name;
	}

	/**
	 * Set the name of this service's thread.
	 *
	 * <p>
	 * Service must be restarted for new name to take effect.
	 * </p>
	 */
	public final void setName(String name) {
		this.name = name;
	}

	/**
	 * Enable or disable the JVM shutdown hook.
	 *
	 * <p>Does nothing if hook is already enabled.</p>
	 */
	public final void setShutdownHookEnabled(boolean enabled) {
		if (shutdownHookEnabled == enabled) return;
		shutdownHookEnabled = enabled;
		if (shutdownHookEnabled) {
			Runtime.getRuntime().addShutdownHook(shutdownHook);
			LOG.info(SHUTDOWN_HOOK_ENABLED);
		} else {
			Runtime.getRuntime().removeShutdownHook(shutdownHook);
			LOG.info(SHUTDOWN_HOOK_DISABLED);
		}
	}
}
