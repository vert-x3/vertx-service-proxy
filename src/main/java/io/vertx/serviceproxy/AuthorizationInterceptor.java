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
 */
//todo use it
public class AuthorizationInterceptor implements Function<Message<JsonObject>, Future<Message<JsonObject>>> {

  private AuthenticationProvider authn;
  private AuthorizationProvider authz;

  private Set<Authorization> authorizations;

  /**
   * Set an authentication provider that will verify all requests before the service is invoked.
   *
   * @param provider an authentication provider
   * @return self
   */
  public AuthorizationInterceptor setAuthenticationProvider(AuthenticationProvider provider) {
    this.authn = provider;
    return this;
  }

  public AuthorizationInterceptor setAuthorizationProvider(AuthorizationProvider provider) {
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
  public Future<Message<JsonObject>> apply(Message<JsonObject> msg) {

    final TokenCredentials authorization = new TokenCredentials(msg.headers().get("auth-token"));

    try {
      authorization.checkValid(null);

      Promise<Message<JsonObject>> promise = Promise.promise();

      if (authn == null) {
        promise.fail(new ReplyException(ReplyFailure.RECIPIENT_FAILURE, 500, "No AuthenticationProvider present"));
        return promise.future();
      }

      authn.authenticate(authorization, authenticate -> {
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

        authz.getAuthorizations(user, getAuthorizations -> {
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
