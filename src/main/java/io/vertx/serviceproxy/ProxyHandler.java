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

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.impl.InterceptorHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
   * @param address  the proxy address
   */
  public MessageConsumer<JsonObject> register(EventBus eventBus, String address) {
    consumer = eventBus.consumer(address, this);
    return consumer;
  }

  /**
   * Register the proxy handle on the event bus.
   *
   * @param vertx              the VertX instance
   * @param address            the proxy address
   * @param interceptorHolders the interceptorHolders
   */
  public MessageConsumer<JsonObject> register(Vertx vertx, String address,
                                              List<InterceptorHolder> interceptorHolders) {
    Objects.requireNonNull(interceptorHolders);
    Handler<Message<JsonObject>> handler = configureHandler(vertx, interceptorHolders);
    consumer = vertx.eventBus().consumer(address, handler);
    return consumer;
  }

  /**
   * Register the local proxy handle on the event bus.
   * The registration will not be propagated to other nodes in the cluster.
   *
   * @param eventBus the event bus
   * @param address  the proxy address
   */
  public MessageConsumer<JsonObject> registerLocal(EventBus eventBus, String address) {
    consumer = eventBus.localConsumer(address, this);
    return consumer;
  }

  /**
   * Register the local proxy handle on the event bus.
   * The registration will not be propagated to other nodes in the cluster.
   *
   * @param vertx              the VertX instance
   * @param address            the proxy address
   * @param interceptorHolders the {@link InterceptorHolder} interceptorHolders
   */
  public MessageConsumer<JsonObject> registerLocal(Vertx vertx, String address,
                                                   List<InterceptorHolder> interceptorHolders) {
    Objects.requireNonNull(interceptorHolders);
    Handler<Message<JsonObject>> handler = configureHandler(vertx, interceptorHolders);
    consumer = vertx.eventBus().localConsumer(address, handler);
    return consumer;
  }

  private Handler<Message<JsonObject>> configureHandler(Vertx vertx, List<InterceptorHolder> interceptorHolders) {
    Handler<Message<JsonObject>> handler = this;
    Map<String, Object> context = new HashMap<>();
    // construct the handler backwards, this allows the checks to be performed in the correct order
    for (int i = interceptorHolders.size() - 1; i >= 0; i--) {
      final InterceptorHolder interceptorHolder = interceptorHolders.get(i);
      Handler<Message<JsonObject>> prev = handler;
      handler = msg -> {
        String action = msg.headers().get("action");
        String holderAction = interceptorHolder.action();
        if (holderAction == null || action.equals(holderAction)) {
          interceptorHolder.interceptor().intercept(vertx, context, msg)
            .onSuccess(prev)
            .onFailure(err -> {
              ReplyException exception = (ReplyException) err;
              msg.fail(exception.failureCode(), exception.getMessage());
            });
        } else {
          prev.handle(msg);
        }
      };
    }
    return handler;
  }

}
