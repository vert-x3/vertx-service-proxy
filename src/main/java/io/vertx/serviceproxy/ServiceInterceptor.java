package io.vertx.serviceproxy;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import java.util.Map;

@VertxGen
@FunctionalInterface
public interface ServiceInterceptor {

  Future<Message<JsonObject>> intercept(Map<String, Object> context, Message<JsonObject> msg);
}
