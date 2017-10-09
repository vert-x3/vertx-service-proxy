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

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.jwt.JWTAuth;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public abstract class ProxyHandler implements Handler<Message<JsonObject>> {

  protected boolean closed;

  protected MessageConsumer<JsonObject> consumer;
  private JWTAuth authProvider;
  private Set<String> authorities;

  public ProxyHandler setJWTAuth(JWTAuth authProvider) {
    this.authProvider = authProvider;
    return this;
  }

  public ProxyHandler setAuthorities(Set<String> authorities) {
    this.authorities = authorities;
    return this;
  }

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
    consumer = eventBus.consumer(address, msg -> {
      if (authProvider == null) {
        handle(msg);
        return;
      }

      final String authorization = msg.headers().get("auth-token");

      if (authorization == null) {
        msg.fail(401, "Unauthorized");
        return;
      }

      authProvider.authenticate(new JsonObject().put("jwt", authorization), authenticate -> {
        if (authenticate.failed()) {
          msg.fail(500, authenticate.cause().getMessage());
          return;
        }

        final User user = authenticate.result();

        if (user == null) {
          msg.fail(403, "Forbidden");
          return;
        }

        final int requiredcount = authorities == null ? 0 : authorities.size();

        if (requiredcount > 0) {

          AtomicInteger count = new AtomicInteger();
          AtomicBoolean sentFailure = new AtomicBoolean();

          Handler<AsyncResult<Boolean>> authHandler = res -> {
            if (res.succeeded()) {
              if (res.result()) {
                if (count.incrementAndGet() == requiredcount) {
                  // Has all required authorities
                  handle(msg);
                }
              } else {
                if (sentFailure.compareAndSet(false, true)) {
                  msg.fail(403, "Forbidden");
                }
              }
            } else {
              msg.fail(500, res.cause().getMessage());
            }
          };
          for (String authority : authorities) {
            if (!sentFailure.get()) {
              user.isAuthorised(authority, authHandler);
            }
          }
        } else {
          // No auth required
          handle(msg);
        }
      });
    });

    return consumer;
  }
}
