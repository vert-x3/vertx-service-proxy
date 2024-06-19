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
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.auth.authentication.CredentialValidationException;
import io.vertx.ext.auth.authentication.TokenCredentials;
import io.vertx.serviceproxy.AuthenticationInterceptor;

import java.util.Map;

/**
 * Create an event bus service interceptor that will provide an authentication check
 */
public class AuthenticationInterceptorImpl implements AuthenticationInterceptor {

  //an authentication authenticationProvider that will verify all requests before the service is invoked.
  final AuthenticationProvider authenticationProvider;

  public AuthenticationInterceptorImpl(AuthenticationProvider authenticationProvider) {
    this.authenticationProvider = authenticationProvider;
  }

  @Override
  public Future<Message<JsonObject>> intercept(Vertx vertx, Map<String, Object> interceptorContext,
                                               Message<JsonObject> body) {
    final ContextInternal vertxContext = (ContextInternal) vertx.getOrCreateContext();
    final TokenCredentials tokenCredentials = new TokenCredentials(body.headers().get("auth-token"));
    try {
      tokenCredentials.checkValid(null);
      if (authenticationProvider == null) {
        return vertxContext.failedFuture(
          new ReplyException(ReplyFailure.RECIPIENT_FAILURE, 500, "No AuthenticationProvider present"));
      }
      return authenticationProvider.authenticate(tokenCredentials)
        .recover(err -> vertxContext.failedFuture(
          new ReplyException(ReplyFailure.RECIPIENT_FAILURE, 500, err.getMessage())))
        .compose(user -> {
          if (user == null) {
            return vertxContext.failedFuture(new ReplyException(ReplyFailure.RECIPIENT_FAILURE, 401, "User is null"));
          }
          //authentication succeeded
          interceptorContext.put("user", user);
          return vertxContext.succeededFuture(body);
        });
    } catch (CredentialValidationException e) {
      return Future.failedFuture(new ReplyException(ReplyFailure.RECIPIENT_FAILURE, 401, e.getMessage()));
    }
  }
}
