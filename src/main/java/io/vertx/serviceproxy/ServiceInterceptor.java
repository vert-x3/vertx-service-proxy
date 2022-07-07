package io.vertx.serviceproxy;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import java.util.Map;

@VertxGen
@FunctionalInterface
public interface ServiceInterceptor {

  /**
   * Perform the interceptor handling
   *
   * @param vertx              the VertX instance
   * @param interceptorContext context to be shared between interceptors
   * @param body               message body
   * @return {@link Future}
   */
  Future<Message<JsonObject>> intercept(Vertx vertx, Map<String, Object> interceptorContext, Message<JsonObject> body);
}
