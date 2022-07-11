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

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import java.util.Map;

@VertxGen
@FunctionalInterface
public interface ServiceInterceptor {

  /**
   * Perform the interceptor handling
   *
   * @param vertx              the VertX instance
   * @param interceptorContext context to be shared between interceptors
   * @param body               message body
   * @return {@link Future}
   */
  Future<Message<JsonObject>> intercept(Vertx vertx, Map<String, Object> interceptorContext, Message<JsonObject> body);
}
