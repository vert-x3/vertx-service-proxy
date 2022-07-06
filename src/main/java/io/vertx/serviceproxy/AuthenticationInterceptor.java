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

import java.util.Map;

/**
 * Create an event bus service interceptor that will provide an authentication check
 */
public class AuthenticationInterceptor implements ServiceInterceptor {

  private AuthenticationProvider authenticationProvider;

  /**
   * Set an authentication authenticationProvider that will verify all requests before the service is invoked.
   *
   * @param authenticationProvider an authentication provider
   * @return self
   */
  public AuthenticationInterceptor setAuthenticationProvider(AuthenticationProvider authenticationProvider) {
    this.authenticationProvider = authenticationProvider;
    return this;
  }

  @Override
  public Future<Message<JsonObject>> intercept(Map<String, Object> context, Message<JsonObject> msg) {
    final TokenCredentials tokenCredentials = new TokenCredentials(msg.headers().get("auth-token"));
    try {
      tokenCredentials.checkValid(null);
      Promise<Message<JsonObject>> promise = Promise.promise();
      if (authenticationProvider == null) {
        promise.fail(new ReplyException(ReplyFailure.RECIPIENT_FAILURE, 500, "No AuthenticationProvider present"));
        return promise.future();
      }
      authenticationProvider.authenticate(tokenCredentials, authenticationAsyncResult -> {
        if (authenticationAsyncResult.failed()) {
          promise.fail(
            new ReplyException(ReplyFailure.RECIPIENT_FAILURE, 500, authenticationAsyncResult.cause().getMessage()));
          return;
        }
        final User user = authenticationAsyncResult.result();
        if (user == null) {
          promise.fail(new ReplyException(ReplyFailure.RECIPIENT_FAILURE, 401, "User is null"));
          return;
        }
        context.put("user", user);
        promise.complete(msg);
      });
      return promise.future();
    } catch (CredentialValidationException e) {
      return Future.failedFuture(new ReplyException(ReplyFailure.RECIPIENT_FAILURE, 401, e.getMessage()));
    }
  }
}
