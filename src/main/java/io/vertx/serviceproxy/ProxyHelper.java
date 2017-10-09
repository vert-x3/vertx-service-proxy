/*
 * Copyright 2014 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.serviceproxy;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;

import java.util.Objects;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 *
 * @deprecated for a more robust proxy creation see: {@link ServiceBinder}
 */
@Deprecated
public class ProxyHelper {

  public static <T> T createProxy(Class<T> clazz, Vertx vertx, String address) {
    return new ServiceProxyBuilder(vertx)
      .setAddress(address)
      .build(clazz);
  }

  public static <T> T createProxy(Class<T> clazz, Vertx vertx, String address, DeliveryOptions options) {
    return new ServiceProxyBuilder(vertx)
      .setAddress(address)
      .setOptions(options)
      .build(clazz);
  }

  public static final long DEFAULT_CONNECTION_TIMEOUT = 5 * 60; // 5 minutes

  /**
   * Registers a service on the event bus.
   *
   * @param clazz   the service class (interface)
   * @param vertx   the vert.x instance
   * @param service the service object
   * @param address the address on which the service is published
   * @param <T>     the type of the service interface
   * @return the consumer used to unregister the service
   */
  public static <T> MessageConsumer<JsonObject> registerService(Class<T> clazz, Vertx vertx, T service, String address) {
    return new ServiceBinder(vertx)
      .setAddress(address)
      .register(clazz, service);
  }

  public static <T> MessageConsumer<JsonObject> registerService(Class<T> clazz, Vertx vertx, T service, String address,
                                                                long timeoutSeconds) {
    return new ServiceBinder(vertx)
      .setAddress(address)
      .setTimeoutSeconds(timeoutSeconds)
      .register(clazz, service);
  }

  public static <T> MessageConsumer<JsonObject> registerService(Class<T> clazz, Vertx vertx, T service, String address,
                                                                boolean topLevel,
                                                                long timeoutSeconds) {
    return new ServiceBinder(vertx)
      .setAddress(address)
      .setTopLevel(topLevel)
      .setTimeoutSeconds(timeoutSeconds)
      .register(clazz, service);
  }

  /**
   * Unregisters a published service.
   *
   * @param consumer the consumer returned by {@link #registerService(Class, Vertx, Object, String)}.
   */
  public static void unregisterService(MessageConsumer<JsonObject> consumer) {
    Objects.requireNonNull(consumer);
    if (consumer instanceof ProxyHandler) {
      ((ProxyHandler) consumer).close();
    } else {
      // Fall back to plain unregister.
      consumer.unregister();
    }
  }
}
