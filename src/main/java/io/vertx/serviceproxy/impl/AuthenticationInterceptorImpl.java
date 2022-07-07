package io.vertx.serviceproxy.impl;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.impl.ContextInternal;
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

  AuthenticationProvider authenticationProvider;

  /**
   * Set an authentication authenticationProvider that will verify all requests before the service is invoked.
   *
   * @param authenticationProvider an authentication provider
   * @return self
   */
  @Override
  public AuthenticationInterceptorImpl setAuthenticationProvider(AuthenticationProvider authenticationProvider) {
    this.authenticationProvider = authenticationProvider;
    return this;
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
