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

import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.serviceproxy.ServiceJWTInterceptor;
import io.vertx.serviceproxy.ServiceBinder;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import io.vertx.serviceproxy.testmodel.*;
import io.vertx.test.core.VertxTestBase;
import org.junit.Test;

public class SecureServiceBinderTest extends VertxTestBase {

  private final static String SERVICE_ADDRESS = "someaddress";
  private final static String SERVICE_LOCAL_ADDRESS = "someaddress.local";
  private static final String TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE1MDE3ODUyMDZ9.08K_rROcCmKTF1cKfPCli2GQFYIOP8dePxeS1SE4dc8";

  private ServiceProxyBuilder serviceProxyBuilder, localServiceProxyBuilder;
  private MessageConsumer<JsonObject> consumer, localConsumer;
  private OKService proxy, localProxy;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    OKService service = new OKServiceImpl();

    ServiceBinder serviceBinder = new ServiceBinder(vertx)
      .setAddress(SERVICE_ADDRESS)
      .addInterceptor(new ServiceJWTInterceptor().setJwtAuth(JWTAuth.create(vertx, new JWTAuthOptions()
        .addPubSecKey(new PubSecKeyOptions().setSecretKey("notasecret").setPublicKey("noasecret").setAlgorithm("HS256")))));

    ServiceBinder localServiceBinder = new ServiceBinder(vertx)
      .setAddress(SERVICE_LOCAL_ADDRESS)
      .addInterceptor(new ServiceJWTInterceptor().setJwtAuth(JWTAuth.create(vertx, new JWTAuthOptions()
        .addPubSecKey(new PubSecKeyOptions().setSecretKey("notasecret").setPublicKey("noasecret").setAlgorithm("HS256")))));

    consumer = serviceBinder.register(OKService.class, service);
    localConsumer = localServiceBinder.registerLocal(OKService.class, service);

    serviceProxyBuilder = new ServiceProxyBuilder(vertx)
      .setAddress(SERVICE_ADDRESS);

    localServiceProxyBuilder = new ServiceProxyBuilder(vertx)
      .setAddress(SERVICE_LOCAL_ADDRESS);
  }

  @Override
  public void tearDown() throws Exception {
    consumer.unregister();
    localConsumer.unregister();
    super.tearDown();
  }

  @Test
  public void testWithToken() {

    serviceProxyBuilder.setToken(TOKEN);

    proxy = serviceProxyBuilder.build(OKService.class);

    proxy.ok(res -> {
      assertFalse(res.failed());
      testComplete();
    });
    await();
  }

  @Test
  public void testWithoutToken() {

    serviceProxyBuilder.setToken(null);

    proxy = serviceProxyBuilder.build(OKService.class);

    proxy.ok(this::checkResult);
    await();
  }

  @Test
  public void testLocalWithToken() {

    localServiceProxyBuilder.setToken(TOKEN);

    localProxy = localServiceProxyBuilder.build(OKService.class);

    localProxy.ok(res -> {
      assertFalse(res.failed());
      testComplete();
    });
    await();
  }

  @Test
  public void testLocalWithoutToken() {

    localServiceProxyBuilder.setToken(null);

    localProxy = localServiceProxyBuilder.build(OKService.class);

    localProxy.ok(this::checkResult);
    await();
  }

  private void checkResult(AsyncResult<String> res) {
    assertTrue(res.failed());
    ReplyException t = (ReplyException) res.cause();
    assertEquals(401, t.failureCode());
    testComplete();
  }
}
