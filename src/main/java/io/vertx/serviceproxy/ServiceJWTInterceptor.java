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
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.jwt.JWTAuth;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Create an event bus service interceptor using a JWT auth that will verify all requests before the service is invoked
 * <p/>
 * Once a JWT is validated it will be queried for authorities. If authorities are missing a error 403 is returned.
 *
 * @deprecated Use {@link ServiceAuthInterceptor} instead
 */
@Deprecated
public class ServiceJWTInterceptor implements Function<Message<JsonObject>, Future<Message<JsonObject>>> {

  private JWTAuth jwtAuth;
  private Set<String> authorities;

  /**
   * Set a JWT auth that will verify all requests before the service is invoked.
   *
   * @param jwtAuth a JWT auth
   * @return self
   */
  public ServiceJWTInterceptor setJwtAuth(JWTAuth jwtAuth) {
    this.jwtAuth = jwtAuth;
    return this;
  }

  /**
   * Set the required authorities for the service, once a JWT is validated it will be
   * queried for these authorities. If authorities are missing a error 403 is returned.
   *
   * @param authorities set of authorities
   * @return self
   */
  public ServiceJWTInterceptor setAuthorities(Set<String> authorities) {
    this.authorities = authorities;
    return this;
  }

  /**
   * Add a single authority to the authorities set.
   *
   * @param authority authority
   * @return self
   */
  public ServiceJWTInterceptor addAuthority(String authority) {
    if (authorities == null) {
      authorities = new HashSet<>();
    }
    authorities.add(authority);
    return this;
  }

  /**
   * Clear the current set of authorities.
   * @return self
   */
  public ServiceJWTInterceptor clearAuthorities() {
    if (authorities != null) {
      authorities.clear();
    }
    return this;
  }

  @Override
  public Future<Message<JsonObject>> apply(Message<JsonObject> msg) {
    final String authorization = msg.headers().get("auth-token");

    if (authorization == null) {
      return Future.failedFuture(new ReplyException(ReplyFailure.RECIPIENT_FAILURE, 401, "Unauthorized"));
    }

    Promise<Message<JsonObject>> promise = Promise.promise();

    jwtAuth.authenticate(new JsonObject().put("jwt", authorization), authenticate -> {
      if (authenticate.failed()) {
        promise.fail(new ReplyException(ReplyFailure.RECIPIENT_FAILURE, 500, authenticate.cause().getMessage()));
        return;
      }

      final User user = authenticate.result();

      if (user == null) {
        promise.fail(new ReplyException(ReplyFailure.RECIPIENT_FAILURE, 403, "Forbidden"));
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
                promise.complete(msg);
              }
            } else {
              if (sentFailure.compareAndSet(false, true)) {
                promise.fail(new ReplyException(ReplyFailure.RECIPIENT_FAILURE, 403, "Forbidden"));
              }
            }
          } else {
            promise.fail(new ReplyException(ReplyFailure.RECIPIENT_FAILURE, 500, res.cause().getMessage()));
          }
        };
        for (String authority : authorities) {
          if (!sentFailure.get()) {
            user.isAuthorized(authority, authHandler);
          }
        }
      } else {
        // No auth required
        promise.complete(msg);
      }
    });

    return promise.future();
  }
}
