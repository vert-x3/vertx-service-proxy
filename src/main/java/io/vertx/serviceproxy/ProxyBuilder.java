package io.vertx.serviceproxy;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;

@VertxGen
public interface ProxyBuilder {

  static ProxyBuilder create(Vertx vertx) {
    return new ServiceProxyBuilder(vertx);
  }

  @Fluent
  ProxyBuilder setAddress(String address);

  @Fluent
  ProxyBuilder setToken(String token);

  @Fluent
  ProxyBuilder setOptions(DeliveryOptions options);

  <T> T build(Class<T> clazz);
}
