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

package io.vertx.serviceproxy.test;

import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceProxyFactory;
import io.vertx.serviceproxy.testmodel.OKService;
import io.vertx.serviceproxy.testmodel.OKServiceImpl;
import io.vertx.test.core.VertxTestBase;
import org.junit.Test;

public class ServiceProxyFactoryTest extends VertxTestBase {

  private final static String SERVICE_ADDRESS = "someaddress";

  private MessageConsumer<JsonObject> consumer;
  private OKService service;
  private OKService proxy;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    service = new OKServiceImpl();

    final ServiceProxyFactory factory = new ServiceProxyFactory(vertx).setAddress(SERVICE_ADDRESS);

    consumer = factory.register(OKService.class, service);
    proxy = factory.createProxy(OKService.class);
  }

  @Override
  public void tearDown() throws Exception {
    consumer.unregister();
    super.tearDown();
  }

  @Test
  public void testFactory() {
    proxy.ok(res -> {
      assertFalse(res.failed());
      testComplete();
    });
    await();
  }
}
