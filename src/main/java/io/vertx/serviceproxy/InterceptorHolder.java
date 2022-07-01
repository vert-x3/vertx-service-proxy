package io.vertx.serviceproxy;

import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import java.util.function.Function;

public class InterceptorHolder {

  private String action;

  private Function<Message<JsonObject>, Future<Message<JsonObject>>> interceptor;

  public InterceptorHolder(String action, Function<Message<JsonObject>, Future<Message<JsonObject>>> interceptor) {
    this.action = action;
    this.interceptor = interceptor;
  }

  public InterceptorHolder(Function<Message<JsonObject>, Future<Message<JsonObject>>> interceptor) {
    this.interceptor = interceptor;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public Function<Message<JsonObject>, Future<Message<JsonObject>>> getInterceptor() {
    return interceptor;
  }

  public void setInterceptor(Function<Message<JsonObject>, Future<Message<JsonObject>>> interceptor) {
    this.interceptor = interceptor;
  }
}
