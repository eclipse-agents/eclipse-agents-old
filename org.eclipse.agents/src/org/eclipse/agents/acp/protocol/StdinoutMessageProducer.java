/*******************************************************************************
 * Copyright (c) 2025 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.agents.acp.protocol;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.agents.internal.Tracer;
import org.eclipse.lsp4j.jsonrpc.JsonRpcException;
import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import org.eclipse.lsp4j.jsonrpc.MessageIssueException;
import org.eclipse.lsp4j.jsonrpc.MessageIssueHandler;
import org.eclipse.lsp4j.jsonrpc.MessageProducer;
import org.eclipse.lsp4j.jsonrpc.json.MessageConstants;
import org.eclipse.lsp4j.jsonrpc.json.MessageJsonHandler;
import org.eclipse.lsp4j.jsonrpc.json.StreamMessageProducer;
import org.eclipse.lsp4j.jsonrpc.messages.Message;

public class StdinoutMessageProducer implements MessageProducer, Closeable, MessageConstants {

	private static final Logger LOG = Logger.getLogger(StreamMessageProducer.class.getName());

	private final MessageJsonHandler jsonHandler;
	private final MessageIssueHandler issueHandler;

	private InputStream input;

	private MessageConsumer callback;
	private boolean keepRunning;

	public StdinoutMessageProducer(InputStream input, MessageJsonHandler jsonHandler) {
		this(input, jsonHandler, null);
	}

	public StdinoutMessageProducer(InputStream input, MessageJsonHandler jsonHandler, MessageIssueHandler issueHandler) {
		this.input = input;
		this.jsonHandler = jsonHandler;
		this.issueHandler = issueHandler;
	}

	public InputStream getInput() {
		return input;
	}

	public void setInput(InputStream input) {
		this.input = input;
	}

	protected static class Headers {
		public int contentLength = -1;
		public String charset = StandardCharsets.UTF_8.name();
	}

	@Override
	public void listen(MessageConsumer callback) {
		if (keepRunning) {
			throw new IllegalStateException("This StreamMessageProducer is already running.");
		}
		this.keepRunning = true;
		this.callback = callback;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
			StringBuilder debugBuilder = null;
			while (keepRunning) {
				String line = reader.readLine();
				if (line == null) {
					// End of input stream has been reached
					keepRunning = false;
				} else {
					if (debugBuilder == null)
						debugBuilder = new StringBuilder();
					debugBuilder.append(line);
					boolean result = handleMessage(line);
					if (!result) {
						keepRunning = false;
					}
				}
			} // while (keepRunning)
		} catch (IOException exception) {
			if (JsonRpcException.indicatesStreamClosed(exception)) {
				// Only log the error if we had intended to keep running
				if (keepRunning)
					fireStreamClosed(exception);
			} else
				throw new JsonRpcException(exception);
		} finally {
			this.callback = null;
			this.keepRunning = false;
		}
	}

	/**
	 * Log an error.
	 */
	protected void fireError(Throwable error) {
		String message = error.getMessage() != null ? error.getMessage() : "An error occurred while processing an incoming message.";
		LOG.log(Level.SEVERE, message, error);
	}

	/**
	 * Report that the stream was closed through an exception.
	 */
	protected void fireStreamClosed(Exception cause) {
		String message = cause.getMessage() != null ? cause.getMessage() : "The input stream was closed.";
		LOG.log(Level.INFO, message, cause);
	}

	/**
	 * Read the JSON content part of a message, parse it, and notify the callback.
	 *
	 * @return {@code true} if we should continue reading from the input stream, {@code false} if we should stop
	 */
	protected boolean handleMessage(String line) throws IOException {
		if (callback == null) {
			callback = message -> LOG.log(Level.INFO, "Received message: " + message);
		}

		try {
			Tracer.trace().trace(Tracer.ACP, line);
			
			Message message = jsonHandler.parseMessage(line);
			callback.consume(message);	
		} catch (MessageIssueException exception) {
			// An issue was found while parsing or validating the message
			if (issueHandler != null)
				issueHandler.handle(exception.getRpcMessage(), exception.getIssues());
			else
				fireError(exception);
		} catch (Exception exception) {
			// UnsupportedEncodingException can be thrown by String constructor
			// JsonParseException can be thrown by jsonHandler
			// We also catch arbitrary exceptions that are thrown by message consumers in order to keep this thread alive
			fireError(exception);
		}
		return true;
	}

	@Override
	public void close() {
		keepRunning = false;
	}

}