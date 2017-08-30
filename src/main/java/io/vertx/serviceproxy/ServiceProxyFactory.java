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
import io.vertx.ext.auth.jwt.JWTAuth;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A factory for Service Proxies which state can be reused during the factory lifecycle.
 *
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
public class ServiceProxyFactory {

  public static final long DEFAULT_CONNECTION_TIMEOUT = 5 * 60; // 5 minutes

  private final Vertx vertx;

  private String address;
  private boolean topLevel = true;
  private long timeoutSeconds = DEFAULT_CONNECTION_TIMEOUT;
  private JWTAuth jwtAuth;
  private Set<String> authorities;
  private DeliveryOptions options;
  private String token;

  /**
   * Creates a factory.
   *
   * @param vertx a non null instance of vertx.
   */
  public ServiceProxyFactory(Vertx vertx) {
    Objects.requireNonNull(vertx);

    this.vertx = vertx;
  }

  /**
   * Set the address to use on the subsequent proxy creations or service registrations.
   *
   * @param address an eventbus address
   * @return self
   */
  public ServiceProxyFactory setAddress(String address) {
    this.address = address;
    return this;
  }

  /**
   * Set if the services to create are a top level services.
   *
   * @param topLevel true for top level (default: true)
   * @return self
   */
  public ServiceProxyFactory setTopLevel(boolean topLevel) {
    this.topLevel = topLevel;
    return this;
  }

  /**
   * Set the default timeout in seconds while waiting for a reply.
   *
   * @param timeoutSeconds the default timeout (default: 5 minutes)
   * @return self
   */
  public ServiceProxyFactory setTimeoutSeconds(long timeoutSeconds) {
    this.timeoutSeconds = timeoutSeconds;
    return this;
  }

  /**
   * Set a JWT auth that will verify all requests before the service is invoked.
   *
   * @param jwtAuth a JWT auth
   * @return self
   */
  public ServiceProxyFactory setJwtAuth(JWTAuth jwtAuth) {
    this.jwtAuth = jwtAuth;
    return this;
  }

  /**
   * Set the required authorities for the service, once a JWT is validated it will be
   * queried for these authorities. If authorities are missing a error 403 is returned.
   *
   * @param authorities set of authorities
   * @return self
   */
  public ServiceProxyFactory setAuthorities(Set<String> authorities) {
    this.authorities = authorities;
    return this;
  }

  /**
   * Add a single authority to the authorities set.
   *
   * @param authority authority
   * @return self
   */
  public ServiceProxyFactory addAuthority(String authority) {
    if (authorities == null) {
      authorities = new HashSet<>();
    }
    authorities.add(authority);
    return this;
  }

  /**
   * Clear the current set of authorities.
   * @return self
   */
  public ServiceProxyFactory clearAuthorities() {
    if (authorities != null) {
      authorities.clear();
    }
    return this;
  }

  /**
   * Set a JWT token to be used on proxy calls.
   *
   * @param token a JWT token
   * @return self
   */
  public ServiceProxyFactory setToken(String token) {
    this.token = token;
    return this;
  }

  /**
   * Set delivery options to be used during a proxy call.
   *
   * @param options delivery options
   * @return self
   */
  public ServiceProxyFactory setOptions(DeliveryOptions options) {
    this.options = options;
    return this;
  }

  /**
   * Registers a service on the event bus.
   *
   * @param clazz   the service class (interface)
   * @param service the service object
   * @param <T>     the type of the service interface
   * @return the consumer used to unregister the service
   */
  public <T> MessageConsumer<JsonObject> register(Class<T> clazz, T service) {
    Objects.requireNonNull(address);

    String handlerClassName = clazz.getName() + "VertxProxyHandler";
    Class<?> handlerClass = loadClass(handlerClassName, clazz);
    Constructor constructor = getConstructor(handlerClass, Vertx.class, clazz, boolean.class, long.class);
    Object instance = createInstance(constructor, vertx, service, topLevel, timeoutSeconds);
    ProxyHandler handler = (ProxyHandler) instance;
    // security
    handler.setJWTAuth(jwtAuth);
    handler.setAuthorities(authorities);
    // register
    return handler.register(vertx.eventBus(), address);
  }

  /**
   * Unregisters a published service.
   *
   * @param consumer the consumer returned by {@link #register(Class, Object)}.
   */
  public void unregister(MessageConsumer<JsonObject> consumer) {
    Objects.requireNonNull(consumer);

    if (consumer instanceof ProxyHandler) {
      ((ProxyHandler) consumer).close();
    } else {
      // Fall back to plain unregister.
      consumer.unregister();
    }
  }

  /**
   * Creates a proxy to a service on the event bus.
   *
   * @param clazz   the service class (interface)
   * @param <T>     the type of the service interface
   * @return a proxy to the service
   */
  public <T> T createProxy(Class<T> clazz) {
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
