package io.vertx.serviceproxy;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.CredentialValidationException;
import io.vertx.ext.auth.authorization.Authorization;
import io.vertx.ext.auth.authorization.AuthorizationContext;
import io.vertx.ext.auth.authorization.AuthorizationProvider;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Create an event bus service interceptor using a token based authentication provider (e.g.: JWT or Oauth2) that will
 * verify all requests before the service is invoked.
 */
public class AuthorizationInterceptor implements ServiceInterceptor {
  private AuthorizationProvider authorizationProvider;

  private Set<Authorization> authorizations;

  public AuthorizationInterceptor setAuthorizationProvider(AuthorizationProvider authorizationProvider) {
    this.authorizationProvider = authorizationProvider;
    return this;
  }

  /**
   * Set the required authorities for the service, once a JWT is validated it will be
   * queried for these authorities. If authorities are missing a error 403 is returned.
   *
   * @param authorizations set of authorities
   * @return self
   */
  public AuthorizationInterceptor setAuthorizations(Set<Authorization> authorizations) {
    this.authorizations = authorizations;
    return this;
  }

  /**
   * Add a single authority to the authorities set.
   *
   * @param authorization authority
   * @return self
   */
  public AuthorizationInterceptor addAuthorization(Authorization authorization) {
    if (authorizations == null) {
      authorizations = new HashSet<>();
    }
    authorizations.add(authorization);
    return this;
  }

  @Override
  public Future<Message<JsonObject>> intercept(Map<String, Object> context, Message<JsonObject> msg) {
    try {
      Promise<Message<JsonObject>> promise = Promise.promise();

      //for some reasons user hasn't been added neither by authn interceptor nor somehow else
      if (!context.containsKey("user")) {
        promise.complete(msg);
        return promise.future();
      }
      if (authorizations == null || authorizations.isEmpty()) {
        promise.complete(msg);
        return promise.future();
      }
      User user = (User) context.get("user");
      authorizationProvider.getAuthorizations(user, getAuthorizationsAsyncResult -> {
        if (getAuthorizationsAsyncResult.failed()) {
          promise.fail(new ReplyException(ReplyFailure.RECIPIENT_FAILURE, 500,
            getAuthorizationsAsyncResult.cause().getMessage()));
        } else {
          AuthorizationContext userAuthorizationContext = AuthorizationContext.create(user);
          for (Authorization authorization : authorizations) {
            // authorization failed
            if (!authorization.match(userAuthorizationContext)) {
              promise.fail(new ReplyException(ReplyFailure.RECIPIENT_FAILURE, 403, "Forbidden"));
              return;
            }
          }
          // all authorities have passed
          promise.complete(msg);
        }
      });
      return promise.future();
    } catch (CredentialValidationException e) {
      return Future.failedFuture(new ReplyException(ReplyFailure.RECIPIENT_FAILURE, 401, "Unauthorized"));
    }
  }
}
