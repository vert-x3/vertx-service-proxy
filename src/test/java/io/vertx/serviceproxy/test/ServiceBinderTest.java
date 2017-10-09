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
import io.vertx.serviceproxy.ServiceBinder;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import io.vertx.serviceproxy.testmodel.OKService;
import io.vertx.serviceproxy.testmodel.OKServiceImpl;
import io.vertx.test.core.VertxTestBase;
import org.junit.Test;

public class ServiceBinderTest extends VertxTestBase {

  private final static String SERVICE_ADDRESS = "someaddress";

  private MessageConsumer<JsonObject> consumer;
  private OKService proxy;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    OKService service = new OKServiceImpl();

    final ServiceBinder serviceBinder = new ServiceBinder(vertx).setAddress(SERVICE_ADDRESS);
    final ServiceProxyBuilder serviceProxyBuilder = new ServiceProxyBuilder(vertx).setAddress(SERVICE_ADDRESS);

    consumer = serviceBinder.register(OKService.class, service);
    proxy = serviceProxyBuilder.build(OKService.class);
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
