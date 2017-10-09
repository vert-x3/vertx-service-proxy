package io.vertx.serviceproxy;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.jwt.JWTAuth;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Create an event bus service interceptor using a JWT auth that will verify all requests before the service is invoked
 * <p/>
 * Once a JWT is validated it will be queried for authorities. If authorities are missing a error 403 is returned.
 */
public class ServiceJWTInterceptor implements Function<Message<JsonObject>, Future<Message<JsonObject>>> {

  private JWTAuth jwtAuth;
  private Set<String> authorities;

  /**
   * Set a JWT auth that will verify all requests before the service is invoked.
   *
   * @param jwtAuth a JWT auth
   * @return self
   */
  public ServiceJWTInterceptor setJwtAuth(JWTAuth jwtAuth) {
    this.jwtAuth = jwtAuth;
    return this;
  }

  /**
   * Set the required authorities for the service, once a JWT is validated it will be
   * queried for these authorities. If authorities are missing a error 403 is returned.
   *
   * @param authorities set of authorities
   * @return self
   */
  public ServiceJWTInterceptor setAuthorities(Set<String> authorities) {
    this.authorities = authorities;
    return this;
  }

  /**
   * Add a single authority to the authorities set.
   *
   * @param authority authority
   * @return self
   */
  public ServiceJWTInterceptor addAuthority(String authority) {
    if (authorities == null) {
      authorities = new HashSet<>();
    }
    authorities.add(authority);
    return this;
  }

  /**
   * Clear the current set of authorities.
   * @return self
   */
  public ServiceJWTInterceptor clearAuthorities() {
    if (authorities != null) {
      authorities.clear();
    }
    return this;
  }

  @Override
  public Future<Message<JsonObject>> apply(Message<JsonObject> msg) {
    final String authorization = msg.headers().get("auth-token");

    if (authorization == null) {
      return Future.failedFuture(new ReplyException(ReplyFailure.RECIPIENT_FAILURE, 401, "Unauthorized"));
    }

    Future<Message<JsonObject>> fut = Future.future();

    jwtAuth.authenticate(new JsonObject().put("jwt", authorization), authenticate -> {
      if (authenticate.failed()) {
        fut.fail(new ReplyException(ReplyFailure.RECIPIENT_FAILURE, 500, authenticate.cause().getMessage()));
        return;
      }

      final User user = authenticate.result();

      if (user == null) {
        fut.fail(new ReplyException(ReplyFailure.RECIPIENT_FAILURE, 403, "Forbidden"));
        return;
      }

      final int requiredcount = authorities == null ? 0 : authorities.size();

      if (requiredcount > 0) {

        AtomicInteger count = new AtomicInteger();
        AtomicBoolean sentFailure = new AtomicBoolean();

        Handler<AsyncResult<Boolean>> authHandler = res -> {
          if (res.succeeded()) {
            if (res.result()) {
              if (count.incrementAndGet() == requiredcount) {
                // Has all required authorities
                fut.complete(msg);
              }
            } else {
              if (sentFailure.compareAndSet(false, true)) {
                fut.fail(new ReplyException(ReplyFailure.RECIPIENT_FAILURE, 403, "Forbidden"));
              }
            }
          } else {
            fut.fail(new ReplyException(ReplyFailure.RECIPIENT_FAILURE, 500, res.cause().getMessage()));
          }
        };
        for (String authority : authorities) {
          if (!sentFailure.get()) {
            user.isAuthorised(authority, authHandler);
          }
        }
      } else {
        // No auth required
        fut.complete(msg);
      }
    });

    return fut;
  }
}
