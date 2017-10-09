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

import java.lang.reflect.Constructor;
import java.util.Objects;

/**
 * A builder for Service Proxies which state can be reused during the builder lifecycle.
 *
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
public class ServiceProxyBuilder {

  private final Vertx vertx;

  private String address;
  private DeliveryOptions options;
  private String token;

  /**
   * Creates a builder.
   *
   * @param vertx a non null instance of vertx.
   */
  public ServiceProxyBuilder(Vertx vertx) {
    Objects.requireNonNull(vertx);

    this.vertx = vertx;
  }

  /**
   * Set the address to use on the subsequent proxy creations or service registrations.
   *
   * @param address an eventbus address
   * @return self
   */
  public ServiceProxyBuilder setAddress(String address) {
    this.address = address;
    return this;
  }

  /**
   * Set a JWT token to be used on proxy calls.
   *
   * @param token a JWT token
   * @return self
   */
  public ServiceProxyBuilder setToken(String token) {
    this.token = token;
    return this;
  }

  /**
   * Set delivery options to be used during a proxy call.
   *
   * @param options delivery options
   * @return self
   */
  public ServiceProxyBuilder setOptions(DeliveryOptions options) {
    this.options = options;
    return this;
  }

  /**
   * Creates a proxy to a service on the event bus.
   *
   * @param clazz   the service class (interface)
   * @param <T>     the type of the service interface
   * @return a proxy to the service
   */
  public <T> T build(Class<T> clazz) {
    Objects.requireNonNull(address);

    String proxyClassName = clazz.getName() + "VertxEBProxy";
    Class<?> proxyClass = loadClass(proxyClassName, clazz);
    Constructor constructor;
    Object instance;

    if (token != null) {
      if (options == null) {
        options = new DeliveryOptions();
      }
      options.addHeader("auth-token", token);
    }

    if (options == null) {
      constructor = getConstructor(proxyClass, Vertx.class, String.class);
      instance = createInstance(constructor, vertx, address);
    } else {
      constructor = getConstructor(proxyClass, Vertx.class, String.class, DeliveryOptions.class);
      instance = createInstance(constructor, vertx, address, options);
    }
    return (T) instance;
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
