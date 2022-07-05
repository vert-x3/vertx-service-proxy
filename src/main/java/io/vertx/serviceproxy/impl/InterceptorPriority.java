package io.vertx.serviceproxy.impl;

/**
 * Defines an interceptor priority to apply them in right order
 */
public enum InterceptorPriority {

  AUTHN,
  AUTHZ,
  USER
}
