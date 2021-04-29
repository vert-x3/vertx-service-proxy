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

import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;
import io.vertx.serviceproxy.ServiceException;
import io.vertx.serviceproxy.testmodel.MyServiceException;
import io.vertx.serviceproxy.testmodel.MyServiceExceptionMessageCodec;
import io.vertx.serviceproxy.testmodel.SomeEnum;
import io.vertx.serviceproxy.testmodel.TestDataObject;
import io.vertx.serviceproxy.testmodel.TestService;
import io.vertx.serviceproxy.testmodel2.FuturizedTestService;
import io.vertx.test.core.VertxTestBase;
import org.junit.Test;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author lalitrao
 */
public class FuturizedServiceProxyTest extends VertxTestBase {

  public final static String SERVICE_ADDRESS = "someaddress";
  public final static String SERVICE_WITH_DEBUG_ADDRESS = "someaddressdebug";
  public final static String SERVICE_LOCAL_ADDRESS = "someaddress.local";
  public final static String TEST_ADDRESS = "testaddress";

  MessageConsumer<JsonObject> consumer, localConsumer, consumerWithDebugEnabled;
  FuturizedTestService service, localService;
  FuturizedTestService proxy, localProxy, proxyWithDebug;
  URI uri1, uri2;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    uri1 = new URI("http://foo.com");
    uri2 = new URI("http://bar.com");
    service = FuturizedTestService.create(vertx);
    localService = FuturizedTestService.create(vertx);

    consumer = new ServiceBinder(vertx).setAddress(SERVICE_ADDRESS)
      .register(FuturizedTestService.class, service);
    consumerWithDebugEnabled = new ServiceBinder(vertx)
      .setAddress(SERVICE_WITH_DEBUG_ADDRESS)
      .setIncludeDebugInfo(true)
      .register(FuturizedTestService.class, service);
    localConsumer = new ServiceBinder(vertx).setAddress(SERVICE_LOCAL_ADDRESS)
      .registerLocal(FuturizedTestService.class, localService);

    proxy = FuturizedTestService.createProxy(vertx, SERVICE_ADDRESS);
    localProxy = FuturizedTestService.createProxy(vertx, SERVICE_LOCAL_ADDRESS);
    proxyWithDebug = FuturizedTestService.createProxy(vertx, SERVICE_WITH_DEBUG_ADDRESS);
    vertx.eventBus().<String>consumer(TEST_ADDRESS).handler(msg -> {
      assertEquals("ok", msg.body());
      testComplete();
    });
  }

  @Override
  public void tearDown() throws Exception {
    consumer.unregister();
    localConsumer.unregister();
    super.tearDown();
  }


  @Test
  public void testStringHandler() {
    proxy.ok().onSuccess(res -> {
      assertEquals("foobar", res);
      testComplete();
    });
    await();
  }
}
