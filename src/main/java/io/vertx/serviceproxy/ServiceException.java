/*
 * Copyright 2021 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */
package io.vertx.serviceproxy;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.json.JsonObject;

/**
 * An Exception to be returned from Service implementations.
 *
 * @author <a href="mailto:oreilldf@gmail.com">Dan O'Reilly</a>
 */
public class ServiceException extends ReplyException {

  private final JsonObject debugInfo;

  /**
   * Create a ServiceException.
   *
   * @param failureCode The failure code.
   * @param message The failure message.
   */
  public ServiceException(int failureCode, String message) {
    this(failureCode, message, new JsonObject());
  }

  public ServiceException(int failureCode, String message, JsonObject debugInfo) {
    super(ReplyFailure.RECIPIENT_FAILURE, failureCode, message);
    this.debugInfo = debugInfo;

  }

  /**
   * Get the Debugging information provided to this ServiceException
   *
   * @return The debug info.
   */
  public JsonObject getDebugInfo() {
    return debugInfo;
  }

  /**
   * Create a failed Future containing a ServiceException.
   *
   * @param failureCode The failure code.
   * @param message The failure message.
   * @param <T> The type of the AsyncResult.
   * @return A failed Future containing the ServiceException.
   */
  public static <T> Future<T> fail(int failureCode, String message) {
    return Future.failedFuture(new ServiceException(failureCode, message));
  }

  /**
   *
   * Create a failed Future containing a ServiceException.
   *
   * @param failureCode The failure code.
   * @param message The failure message.
   * @param debugInfo The debug info.
   * @param <T> The type of the AsyncResult.
   * @return A failed Future containing the ServiceException.
   */
  public static <T> Future<T> fail(int failureCode, String message, JsonObject debugInfo) {
    return Future.failedFuture(new ServiceException(failureCode, message, debugInfo));
  }

}
