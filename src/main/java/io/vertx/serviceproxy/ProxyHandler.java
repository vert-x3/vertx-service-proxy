/*
 * Copyright 2014 Red Hat, Inc.
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

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.function.Function;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public abstract class ProxyHandler implements Handler<Message<JsonObject>> {

  protected boolean closed;
  protected MessageConsumer<JsonObject> consumer;

  public void close() {
    consumer.unregister();
    closed = true;
  }

  /**
   * Register the proxy handle on the event bus.
   *
   * @param eventBus the event bus
   * @param address the proxy address
   */
  public MessageConsumer<JsonObject> register(EventBus eventBus, String address) {
    return register(eventBus, address, null);
  }

  /**
   * Register the local proxy handle on the event bus.
   * The registration will not be propagated to other nodes in the cluster.
   *
   * @param eventBus the event bus
   * @param address the proxy address
   */
  public MessageConsumer<JsonObject> registerLocal(EventBus eventBus, String address) {
    return registerLocal(eventBus, address, null);
  }

  /**
   * Register the proxy handle on the event bus.
   *
   * @param eventBus the event bus
   * @param address the proxy address
   * @param interceptors the interceptors
   */
  public MessageConsumer<JsonObject> register(EventBus eventBus, String address, List<Function<Message<JsonObject>, Future<Message<JsonObject>>>> interceptors) {
    Handler<Message<JsonObject>> handler = configureHandler(interceptors);
    consumer = eventBus.consumer(address, handler);
    return consumer;
  }

  /**
   * Register the local proxy handle on the event bus.
   * The registration will not be propagated to other nodes in the cluster.
   *
   * @param eventBus the event bus
   * @param address the proxy address
   * @param interceptors the interceptors
   */
  public MessageConsumer<JsonObject> registerLocal(EventBus eventBus, String address, List<Function<Message<JsonObject>, Future<Message<JsonObject>>>> interceptors) {
    Handler<Message<JsonObject>> handler = configureHandler(interceptors);
    consumer = eventBus.localConsumer(address, handler);
    return consumer;
  }

  private Handler<Message<JsonObject>> configureHandler(List<Function<Message<JsonObject>, Future<Message<JsonObject>>>> interceptors) {
    Handler<Message<JsonObject>> handler = this;
    if (interceptors != null) {
      for (Function<Message<JsonObject>, Future<Message<JsonObject>>> interceptor : interceptors) {
        Handler<Message<JsonObject>> prev = handler;
        handler = msg -> {
          Future<Message<JsonObject>> fut = interceptor.apply(msg);
          fut.setHandler(ar -> {
            if (ar.succeeded()) {
              prev.handle(msg);
            } else {
              ReplyException exception = (ReplyException) ar.cause();
              msg.fail(exception.failureCode(), exception.getMessage());
            }
          });
        };
      }
    }
    return handler;
  }

}
