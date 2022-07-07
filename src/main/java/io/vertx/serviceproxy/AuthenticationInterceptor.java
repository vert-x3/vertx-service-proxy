package io.vertx.serviceproxy;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.auth.authentication.AuthenticationProvider;

/**
 * Create an event bus service interceptor that will provide an authentication check
 */
@VertxGen
public interface AuthenticationInterceptor extends ServiceInterceptor {

  /**
   * Set an authentication authenticationProvider that will verify all requests before the service is invoked.
   *
   * @param authenticationProvider an authentication provider
   * @return self
   */
  AuthenticationInterceptor setAuthenticationProvider(AuthenticationProvider authenticationProvider);
}
