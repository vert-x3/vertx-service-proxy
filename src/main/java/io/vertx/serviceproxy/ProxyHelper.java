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
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;

import java.lang.reflect.Constructor;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class ProxyHelper {

  public static <T> T createProxy(Class<T> clazz, Vertx vertx, String address) {
    String proxyClassName = clazz.getName() + "VertxEBProxy";
    Class<?> proxyClass = loadClass(proxyClassName);
    Constructor constructor = getConstructor(proxyClass, Vertx.class, String.class);
    Object instance = createInstance(constructor, vertx, address);
    return (T)instance;
  }

  public static <T> MessageConsumer<JsonObject> registerService(Class<T> clazz, Vertx vertx, T service, String address) {
    String handlerClassName = clazz.getName() + "VertxProxyHandler";
    Class<?> handlerClass = loadClass(handlerClassName);
    Constructor constructor = getConstructor(handlerClass, Vertx.class, clazz, String.class);
    Object instance = createInstance(constructor, vertx, service, address);
    ProxyHandler handler = (ProxyHandler)instance;
    MessageConsumer<JsonObject> consumer = vertx.eventBus().<JsonObject>consumer(address).handler(handler);
    handler.setConsumer(consumer);
    return consumer;
  }

  private static Class<?> loadClass(String name) {
    try {
      return Class.forName(name);
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
