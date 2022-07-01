package io.vertx.serviceproxy;

import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import java.util.function.Function;

public class InterceptorHolder {

  private final String action;

  private final Function<Message<JsonObject>, Future<Message<JsonObject>>> interceptor;

  public InterceptorHolder(String action, Function<Message<JsonObject>, Future<Message<JsonObject>>> interceptor) {
    this.action = action;
    this.interceptor = interceptor;
  }

  public InterceptorHolder(Function<Message<JsonObject>, Future<Message<JsonObject>>> interceptor) {
    this.action = null;
    this.interceptor = interceptor;
  }

  public String action() {
    return action;
  }

  public Function<Message<JsonObject>, Future<Message<JsonObject>>> interceptor() {
    return interceptor;
  }
}
