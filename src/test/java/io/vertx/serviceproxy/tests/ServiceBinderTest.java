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

package io.vertx.serviceproxy.tests;

import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import io.vertx.serviceproxy.tests.testmodel.OKService;
import io.vertx.serviceproxy.tests.testmodel.OKServiceImpl;
import io.vertx.test.core.VertxTestBase;
import org.junit.Test;

public class ServiceBinderTest extends VertxTestBase {

  private final static String SERVICE_ADDRESS = "someaddress";
  private final static String SERVICE_LOCAL_ADDRESS = "someaddress.local";

  private MessageConsumer<JsonObject> consumer, localConsumer;
  private OKService proxy, localProxy;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    OKService service = new OKServiceImpl();

    final ServiceBinder serviceBinder = new ServiceBinder(vertx).setAddress(SERVICE_ADDRESS);
    final ServiceBinder serviceLocalBinder = new ServiceBinder(vertx).setAddress(SERVICE_LOCAL_ADDRESS);
    final ServiceProxyBuilder serviceProxyBuilder = new ServiceProxyBuilder(vertx).setAddress(SERVICE_ADDRESS);
    final ServiceProxyBuilder serviceLocalProxyBuilder = new ServiceProxyBuilder(vertx).setAddress(SERVICE_LOCAL_ADDRESS);

    consumer = serviceBinder.register(OKService.class, service);
    localConsumer = serviceLocalBinder.registerLocal(OKService.class, service);
    proxy = serviceProxyBuilder.build(OKService.class);
    localProxy = serviceLocalProxyBuilder.build(OKService.class);
  }

  @Override
  public void tearDown() throws Exception {
    consumer.unregister();
    localConsumer.unregister();
    super.tearDown();
  }

  @Test
  public void testFactory() {
    proxy.ok().onComplete(res -> {
      assertFalse(res.failed());
      testComplete();
    });
    await();
  }

  @Test
  public void testLocalFactory() {
    localProxy.ok().onComplete(res -> {
      assertFalse(res.failed());
      testComplete();
    });
    await();
  }
}
