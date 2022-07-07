package io.vertx.serviceproxy;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.serviceproxy.impl.AuthenticationInterceptorImpl;

/**
 * Create an event bus service interceptor that will provide an authentication check
 */
@VertxGen
public interface AuthenticationInterceptor extends ServiceInterceptor {

  static AuthenticationInterceptor create(AuthenticationProvider authenticationProvider) {
    return new AuthenticationInterceptorImpl(authenticationProvider);
  }
}
