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

package io.vertx.proxygen.test;

import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.proxygen.testmodel.SomeEnum;
import io.vertx.proxygen.testmodel.TestService;
import io.vertx.test.core.VertxTestBase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class ProxyGenTest extends VertxTestBase {

  TestService service;
  TestService proxy;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    service = TestService.testService(vertx);
    service.register(vertx, "someaddress");
    proxy = TestService.createProxy(vertx, "someaddress");
    vertx.eventBus().<String>consumer("testaddress").handler(msg -> {
      assertEquals("ok", msg.body());
      testComplete();
    });
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }

  @Test
  public void testNoParams() {
    proxy.noParams();
    await();
  }

  @Test
  public void testBasicTypes() {
    proxy.basicTypes("foo", (byte) 123, (short) 1234, 12345, 123456l, 12.34f, 12.3456d, 'X', true);
    await();
  }

  @Test
  public void testBasicBoxedTypes() {
    proxy.basicBoxedTypes("foo", (byte) 123, (short) 1234, 12345, 123456l, 12.34f, 12.3456d, 'X', true);
    await();
  }

  @Test
  public void testJsonTypes() {
    proxy.jsonTypes(new JsonObject().putString("foo", "bar"), new JsonArray().add("wibble"));
    await();
  }

  @Test
  public void testEnumType() {
    proxy.enumType(SomeEnum.WIBBLE);
    await();
  }

  @Test
  public void testStringHandler() {
    proxy.stringHandler(onSuccess(res -> {
      assertEquals("foobar", res);
      testComplete();
    }));
    await();
  }

  @Test
  public void testByteHandler() {
    proxy.byteHandler(onSuccess(res -> {
      assertEquals(Byte.valueOf((byte) 123), res);
      testComplete();
    }));
    await();
  }

  @Test
  public void testShortHandler() {
    proxy.shortHandler(onSuccess(res -> {
      assertEquals(Short.valueOf((short)1234), res);
      testComplete();
    }));
    await();
  }

  @Test
  public void testIntHandler() {
    proxy.intHandler(onSuccess(res -> {
      assertEquals(Integer.valueOf(12345), res);
      testComplete();
    }));
    await();
  }

  @Test
  public void testLongHandler() {
    proxy.longHandler(onSuccess(res -> {
      assertEquals(Long.valueOf(123456l), res);
      testComplete();
    }));
    await();
  }

  @Test
  public void testFloatHandler() {
    proxy.floatHandler(onSuccess(res -> {
      assertEquals(Float.valueOf(12.34f), res);
      testComplete();
    }));
    await();
  }

  @Test
  public void testDoubleHandler() {
    proxy.doubleHandler(onSuccess(res -> {
      assertEquals(Double.valueOf(12.3456d), res);
      testComplete();
    }));
    await();
  }

  @Test
  public void testCharHandler() {
    proxy.charHandler(onSuccess(res -> {
      assertEquals(Character.valueOf('X'), res);
      testComplete();
    }));
    await();
  }

  @Test
  public void testBooleanHandler() {
    proxy.booleanHandler(onSuccess(res -> {
      assertEquals(true, res);
      testComplete();
    }));
    await();
  }

  @Test
  public void testJsonObjectHandler() {
    proxy.jsonObjectHandler(onSuccess(res -> {
      assertEquals("wibble", res.getString("blah"));
      testComplete();
    }));
    await();
  }

  @Test
  public void testJsonArrayHandler() {
    proxy.jsonArrayHandler(onSuccess(res -> {
      assertEquals("blurrg", res.get(0));
      testComplete();
    }));
    await();
  }

  @Test
  public void testVoidHandler() {
    proxy.voidHandler(onSuccess(res -> {
      assertNull(res);
      testComplete();
    }));
    await();
  }

  @Test
  public void testFluentMethod() {
    assertSame(proxy, proxy.fluentMethod("foo", onSuccess(res -> {
      assertEquals("bar", res);
      testComplete();
    })));
    await();
  }

  @Test
  public void testFluentNoParams() {
    assertSame(proxy, proxy.fluentNoParams());
    await();
  }

  @Test
  public void testFailingMethod() {
    proxy.failingMethod(onFailure(t -> {
      assertTrue(t instanceof ReplyException);
      ReplyException re = (ReplyException)t;
      assertEquals(ReplyFailure.RECIPIENT_FAILURE, re.failureType());
      assertEquals("wibble", re.getMessage());
      testComplete();
    }));
    await();
  }

  @Test
  public void testCallWithMessage() {
    JsonObject message = new JsonObject();
    message.putObject("object", new JsonObject().putString("foo", "bar"));
    message.putString("str", "blah");
    message.putNumber("i", 1234);
    message.putNumber("chr", (int)'X'); // chars are mapped to ints
    message.putString("senum", SomeEnum.BAR.toString()); // enums are mapped to strings
    vertx.eventBus().<String>send("someaddress", message, new DeliveryOptions().addHeader("action", "invokeWithMessage"), onSuccess(res -> {
      assertEquals("goats", res.body());
      testComplete();
    }));
    await();
  }

  @Test
  public void testCallWithMessageInvalidAction() {
    JsonObject message = new JsonObject();
    message.putObject("object", new JsonObject().putString("foo", "bar"));
    message.putString("str", "blah");
    message.putNumber("i", 1234);
    vertx.eventBus().send("someaddress", message, new DeliveryOptions().addHeader("action", "yourmum").setSendTimeout(500), onFailure(t -> {
      assertTrue(t instanceof ReplyException);
      ReplyException re = (ReplyException)t;
      // This will as operation will fail to be invoked
      assertEquals(ReplyFailure.TIMEOUT, re.failureType());
      testComplete();
    }));
    await();
  }

  @Test
  public void testCallWithMessageParamWrongType() {
    JsonObject message = new JsonObject();
    message.putObject("object", new JsonObject().putString("foo", "bar"));
    message.putNumber("str", 76523);
    message.putNumber("i", 1234);
    message.putNumber("char", (int)'X'); // chars are mapped to ints
    message.putString("enum", SomeEnum.BAR.toString()); // enums are mapped to strings
    vertx.eventBus().send("someaddress", message, new DeliveryOptions().addHeader("action", "invokeWithMessage").setSendTimeout(500), onFailure(t -> {
      assertTrue(t instanceof ReplyException);
      ReplyException re = (ReplyException) t;
      // This will as operation will fail to be invoked
      assertEquals(ReplyFailure.TIMEOUT, re.failureType());
      testComplete();
    }));
    await();
  }

  @Test
  public void testListStringHandler() {
    proxy.listStringHandler(onSuccess(list -> {
      assertEquals("foo", list.get(0));
      assertEquals("bar", list.get(1));
      assertEquals("wibble", list.get(2));
      testComplete();
    }));
    await();
  }

  @Test
  public void testListByteHandler() {
    proxy.listByteHandler(onSuccess(list -> {
      assertEquals(Byte.valueOf((byte) 1), list.get(0));
      assertEquals(Byte.valueOf((byte) 2), list.get(1));
      assertEquals(Byte.valueOf((byte) 3), list.get(2));
      testComplete();
    }));
    await();
  }

  @Test
  public void testListShortHandler() {
    proxy.listShortHandler(onSuccess(list -> {
      assertEquals(Short.valueOf((short)11), list.get(0));
      assertEquals(Short.valueOf((short)12), list.get(1));
      assertEquals(Short.valueOf((short)13), list.get(2));
      testComplete();
    }));
    await();
  }

  @Test
  public void testListIntHandler() {
    proxy.listIntHandler(onSuccess(list -> {
      assertEquals(100, list.get(0).intValue());
      assertEquals(101, list.get(1).intValue());
      assertEquals(102, list.get(2).intValue());
      testComplete();
    }));
    await();
  }

  @Test
  public void testListLongHandler() {
    proxy.listLongHandler(onSuccess(list -> {
      assertEquals(1000l, list.get(0).longValue());
      assertEquals(1001l, list.get(1).longValue());
      assertEquals(1002l, list.get(2).longValue());
      testComplete();
    }));
    await();
  }

  @Test
  public void testListFloatHandler() {
    proxy.listFloatHandler(onSuccess(list -> {
      assertEquals(1.1f, list.get(0), 0);
      assertEquals(1.2f, list.get(1), 0);
      assertEquals(1.3f, list.get(2), 0);
      testComplete();
    }));
    await();
  }

  @Test
  public void testListDoubleHandler() {
    proxy.listDoubleHandler(onSuccess(list -> {
      assertEquals(1.11d, list.get(0), 0);
      assertEquals(1.12d, list.get(1), 0);
      assertEquals(1.13d, list.get(2), 0);
      testComplete();
    }));
    await();
  }

  @Test
  public void testListCharHandler() {
    proxy.listCharHandler(onSuccess(list -> {
      assertEquals('X', list.get(0).charValue());
      assertEquals('Y', list.get(1).charValue());
      assertEquals('Z', list.get(2).charValue());
      testComplete();
    }));
    await();
  }

  @Test
  public void testListBoolHandler() {
    proxy.listBoolHandler(onSuccess(list -> {
      assertEquals(true, list.get(0));
      assertEquals(false, list.get(1));
      assertEquals(true, list.get(2));
      testComplete();
    }));
    await();
  }
  @Test
  public void testListJsonObjectHandler() {
    proxy.listJsonObjectHandler(onSuccess(list -> {
      assertEquals("foo", list.get(0).getString("a"));
      assertEquals("bar", list.get(1).getString("b"));
      assertEquals("wibble", list.get(2).getString("c"));
      testComplete();
    }));
    await();
  }

  @Test
  public void testListJsonArrayHandler() {
    proxy.listJsonArrayHandler(onSuccess(list -> {
      assertEquals("foo", list.get(0).get(0));
      assertEquals("bar", list.get(1).get(0));
      assertEquals("wibble", list.get(2).get(0));
      testComplete();
    }));
    await();
    List<String> foo = new ArrayList<>();
    foo.stream().filter(str -> str.contains("foo")).collect(Collectors.toList());
  }

}
