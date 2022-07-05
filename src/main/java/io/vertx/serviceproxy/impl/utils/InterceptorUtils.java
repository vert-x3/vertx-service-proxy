package io.vertx.serviceproxy.impl.utils;

import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.AuthenticationInterceptor;
import io.vertx.serviceproxy.AuthorizationInterceptor;
import io.vertx.serviceproxy.InterceptorHolder;
import io.vertx.serviceproxy.impl.InterceptorPriority;

import java.util.List;
import java.util.function.Function;

public class InterceptorUtils {

  private InterceptorUtils() {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns interceptor's weight according to the class instance
   *
   * @param interceptor interceptor to define
   * @return {@link InterceptorPriority}
   */
  public static InterceptorPriority getWeight(Function<Message<JsonObject>, Future<Message<JsonObject>>> interceptor) {
    if (interceptor instanceof AuthenticationInterceptor) {
      return InterceptorPriority.AUTHN;
    }
    if (interceptor instanceof AuthorizationInterceptor) {
      return InterceptorPriority.AUTHZ;
    }

    return InterceptorPriority.USER;
  }

  /**
   * Checks if interceptors are adding in the right priority (according to the {@link InterceptorPriority}).
   * If not, throws an {@link IllegalStateException}
   *
   * @param interceptorHolders current interceptor holders
   * @param interceptor        interceptor to add
   */
  public static void checkInterceptorOrder(List<InterceptorHolder> interceptorHolders,
                                           Function<Message<JsonObject>, Future<Message<JsonObject>>> interceptor) {
    if (interceptorHolders.isEmpty()) {
      return;
    }
    final InterceptorPriority weight = getWeight(interceptor);
    final InterceptorPriority lastWeight;
    lastWeight = getWeight(interceptorHolders.get(interceptorHolders.size() - 1).interceptor());
    if (lastWeight.ordinal() > weight.ordinal()) {
      String message = String.format("Cannot add [%s] interceptor to service binder, please check adding order!",
        weight.name());
      throw new IllegalStateException(message);
    }
  }
}
