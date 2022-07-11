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
import io.vertx.ext.auth.authorization.Authorization;
import io.vertx.ext.auth.authorization.AuthorizationProvider;
import io.vertx.serviceproxy.impl.AuthorizationInterceptorImpl;

import java.util.Set;

/**
 * Create an event bus service interceptor that will provide an authorization check
 */
@VertxGen
public interface AuthorizationInterceptor extends ServiceInterceptor {

  static AuthorizationInterceptor create(AuthorizationProvider authorizationProvider) {
    return new AuthorizationInterceptorImpl(authorizationProvider);
  }

  /**
   * Set the required authorities for the service, once a JWT is validated it will be
   * queried for these authorities. If authorities are missing a error 403 is returned.
   *
   * @param authorizations set of authorities
   * @return self
   */
  AuthorizationInterceptor setAuthorizations(Set<Authorization> authorizations);

  /**
   * Add a single authority to the authorities set.
   *
   * @param authorization authority
   * @return self
   */
  AuthorizationInterceptor addAuthorization(Authorization authorization);
}
