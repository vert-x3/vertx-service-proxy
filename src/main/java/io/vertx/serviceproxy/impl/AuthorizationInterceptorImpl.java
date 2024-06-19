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
package io.vertx.serviceproxy.impl;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.internal.ContextInternal;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.user.User;
import io.vertx.ext.auth.authorization.Authorization;
import io.vertx.ext.auth.authorization.AuthorizationContext;
import io.vertx.ext.auth.authorization.AuthorizationProvider;
import io.vertx.serviceproxy.AuthorizationInterceptor;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Create an event bus service interceptor that will provide an authorization check
 */
public class AuthorizationInterceptorImpl implements AuthorizationInterceptor {
  private final AuthorizationProvider authorizationProvider;

  private Set<Authorization> authorizations;

  public AuthorizationInterceptorImpl(AuthorizationProvider authorizationProvider) {
    this.authorizationProvider = authorizationProvider;
  }

  /**
   * Set the required authorities for the service, once a JWT is validated it will be
   * queried for these authorities. If authorities are missing a error 403 is returned.
   *
   * @param authorizations set of authorities
   * @return self
   */
  @Override
  public AuthorizationInterceptorImpl setAuthorizations(Set<Authorization> authorizations) {
    this.authorizations = authorizations;
    return this;
  }

  /**
   * Add a single authority to the authorities set.
   *
   * @param authorization authority
   * @return self
   */
  @Override
  public AuthorizationInterceptorImpl addAuthorization(Authorization authorization) {
    if (authorizations == null) {
      authorizations = new HashSet<>();
    }
    authorizations.add(authorization);
    return this;
  }

  @Override
  public Future<Message<JsonObject>> intercept(Vertx vertx, Map<String, Object> interceptorContext,
                                               Message<JsonObject> body) {
    final ContextInternal vertxContext = (ContextInternal) vertx.getOrCreateContext();
    //for some reasons user hasn't been added neither by authn interceptor nor somehow else
    if (!interceptorContext.containsKey("user")) {
      return vertxContext.succeededFuture(body);
    }
    if (authorizations == null || authorizations.isEmpty()) {
      return vertxContext.succeededFuture(body);
    }
    User user = (User) interceptorContext.get("user");
    return authorizationProvider.getAuthorizations(user)
      .recover(err -> vertxContext.failedFuture(
        new ReplyException(ReplyFailure.RECIPIENT_FAILURE, 500, err.getMessage())))
      .compose(voidResult -> {
        AuthorizationContext userAuthorizationContext = AuthorizationContext.create(user);
        for (Authorization authorization : authorizations) {
          // authorization failed
          if (!authorization.match(userAuthorizationContext)) {
            return vertxContext.failedFuture(new ReplyException(ReplyFailure.RECIPIENT_FAILURE, 403, "Forbidden"));
          }
        }
        // all authorities have passed
        return vertxContext.succeededFuture(body);
      });
  }
}
