/*
 * Copyright 2021 Red Hat, Inc.
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
package io.vertx.serviceproxy.impl;

import io.vertx.serviceproxy.ServiceInterceptor;

public class InterceptorHolder {

  private final String action;

  private final ServiceInterceptor interceptor;

  public InterceptorHolder(String action, ServiceInterceptor interceptor) {
    this.action = action;
    this.interceptor = interceptor;
  }

  public InterceptorHolder(ServiceInterceptor interceptor) {
    this.action = null;
    this.interceptor = interceptor;
  }

  public String action() {
    return action;
  }

  public ServiceInterceptor interceptor() {
    return interceptor;
  }
}
