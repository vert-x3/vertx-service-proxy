package io.vertx.serviceproxy.impl.utils;

import io.vertx.serviceproxy.AuthenticationInterceptor;
import io.vertx.serviceproxy.AuthorizationInterceptor;
import io.vertx.serviceproxy.InterceptorHolder;
import io.vertx.serviceproxy.ServiceInterceptor;
import io.vertx.serviceproxy.impl.InterceptorPriority;

import java.util.List;

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
  public static InterceptorPriority getWeight(ServiceInterceptor interceptor) {
    if (interceptor instanceof AuthenticationInterceptor) {
      return InterceptorPriority.AUTHENTICATION;
    }
    if (interceptor instanceof AuthorizationInterceptor) {
      return InterceptorPriority.AUTHORIZATION;
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
                                           ServiceInterceptor interceptor) {
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
