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

//todo javadoc
public class InterceptorUtils {

  private InterceptorUtils() {
    throw new UnsupportedOperationException();
  }

  public static InterceptorPriority getWeight(Function<Message<JsonObject>, Future<Message<JsonObject>>> interceptor) {
    if (interceptor instanceof AuthenticationInterceptor) {
      return InterceptorPriority.AUTHN;
    }
    if (interceptor instanceof AuthorizationInterceptor) {
      return InterceptorPriority.AUTHZ;
    }

    return InterceptorPriority.USER;
  }

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
