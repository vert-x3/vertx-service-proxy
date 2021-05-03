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

package io.vertx.serviceproxy.testmodel2;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import io.vertx.serviceproxy.testmodel2.impl.FuturizedTestServiceImpl;

@ProxyGen
@VertxGen
public interface FuturizedTestService {

  static FuturizedTestService create(Vertx vertx) throws Exception {
    return new FuturizedTestServiceImpl(vertx);
  }

  static FuturizedTestService createProxy(Vertx vertx, String address) {
    return new ServiceProxyBuilder(vertx).setAddress(address).build(FuturizedTestService.class);
  }

  Future<String> ok();
}
