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

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.auth.authentication.CredentialValidationException;
import io.vertx.ext.auth.authentication.TokenCredentials;
import io.vertx.ext.auth.authorization.Authorization;
import io.vertx.ext.auth.authorization.AuthorizationContext;
import io.vertx.ext.auth.authorization.AuthorizationProvider;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * Create an event bus service interceptor using a token based authentication provider (e.g.: JWT or Oauth2) that will
 * verify all requests before the service is invoked.
 *
 * @deprecated use {@link AuthenticationInterceptor}, {@link AuthorizationInterceptor} instead
 */
@Deprecated
public class ServiceAuthInterceptor implements Function<Message<JsonObject>, Future<Message<JsonObject>>> {

  private AuthenticationProvider authn;
  private AuthorizationProvider authz;

  private Set<Authorization> authorizations;

  /**
   * Set an authentication provider that will verify all requests before the service is invoked.
   *
   * @param provider an authentication provider
   * @return self
   */
  public ServiceAuthInterceptor setAuthenticationProvider(AuthenticationProvider provider) {
    this.authn = provider;
    return this;
  }

  public ServiceAuthInterceptor setAuthorizationProvider(AuthorizationProvider provider) {
    this.authz = provider;
    return this;
  }

  /**
   * Set the required authorities for the service, once a JWT is validated it will be
   * queried for these authorities. If authorities are missing a error 403 is returned.
   *
   * @param authorizations set of authorities
   * @return self
   */
  public ServiceAuthInterceptor setAuthorizations(Set<Authorization> authorizations) {
    this.authorizations = authorizations;
    return this;
  }

  /**
   * Add a single authority to the authorities set.
   *
   * @param authorization authority
   * @return self
   */
  public ServiceAuthInterceptor addAuthorization(Authorization authorization) {
    if (authorizations == null) {
      authorizations = new HashSet<>();
    }
    authorizations.add(authorization);
    return this;
  }

  @Override
  public Future<Message<JsonObject>> apply(Message<JsonObject> msg) {

    final TokenCredentials authorization = new TokenCredentials(msg.headers().get("auth-token"));

    try {
      authorization.checkValid(null);

      Promise<Message<JsonObject>> promise = Promise.promise();

      if (authn == null) {
        promise.fail(new ReplyException(ReplyFailure.RECIPIENT_FAILURE, 500, "No AuthenticationProvider present"));
        return promise.future();
      }

      authn.authenticate(authorization).onComplete(authenticate -> {
        if (authenticate.failed()) {
          promise.fail(new ReplyException(ReplyFailure.RECIPIENT_FAILURE, 500, authenticate.cause().getMessage()));
          return;
        }

        final User user = authenticate.result();

        if (user == null) {
          promise.fail(new ReplyException(ReplyFailure.RECIPIENT_FAILURE, 401, "Unauthorized"));
          return;
        }

        if (authorizations == null || authorizations.isEmpty()) {
          promise.complete(msg);
          return;
        }

        authz.getAuthorizations(user).onComplete(getAuthorizations -> {
          if (getAuthorizations.failed()) {
            promise.fail(new ReplyException(ReplyFailure.RECIPIENT_FAILURE, 500, authenticate.cause().getMessage()));
          } else {
            AuthorizationContext context = AuthorizationContext.create(user);
            for (Authorization authority : authorizations) {
              if (!authority.match(context)) {
                // failed
                promise.fail(new ReplyException(ReplyFailure.RECIPIENT_FAILURE, 403, "Forbidden"));
                return;
              }
            }
            // all authorities have passed
            promise.complete(msg);
          }
        });
      });

      return promise.future();
    } catch (CredentialValidationException e) {
      return Future.failedFuture(new ReplyException(ReplyFailure.RECIPIENT_FAILURE, 401, "Unauthorized"));
    }
  }
}
