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

import java.lang.reflect.Constructor;
import java.util.Objects;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class ProxyHelper {

  public static <T> T createProxy(Class<T> clazz, Vertx vertx, String address) {
    return createProxy(clazz, vertx, address, null);
  }

  public static <T> T createProxy(Class<T> clazz, Vertx vertx, String address, DeliveryOptions options) {
    String proxyClassName = clazz.getName() + "VertxEBProxy";
    Class<?> proxyClass = loadClass(proxyClassName, clazz);
    Constructor constructor;
    Object instance;
    if (options == null) {
      constructor = getConstructor(proxyClass, Vertx.class, String.class);
      instance = createInstance(constructor, vertx, address);
    } else {
      constructor = getConstructor(proxyClass, Vertx.class, String.class, DeliveryOptions.class);
      instance = createInstance(constructor, vertx, address, options);
    }
    return (T) instance;
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
    // No timeout - used for top level services
    return registerService(clazz, vertx, service, address, DEFAULT_CONNECTION_TIMEOUT);
  }

  public static <T> MessageConsumer<JsonObject> registerService(Class<T> clazz, Vertx vertx, T service, String address,
                                                                long timeoutSeconds) {
    // No timeout - used for top level services
    return registerService(clazz, vertx, service, address, true, timeoutSeconds);
  }

  public static <T> MessageConsumer<JsonObject> registerService(Class<T> clazz, Vertx vertx, T service, String address,
                                                                boolean topLevel,
                                                                long timeoutSeconds) {
    String handlerClassName = clazz.getName() + "VertxProxyHandler";
    Class<?> handlerClass = loadClass(handlerClassName, clazz);
    Constructor constructor = getConstructor(handlerClass, Vertx.class, clazz, boolean.class, long.class);
    Object instance = createInstance(constructor, vertx, service, topLevel, timeoutSeconds);
    ProxyHandler handler = (ProxyHandler) instance;
    return handler.registerHandler(address);
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

  private static Class<?> loadClass(String name, Class origin) {
    try {
      return origin.getClassLoader().loadClass(name);
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException("Cannot find proxyClass: " + name, e);
    }
  }

  private static Constructor getConstructor(Class<?> clazz, Class<?>... types) {
    try {
      return clazz.getDeclaredConstructor(types);
    } catch (NoSuchMethodException e) {
      throw new IllegalStateException("Cannot find constructor on: " + clazz.getName(), e);
    }
  }

  private static Object createInstance(Constructor constructor, Object... args) {
    try {
      return constructor.newInstance(args);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to call constructor on", e);
    }
  }
}
