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

import static java.util.concurrent.TimeUnit.SECONDS;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.serviceproxy.testmodel.*;
import org.junit.Test;

import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;
import io.vertx.serviceproxy.ServiceException;
import io.vertx.test.core.VertxTestBase;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author lalitrao
 */
public class ServiceProxyTest extends VertxTestBase {

  public final static String SERVICE_ADDRESS = "someaddress";
  public final static String SERVICE_WITH_DEBUG_ADDRESS = "someaddressdebug";
  public final static String SERVICE_LOCAL_ADDRESS = "someaddress.local";
  public final static String FUTURE_SERVICE_ADDRESS = "someaddress.future";
  public final static String FUTURE_SERVICE_LOCAL_ADDRESS = "someaddress.future.local";
  public final static String TEST_ADDRESS = "testaddress";

  MessageConsumer<JsonObject> consumer, localConsumer, consumerWithDebugEnabled;
  MessageConsumer<JsonObject> futureConsumer, localFutureConsumer;
  TestService service, localService;
  TestFutureService futureService, localFutureService;
  TestService proxy, localProxy, proxyWithDebug;
  TestFutureService futureProxy, localFutureProxy;
  URI uri1, uri2;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    uri1 = new URI("http://foo.com");
    uri2 = new URI("http://bar.com");
    service = TestService.create(vertx);
    localService = TestService.create(vertx);
    futureService = TestFutureService.create(vertx, service);
    localFutureService = TestFutureService.create(vertx, localService);

    consumer = new ServiceBinder(vertx).setAddress(SERVICE_ADDRESS)
      .register(TestService.class, service);
    consumerWithDebugEnabled = new ServiceBinder(vertx)
      .setAddress(SERVICE_WITH_DEBUG_ADDRESS)
      .setIncludeDebugInfo(true)
      .register(TestService.class, service);
    localConsumer = new ServiceBinder(vertx).setAddress(SERVICE_LOCAL_ADDRESS)
      .registerLocal(TestService.class, localService);
    futureConsumer = new ServiceBinder(vertx).setAddress(FUTURE_SERVICE_ADDRESS)
      .register(TestFutureService.class, futureService);
    localFutureConsumer = new ServiceBinder(vertx).setAddress(FUTURE_SERVICE_LOCAL_ADDRESS)
      .registerLocal(TestFutureService.class, localFutureService);

    proxy = TestService.createProxy(vertx, SERVICE_ADDRESS);
    localProxy = TestService.createProxy(vertx, SERVICE_LOCAL_ADDRESS);
    proxyWithDebug = TestService.createProxy(vertx, SERVICE_WITH_DEBUG_ADDRESS);
    futureProxy = TestFutureService.createProxy(vertx, FUTURE_SERVICE_ADDRESS);
    localFutureProxy = TestFutureService.createProxy(vertx, FUTURE_SERVICE_LOCAL_ADDRESS);
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
  public void testErrorHandling() {
    failingCallHandler(proxy::failingCall);
  }

  private void failingCallHandler(BiConsumer<String, Handler<AsyncResult<JsonObject>>> consumer) {
    consumer.accept("Fail", handler -> {
      assertTrue(handler.cause() instanceof ServiceException);
      assertEquals("Call has failed", handler.cause().getMessage());
      assertEquals(25, ((ServiceException) handler.cause()).failureCode());
      assertEquals(new JsonObject().put("test", "val"), ((ServiceException) handler.cause()).getDebugInfo());
      testComplete();
    });
    await();
  }

  @Test
  public void testFailingCall() {
    failingCallHandler((str, h) -> futureProxy.failingCall(str).onComplete(h));
  }

  @Test
  public void testErrorHandlingServiceExceptionSubclass() {
    vertx.eventBus().registerDefaultCodec(MyServiceException.class,
      new MyServiceExceptionMessageCodec());
    proxy.failingCall("Fail subclass", handler -> {
      assertTrue(handler.cause() instanceof MyServiceException);
      assertEquals("Call has failed", handler.cause().getMessage());
      assertEquals(25, ((MyServiceException) handler.cause()).failureCode());
      assertEquals("some extra", ((MyServiceException) handler.cause()).getExtra());
      testComplete();
    });
    await();

  }

  @Test
  public void testCauseErrorHandling() {
    proxyWithDebug.failingCall("Fail with cause", handler -> {
      assertTrue(handler.cause() instanceof ServiceException);
      ServiceException cause = (ServiceException) handler.cause();
      assertEquals("Failed!", handler.cause().getMessage());
      assertEquals(IllegalArgumentException.class.getCanonicalName(), cause.getDebugInfo().getString("causeName"));
      assertEquals("Failed!", cause.getDebugInfo().getString("causeMessage"));
      assertFalse(cause.getDebugInfo().getJsonArray("causeStackTrace").isEmpty());
      testComplete();
    });
    await();
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
  public void testBasicBoxedTypesNull() {
    proxy.basicBoxedTypesNull(null, null, null, null, null, null, null, null, null);
    await();
  }

  @Test
  public void testJsonTypes() {
    proxy.jsonTypes(new JsonObject().put("foo", "bar"), new JsonArray().add("wibble"));
    await();
  }

  @Test
  public void testJsonTypesNull() {
    proxy.jsonTypesNull(null, null);
    await();
  }

  @Test
  public void testEnumType() {
    proxy.enumType(SomeEnum.WIBBLE);
    await();
  }

  @Test
  public void testEnumTypeNull() {
    proxy.enumTypeNull(null);
    await();
  }

  @Test
  public void testEnumTypeAsResult() {
    enumTypeHandler(h -> proxy.enumTypeAsResult(h));
  }

  private void enumTypeHandler(Consumer<Handler<AsyncResult<SomeEnum>>> consumer) {
    consumer.accept(ar -> {
      if (ar.failed()) {
        fail("Failure not expected");
      } else {
        assertEquals(ar.result(), SomeEnum.WIBBLE);
      }
      testComplete();
    });
    await();
  }

  @Test
  public void testEnumTypeAsFuture() {
    enumTypeHandler(futureProxy.enumTypeAsResult()::onComplete);
  }

  @Test
  public void testEnumTypeAsResultWithNull() {
    enumTypeWithNullHandler(h -> proxy.enumTypeAsResultNull(h));
  }

  private void enumTypeWithNullHandler(Consumer<Handler<AsyncResult<SomeEnum>>> consumer) {
    consumer.accept(ar -> {
      if (ar.failed()) {
        fail("Failure not expected");
      } else {
        assertNull(ar.result());
      }
      testComplete();
    });
    await();
  }

  @Test
  public void testEnumTypeAsFutureWithNull() {
    enumTypeWithNullHandler(futureProxy.enumTypeAsResultNull()::onComplete);
  }

  @Test
  public void testDataObjectType() {
    proxy.dataObjectType(new TestDataObject().setString("foo").setNumber(123).setBool(true));
    await();
  }

  @Test
  public void testListdataObjectType() {
    List<TestDataObject> testDataList = Arrays.asList(
      new TestDataObject().setString("foo").setNumber(123).setBool(true),
      new TestDataObject().setString("bar").setNumber(456).setBool(false));
    proxy.listdataObjectType(testDataList);
    await();
  }

  @Test
  public void testSetdataObjectType() {
    Set<TestDataObject> testDataSet = new HashSet<>(Arrays.asList(
      new TestDataObject().setString("String foo").setNumber(123).setBool(true),
      new TestDataObject().setString("String bar").setNumber(456).setBool(false)));
    proxy.setdataObjectType(testDataSet);
    await();
  }

  @Test
  public void testMapdataObjectType() {
    Map<String, TestDataObject> map = new HashMap<>();
    map.put("do1", new TestDataObject().setNumber(1).setString("String 1").setBool(false));
    map.put("do2", new TestDataObject().setNumber(2).setString("String 2").setBool(true));
    proxy.mapDataObjectType(map);
    await();
  }

  @Test
  public void testDateTimeType() {
    proxy.dateTimeType(ZonedDateTime.parse("2019-03-25T17:08:31.069+01:00[Europe/Rome]"));
    await();
  }

  @Test
  public void testListDateTimeType() {
    proxy.listDateTimeType(
      Arrays.asList(
        ZonedDateTime.parse("2019-03-25T17:08:31.069+01:00[Europe/Rome]"),
        ZonedDateTime.parse("2019-03-25T17:08:31.069+01:00[Europe/Rome]").plusHours(1)
      )
    );
    await();
  }

  @Test
  public void testSetDateTimeType() {
    proxy.setDateTimeType(
      new HashSet<>(Arrays.asList(
        ZonedDateTime.parse("2019-03-25T17:08:31.069+01:00[Europe/Rome]"),
        ZonedDateTime.parse("2019-03-25T17:08:31.069+01:00[Europe/Rome]").plusHours(1)
      ))
    );
    await();
  }

  @Test
  public void testMapDateTimeType() {
    Map<String, ZonedDateTime> expected = new HashMap<>();
    expected.put("date1", ZonedDateTime.parse("2019-03-25T17:08:31.069+01:00[Europe/Rome]"));
    expected.put("date2", ZonedDateTime.parse("2019-03-25T17:08:31.069+01:00[Europe/Rome]").plusHours(1));
    proxy.mapDateTimeType(expected);
    await();
  }

  @Test
  public void testDataObjectTypeNull() {
    proxy.dataObjectTypeNull(null);
    await();
  }

  @Test
  public void testlistdataObjectTypeHavingNullValues() {
    List<TestDataObject> testDataList = Arrays.asList(
      new TestDataObject().setString("foo").setNumber(123).setBool(true),
      null,
      new TestDataObject().setString("bar").setNumber(456).setBool(false));
    proxy.listdataObjectTypeHavingNullValues(testDataList);
    await();
  }

  @Test
  public void testListDataObjectTypeNull() {
    proxy.listdataObjectTypeNull(null);
    await();
  }

  @Test
  public void testSetdataObjectTypeHavingNullValues() {
    Set<TestDataObject> testDataSet = new HashSet<>(Arrays.asList(
      new TestDataObject().setString("String foo").setNumber(123).setBool(true),
      null,
      new TestDataObject().setString("String bar").setNumber(456).setBool(false)));
    proxy.setdataObjectTypeHavingNullValues(testDataSet);
    await();
  }

  @Test
  public void testSetDataObjectTypeNull() {
    proxy.setdataObjectTypeNull(null);
    await();
  }

  @Test
  public void testListTypes() {
    proxy.listParams(Arrays.asList("foo", "bar"), Arrays.asList((byte) 12, (byte) 13), Arrays.asList((short) 123, (short) 134), Arrays.asList(1234, 1235),
      Arrays.asList(12345l, 12346l), Arrays.asList(new JsonObject().put("foo", "bar"), new JsonObject().put("blah", "eek")),
      Arrays.asList(new JsonArray().add("foo"), new JsonArray().add("blah")),
      Arrays.asList(new TestDataObject().setNumber(1).setString("String 1").setBool(false), new TestDataObject().setNumber(2).setString("String 2").setBool(true)));
    await();
  }

  @Test
  public void testSetTypes() {
    proxy.setParams(new HashSet<>(Arrays.asList("foo", "bar")), new HashSet<>(Arrays.asList((byte) 12, (byte) 13)), new HashSet<>(Arrays.asList((short) 123, (short) 134)),
      new HashSet<>(Arrays.asList(1234, 1235)),
      new HashSet<>(Arrays.asList(12345l, 12346l)), new HashSet<>(Arrays.asList(new JsonObject().put("foo", "bar"), new JsonObject().put("blah", "eek"))),
      new HashSet<>(Arrays.asList(new JsonArray().add("foo"), new JsonArray().add("blah"))),
      new HashSet<>(Arrays.asList(new TestDataObject().setNumber(1).setString("String 1").setBool(false), new TestDataObject().setNumber(2).setString("String 2").setBool(true))));
    await();
  }

  @Test
  public void testMapTypes() {
    proxy.mapParams(new HashMap<String, String>() {{
                      put("eek", "foo");
                      put("wob", "bar");
                    }},
      new HashMap<String, Byte>() {{
        put("eek", (byte) 12);
        put("wob", (byte) 13);
      }},
      new HashMap<String, Short>() {{
        put("eek", (short) 123);
        put("wob", (short) 134);
      }},
      new HashMap<String, Integer>() {{
        put("eek", 1234);
        put("wob", 1235);
      }},
      new HashMap<String, Long>() {{
        put("eek", 12345l);
        put("wob", 12356l);
      }},
      new HashMap<String, JsonObject>() {{
        put("eek", new JsonObject().put("foo", "bar"));
        put("wob", new JsonObject().put("blah", "eek"));
      }},
      new HashMap<String, JsonArray>() {{
        put("eek", new JsonArray().add("foo"));
        put("wob", new JsonArray().add("blah"));
      }});
    await();
  }

  @Test
  public void testStringHandler() {
    stringHandler(proxy::stringHandler);
  }

  private void stringHandler(Consumer<Handler<AsyncResult<String>>> consumer) {
    consumer.accept(onSuccess(res -> {
      assertEquals("foobar", res);
      testComplete();
    }));
    await();
  }

  @Test
  public void testStringFuture() {
    stringHandler(futureProxy.stringFuture()::onComplete);
  }

  @Test
  public void testStringNullHandler() {
    stringNullHandler(proxy::stringNullHandler);
  }

  private void stringNullHandler(Consumer<Handler<AsyncResult<String>>> consumer) {
    consumer.accept(onSuccess(res -> {
      assertNull(res);
      testComplete();
    }));
    await();
  }

  @Test
  public void testStringNullFuture() {
    stringNullHandler(futureProxy.stringNullFuture()::onComplete);
  }

  @Test
  public void testByteHandler() {
    byteHandler(proxy::byteHandler);
  }

  private void byteHandler(Consumer<Handler<AsyncResult<Byte>>> consumer) {
    consumer.accept(onSuccess(res -> {
      assertEquals(Byte.valueOf((byte) 123), res);
      testComplete();
    }));
    await();
  }

  @Test
  public void testByteFuture() {
    byteHandler(futureProxy.byteFuture()::onComplete);
  }

  @Test
  public void testByteNullHandler() {
    byteNullHandler(proxy::byteNullHandler);
  }

  private void byteNullHandler(Consumer<Handler<AsyncResult<Byte>>> consumer) {
    consumer.accept(onSuccess(res -> {
      assertNull(res);
      testComplete();
    }));
    await();
  }

  @Test
  public void testByteNullFuture() {
    byteNullHandler(futureProxy.byteNullFuture()::onComplete);
  }

  @Test
  public void testShortHandler() {
    shortHandler(proxy::shortHandler);
  }

  private void shortHandler(Consumer<Handler<AsyncResult<Short>>> consumer) {
    consumer.accept(onSuccess(res -> {
      assertEquals(Short.valueOf((short) 1234), res);
      testComplete();
    }));
    await();
  }

  @Test
  public void testShortFuture() {
    shortHandler(futureProxy.shortFuture()::onComplete);
  }

  @Test
  public void testShortNullHandler() {
    shortNullHandler(proxy::shortNullHandler);
  }

  private void shortNullHandler(Consumer<Handler<AsyncResult<Short>>> consumer) {
    consumer.accept(onSuccess(res -> {
      assertNull(res);
      testComplete();
    }));
    await();
  }

  @Test
  public void testShortNullFuture() {
    shortNullHandler(futureProxy.shortNullFuture()::onComplete);
  }

  @Test
  public void testIntHandler() {
    intHandler(proxy::intHandler);
  }

  private void intHandler(Consumer<Handler<AsyncResult<Integer>>> consumer) {
    consumer.accept(onSuccess(res -> {
      assertEquals(Integer.valueOf(12345), res);
      testComplete();
    }));
    await();
  }

  @Test
  public void testIntFuture() {
    intHandler(futureProxy.intFuture()::onComplete);
  }

  @Test
  public void testIntNullHandler() {
    intNullHandler(proxy::intNullHandler);
  }

  private void intNullHandler(Consumer<Handler<AsyncResult<Integer>>> consumer) {
    consumer.accept(onSuccess(res -> {
      assertNull(res);
      testComplete();
    }));
    await();
  }

  @Test
  public void testIntNullFuture() {
    intNullHandler(futureProxy.intNullFuture()::onComplete);
  }

  @Test
  public void testLongHandler() {
    longHandler(proxy::longHandler);
  }

  private void longHandler(Consumer<Handler<AsyncResult<Long>>> consumer) {
    consumer.accept(onSuccess(res -> {
      assertEquals(Long.valueOf(123456l), res);
      testComplete();
    }));
    await();
  }

  @Test
  public void testLongFuture() {
    longHandler(futureProxy.longFuture()::onComplete);
  }

  @Test
  public void testLongNullHandler() {
    longNullHandler(proxy::longNullHandler);
  }

  private void longNullHandler(Consumer<Handler<AsyncResult<Long>>> consumer) {
    consumer.accept(onSuccess(res -> {
      assertNull(res);
      testComplete();
    }));
    await();
  }

  @Test
  public void testLongNullFuture() {
    longNullHandler(futureProxy.longNullFuture()::onComplete);
  }

  @Test
  public void testFloatHandler() {
    floatHandler(proxy::floatHandler);
  }

  private void floatHandler(Consumer<Handler<AsyncResult<Float>>> consumer) {
    consumer.accept(onSuccess(res -> {
      assertEquals(Float.valueOf(12.34f), res);
      testComplete();
    }));
    await();
  }

  @Test
  public void testFloatFuture() {
    floatHandler(futureProxy.floatFuture()::onComplete);
  }

  @Test
  public void testFloatNullHandler() {
    floatNullHandler(proxy::floatNullHandler);
  }

  private void floatNullHandler(Consumer<Handler<AsyncResult<Float>>> consumer) {
    consumer.accept(onSuccess(res -> {
      assertNull(res);
      testComplete();
    }));
    await();
  }

  @Test
  public void testFloatNullFuture() {
    floatNullHandler(futureProxy.floatNullFuture()::onComplete);
  }

  @Test
  public void testDoubleHandler() {
    doubleHandler(proxy::doubleHandler);
  }

  private void doubleHandler(Consumer<Handler<AsyncResult<Double>>> consumer) {
    consumer.accept(onSuccess(res -> {
      assertEquals(Double.valueOf(12.3456d), res);
      testComplete();
    }));
    await();
  }

  @Test
  public void testDoubleFuture() {
    doubleHandler(futureProxy.doubleFuture()::onComplete);
  }

  @Test
  public void testDoubleNullHandler() {
    doubleNullHandler(proxy::doubleNullHandler);
  }

  private void doubleNullHandler(Consumer<Handler<AsyncResult<Double>>> consumer) {
    consumer.accept(onSuccess(res -> {
      assertNull(res);
      testComplete();
    }));
    await();
  }

  @Test
  public void testDoubleNullFuture() {
    doubleNullHandler(futureProxy.doubleNullFuture()::onComplete);
  }

  @Test
  public void testCharHandler() {
    charHandler(proxy::charHandler);
  }

  private void charHandler(Consumer<Handler<AsyncResult<Character>>> consumer) {
    consumer.accept(onSuccess(res -> {
      assertEquals(Character.valueOf('X'), res);
      testComplete();
    }));
    await();
  }

  @Test
  public void testCharFuture() {
    charHandler(futureProxy.charFuture()::onComplete);
  }

  @Test
  public void testCharNullHandler() {
    charNullHandler(proxy::charNullHandler);
  }

  private void charNullHandler(Consumer<Handler<AsyncResult<Character>>> consumer) {
    consumer.accept(onSuccess(res -> {
      assertNull(res);
      testComplete();
    }));
    await();
  }

  @Test
  public void testCharNullFuture() {
    charNullHandler(futureProxy.charNullFuture()::onComplete);
  }

  @Test
  public void testBooleanHandler() {
    booleanHandler(proxy::booleanHandler);
  }

  private void booleanHandler(Consumer<Handler<AsyncResult<Boolean>>> consumer) {
    consumer.accept(onSuccess(res -> {
      assertEquals(true, res);
      testComplete();
    }));
    await();
  }

  @Test
  public void testBooleanFuture() {
    booleanHandler(futureProxy.booleanFuture()::onComplete);
  }

  @Test
  public void testBooleanNullHandler() {
    booleanNullHandler(proxy::booleanNullHandler);
  }

  private void booleanNullHandler(Consumer<Handler<AsyncResult<Boolean>>> consumer) {
    consumer.accept(onSuccess(res -> {
      assertNull(res);
      testComplete();
    }));
    await();
  }

  @Test
  public void testBooleanNullFuture() {
    booleanNullHandler(futureProxy.booleanNullFuture()::onComplete);
  }

  @Test
  public void testJsonObjectHandler() {
    jsonObjectHandler(proxy::jsonObjectHandler);
  }

  private void jsonObjectHandler(Consumer<Handler<AsyncResult<JsonObject>>> consumer) {
    consumer.accept(onSuccess(res -> {
      assertEquals("wibble", res.getString("blah"));
      testComplete();
    }));
    await();
  }

  @Test
  public void testJsonObjectFuture() {
    jsonObjectHandler(futureProxy.jsonObjectFuture()::onComplete);
  }

  @Test
  public void testJsonObjectNullHandler() {
    jsonObjectNullHandler(proxy::jsonObjectNullHandler);
  }

  private void jsonObjectNullHandler(Consumer<Handler<AsyncResult<JsonObject>>> consumer) {
    consumer.accept(onSuccess(res -> {
      assertNull(res);
      testComplete();
    }));
    await();
  }

  @Test
  public void testJsonObjectNullFuture() {
    jsonObjectNullHandler(futureProxy.jsonObjectNullFuture()::onComplete);
  }

  @Test
  public void testJsonArrayHandler() {
    jsonArrayHandler(proxy::jsonArrayHandler);
  }

  private void jsonArrayHandler(Consumer<Handler<AsyncResult<JsonArray>>> consumer) {
    consumer.accept(onSuccess(res -> {
      assertEquals("blurrg", res.getString(0));
      testComplete();
    }));
    await();
  }

  @Test
  public void testJsonArrayFuture() {
    jsonArrayHandler(futureProxy.jsonArrayFuture()::onComplete);
  }

  @Test
  public void testJsonArrayNullHandler() {
    jsonArrayNullHandler(proxy::jsonArrayNullHandler);
  }

  private void jsonArrayNullHandler(Consumer<Handler<AsyncResult<JsonArray>>> consumer) {
    consumer.accept(onSuccess(res -> {
      assertNull(res);
      testComplete();
    }));
    await();
  }

  @Test
  public void testJsonArrayNullFuture() {
    jsonArrayNullHandler(futureProxy.jsonArrayNullFuture()::onComplete);
  }

  @Test
  public void testDataObjectHandler() {
    dataObjectHandler(proxy::dataObjectHandler);
  }

  private void dataObjectHandler(Consumer<Handler<AsyncResult<TestDataObject>>> consumer) {
    consumer.accept(onSuccess(res -> {
      assertEquals(new TestDataObject().setString("foo").setNumber(123).setBool(true), res);
      testComplete();
    }));
    await();
  }

  @Test
  public void testDataObjectFuture() {
    dataObjectHandler(futureProxy.dataObjectFuture()::onComplete);
  }

  @Test
  public void testDataObjectNullHandler() {
    dataObjectNullHandler(proxy::dataObjectNullHandler);
  }

  private void dataObjectNullHandler(Consumer<Handler<AsyncResult<TestDataObject>>> consumer) {
    consumer.accept(onSuccess(res -> {
      assertNull(res);
      testComplete();
    }));
    await();
  }

  @Test
  public void testDataObjectNullFuture() {
    dataObjectNullHandler(futureProxy.dataObjectNullFuture()::onComplete);
  }

  @Test
  public void testVoidHandler() {
    voidHandler(proxy::voidHandler);
  }

  private void voidHandler(Consumer<Handler<AsyncResult<Void>>> consumer) {
    consumer.accept(onSuccess(res -> {
      assertNull(res);
      testComplete();
    }));
    await();
  }

  @Test
  public void testVoidFuture() {
    voidHandler(futureProxy.voidFuture()::onComplete);
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
    vertx.eventBus().consumer("fluentReceived").handler(msg -> {
      testComplete();
    });
    assertSame(proxy, proxy.fluentNoParams());
    await();
  }

  @Test
  public void testFailingMethod() {
    failingMethodHandler(proxy::failingMethod);
  }

  private void failingMethodHandler(Consumer<Handler<AsyncResult<JsonObject>>> consumer) {
    consumer.accept(onFailure(t -> {
      assertTrue(t instanceof ReplyException);
      ServiceException se = (ServiceException) t;
      assertEquals(ReplyFailure.RECIPIENT_FAILURE, se.failureType());
      assertEquals("wibble", se.getMessage());
      assertTrue(se.getDebugInfo().isEmpty());
      testComplete();
    }));
    await();
  }

  @Test
  public void testFailingFuture() {
    failingMethodHandler(futureProxy.failingFuture()::onComplete);
  }

  @Test
  public void testCallWithMessage() {
    JsonObject message = new JsonObject();
    message.put("object", new JsonObject().put("foo", "bar"));
    message.put("str", "blah");
    message.put("i", 1234);
    message.put("chr", (int) 'X'); // chars are mapped to ints
    message.put("senum", SomeEnum.BAR.toString()); // enums are mapped to strings
    vertx.eventBus().<String>request("someaddress", message, new DeliveryOptions().addHeader("action", "invokeWithMessage"), onSuccess(res -> {
      assertEquals("goats", res.body());
      testComplete();
    }));
    await();
  }

  @Test
  public void testCallWithMessageInvalidAction() {
    JsonObject message = new JsonObject();
    message.put("object", new JsonObject().put("foo", "bar"));
    message.put("str", "blah");
    message.put("i", 1234);
    vertx.eventBus().request(SERVICE_WITH_DEBUG_ADDRESS, message, new DeliveryOptions().addHeader("action", "yourmum").setSendTimeout(500), onFailure(t -> {
      assertTrue(t instanceof ServiceException);
      ServiceException se = (ServiceException) t;
      // This will as operation will fail to be invoked
      assertEquals(ReplyFailure.RECIPIENT_FAILURE, se.failureType());
      assertEquals(IllegalStateException.class.getCanonicalName(), se.getDebugInfo().getString("causeName"));
      testComplete();
    }));
    await();
  }

  @Test
  public void testCallWithMessageParamWrongType() {
    JsonObject message = new JsonObject();
    message.put("object", new JsonObject().put("foo", "bar"));
    message.put("str", 76523); // <- wrong one
    message.put("i", 1234);
    message.put("char", (int) 'X'); // chars are mapped to ints
    message.put("enum", SomeEnum.BAR.toString()); // enums are mapped to strings
    vertx.eventBus().request(SERVICE_WITH_DEBUG_ADDRESS, message, new DeliveryOptions().addHeader("action", "invokeWithMessage").setSendTimeout(500), onFailure(t -> {
      assertTrue(t instanceof ServiceException);
      ServiceException se = (ServiceException) t;
      // This will as operation will fail to be invoked
      assertEquals(ReplyFailure.RECIPIENT_FAILURE, se.failureType());
      assertEquals(ClassCastException.class.getCanonicalName(), se.getDebugInfo().getString("causeName"));
      assertFalse(se.getDebugInfo().getJsonArray("causeStackTrace").isEmpty());
      testComplete();
    }));
    await();
  }

  @Test
  public void testListStringHandler() {
    listStringHandler(proxy::listStringHandler);
  }

  private void listStringHandler(Consumer<Handler<AsyncResult<List<String>>>> consumer) {
    consumer.accept(onSuccess(list -> {
      assertEquals("foo", list.get(0));
      assertEquals("bar", list.get(1));
      assertEquals("wibble", list.get(2));
      testComplete();
    }));
    await();
  }

  @Test
  public void testListStringFuture() {
    listStringHandler(futureProxy.listStringFuture()::onComplete);
  }

  @Test
  public void testListByteHandler() {
    listByteHandler(proxy::listByteHandler);
  }

  private void listByteHandler(Consumer<Handler<AsyncResult<List<Byte>>>> consumer) {
    consumer.accept(onSuccess(list -> {
      assertEquals(Byte.valueOf((byte) 1), list.get(0));
      assertEquals(Byte.valueOf((byte) 2), list.get(1));
      assertEquals(Byte.valueOf((byte) 3), list.get(2));
      testComplete();
    }));
    await();
  }

  @Test
  public void testListByteFuture() {
    listByteHandler(futureProxy.listByteFuture()::onComplete);
  }

  @Test
  public void testListShortHandler() {
    listShortHandler(proxy::listShortHandler);
  }

  private void listShortHandler(Consumer<Handler<AsyncResult<List<Short>>>> consumer) {
    consumer.accept(onSuccess(list -> {
      assertEquals(Short.valueOf((short) 11), list.get(0));
      assertEquals(Short.valueOf((short) 12), list.get(1));
      assertEquals(Short.valueOf((short) 13), list.get(2));
      testComplete();
    }));
    await();
  }

  @Test
  public void testListShortFuture() {
    listShortHandler(futureProxy.listShortFuture()::onComplete);
  }

  @Test
  public void testListIntHandler() {
    listIntHandler(proxy::listIntHandler);
  }

  private void listIntHandler(Consumer<Handler<AsyncResult<List<Integer>>>> consumer) {
    consumer.accept(onSuccess(list -> {
      assertEquals(100, list.get(0).intValue());
      assertEquals(101, list.get(1).intValue());
      assertEquals(102, list.get(2).intValue());
      testComplete();
    }));
    await();
  }

  @Test
  public void testListIntFuture() {
    listIntHandler(futureProxy.listIntFuture()::onComplete);
  }

  @Test
  public void testListLongHandler() {
    listLongHandler(proxy::listLongHandler);
  }

  private void listLongHandler(Consumer<Handler<AsyncResult<List<Long>>>> consumer) {
    consumer.accept(onSuccess(list -> {
      assertEquals(1000L, list.get(0).longValue());
      assertEquals(1001L, list.get(1).longValue());
      assertEquals(1002L, list.get(2).longValue());
      testComplete();
    }));
    await();
  }

  @Test
  public void testListLongFuture() {
    listLongHandler(futureProxy.listLongFuture()::onComplete);
  }

  @Test
  public void testListFloatHandler() {
    listFloatHandler(proxy::listFloatHandler);
  }

  private void listFloatHandler(Consumer<Handler<AsyncResult<List<Float>>>> consumer) {
    consumer.accept(onSuccess(list -> {
      assertEquals(1.1f, list.get(0), 0);
      assertEquals(1.2f, list.get(1), 0);
      assertEquals(1.3f, list.get(2), 0);
      testComplete();
    }));
    await();
  }

  @Test
  public void testListFloatFuture() {
    listFloatHandler(futureProxy.listFloatFuture()::onComplete);
  }

  @Test
  public void testListDoubleHandler() {
    listDoubleHandler(proxy::listDoubleHandler);
  }

  private void listDoubleHandler(Consumer<Handler<AsyncResult<List<Double>>>> consumer) {
    consumer.accept(onSuccess(list -> {
      assertEquals(1.11d, list.get(0), 0);
      assertEquals(1.12d, list.get(1), 0);
      assertEquals(1.13d, list.get(2), 0);
      testComplete();
    }));
    await();
  }

  @Test
  public void testListDoubleFuture() {
    listDoubleHandler(futureProxy.listDoubleFuture()::onComplete);
  }

  @Test
  public void testListCharHandler() {
    listCharHandler(proxy::listCharHandler);
  }

  private void listCharHandler(Consumer<Handler<AsyncResult<List<Character>>>> consumer) {
    consumer.accept(onSuccess(list -> {
      assertEquals('X', list.get(0).charValue());
      assertEquals('Y', list.get(1).charValue());
      assertEquals('Z', list.get(2).charValue());
      testComplete();
    }));
    await();
  }

  @Test
  public void testListCharFuture() {
    listCharHandler(futureProxy.listCharFuture()::onComplete);
  }

  @Test
  public void testListBoolHandler() {
    listBoolHandler(proxy::listBoolHandler);
  }

  private void listBoolHandler(Consumer<Handler<AsyncResult<List<Boolean>>>> consumer) {
    consumer.accept(onSuccess(list -> {
      assertEquals(true, list.get(0));
      assertEquals(false, list.get(1));
      assertEquals(true, list.get(2));
      testComplete();
    }));
    await();
  }

  @Test
  public void testListBoolFuture() {
    listBoolHandler(futureProxy.listBoolFuture()::onComplete);
  }

  @Test
  public void testListJsonObjectHandler() {
    listJsonObjectHandler(proxy::listJsonObjectHandler);
  }

  private void listJsonObjectHandler(Consumer<Handler<AsyncResult<List<JsonObject>>>> consumer) {
    consumer.accept(onSuccess(list -> {
      assertEquals("foo", list.get(0).getString("a"));
      assertEquals("bar", list.get(1).getString("b"));
      assertEquals("wibble", list.get(2).getString("c"));
      testComplete();
    }));
    await();
  }

  @Test
  public void testListJsonObjectFuture() {
    listJsonObjectHandler(futureProxy.listJsonObjectFuture()::onComplete);
  }

  @Test
  public void testListJsonArrayHandler() {
    listJsonArrayHandler(proxy::listJsonArrayHandler);
  }

  private void listJsonArrayHandler(Consumer<Handler<AsyncResult<List<JsonArray>>>> consumer) {
    consumer.accept(onSuccess(list -> {
      assertEquals("foo", list.get(0).getString(0));
      assertEquals("bar", list.get(1).getString(0));
      assertEquals("wibble", list.get(2).getString(0));
      testComplete();
    }));
    await();
  }

  @Test
  public void testListJsonArrayFuture() {
    listJsonArrayHandler(futureProxy.listJsonArrayFuture()::onComplete);
  }

  @Test
  public void testSetStringHandler() {
    setStringHandler(proxy::setStringHandler);
  }

  private void setStringHandler(Consumer<Handler<AsyncResult<Set<String>>>> consumer) {
    consumer.accept(onSuccess(set -> {
      assertTrue(set.contains("foo"));
      assertTrue(set.contains("bar"));
      assertTrue(set.contains("wibble"));
      testComplete();
    }));
    await();
  }

  @Test
  public void testSetStringFuture() {
    setStringHandler(futureProxy.setStringFuture()::onComplete);
  }

  @Test
  public void testSetByteHandler() {
    setByteHandler(proxy::setByteHandler);
  }

  private void setByteHandler(Consumer<Handler<AsyncResult<Set<Byte>>>> consumer) {
    consumer.accept(onSuccess(set -> {
      assertTrue(set.contains((byte) 1));
      assertTrue(set.contains((byte) 2));
      assertTrue(set.contains((byte) 3));
      testComplete();
    }));
    await();
  }

  @Test
  public void testSetByteFuture() {
    setByteHandler(futureProxy.setByteFuture()::onComplete);
  }

  @Test
  public void testSetShortHandler() {
    setShortHandler(proxy::setShortHandler);

  }

  private void setShortHandler(Consumer<Handler<AsyncResult<Set<Short>>>> consumer) {
    consumer.accept(onSuccess(set -> {
      assertTrue(set.contains((short) 11));
      assertTrue(set.contains((short) 12));
      assertTrue(set.contains((short) 13));
      testComplete();
    }));
    await();
  }

  @Test
  public void testSetShortFuture() {
    setShortHandler(futureProxy.setShortFuture()::onComplete);
  }

  @Test
  public void testSetIntHandler() {
    setIntHandler(proxy::setIntHandler);
  }

  private void setIntHandler(Consumer<Handler<AsyncResult<Set<Integer>>>> consumer) {
    consumer.accept(onSuccess(set -> {
      assertTrue(set.contains(100));
      assertTrue(set.contains(101));
      assertTrue(set.contains(102));
      testComplete();
    }));
    await();
  }

  @Test
  public void testSetIntFuture() {
    setIntHandler(futureProxy.setIntFuture()::onComplete);
  }

  @Test
  public void testSetLongHandler() {
    setLongHandler(proxy::setLongHandler);
  }

  private void setLongHandler(Consumer<Handler<AsyncResult<Set<Long>>>> consumer) {
    consumer.accept(onSuccess(set -> {
      assertTrue(set.contains(1000l));
      assertTrue(set.contains(1001l));
      assertTrue(set.contains(1002l));
      testComplete();
    }));
    await();
  }

  @Test
  public void testSetLongFuture() {
    setLongHandler(futureProxy.setLongFuture()::onComplete);
  }

  @Test
  public void testSetFloatHandler() {
    setFloatHandler(proxy::setFloatHandler);
  }

  private void setFloatHandler(Consumer<Handler<AsyncResult<Set<Float>>>> consumer) {
    consumer.accept(onSuccess(set -> {
      assertTrue(set.contains(1.1f));
      assertTrue(set.contains(1.2f));
      assertTrue(set.contains(1.3f));
      testComplete();
    }));
    await();
  }

  @Test
  public void testSetFloatFuture() {
    setFloatHandler(futureProxy.setFloatFuture()::onComplete);
  }

  @Test
  public void testSetDoubleHandler() {
    setDoubleHandler(proxy::setDoubleHandler);
  }

  private void setDoubleHandler(Consumer<Handler<AsyncResult<Set<Double>>>> consumer) {
    consumer.accept(onSuccess(set -> {
      assertTrue(set.contains(1.11d));
      assertTrue(set.contains(1.12d));
      assertTrue(set.contains(1.13d));
      testComplete();
    }));
    await();
  }

  @Test
  public void testSetDoubleFuture() {
    setDoubleHandler(futureProxy.setDoubleFuture()::onComplete);
  }

  @Test
  public void testSetCharHandler() {
    setCharHandler(proxy::setCharHandler);
  }

  private void setCharHandler(Consumer<Handler<AsyncResult<Set<Character>>>> consumer) {
    consumer.accept(onSuccess(set -> {
      assertTrue(set.contains('X'));
      assertTrue(set.contains('Y'));
      assertTrue(set.contains('Z'));
      testComplete();
    }));
    await();
  }

  @Test
  public void testSetCharFuture() {
    setCharHandler(futureProxy.setCharFuture()::onComplete);
  }

  @Test
  public void testSetBoolHandler() {
    setBoolHandler(proxy::setBoolHandler);
  }

  private void setBoolHandler(Consumer<Handler<AsyncResult<Set<Boolean>>>> consumer) {
    consumer.accept(onSuccess(set -> {
      assertTrue(set.contains(true));
      assertTrue(set.contains(false));
      testComplete();
    }));
    await();
  }

  @Test
  public void testSetBoolFuture() {
    setBoolHandler(futureProxy.setBoolFuture()::onComplete);
  }

  @Test
  public void testMapStringHandler() {
    mapStringHandler(proxy::mapStringHandler);
  }

  private void mapStringHandler(Consumer<Handler<AsyncResult<Map<String, String>>>> consumer) {
    Map<String, String> expected = new HashMap<>();
    expected.put("1", "foo");
    expected.put("2", "bar");
    expected.put("3", "wibble");
    consumer.accept(onSuccess(map -> {
      assertEquals(expected, map);
      testComplete();
    }));
    await();
  }

  @Test
  public void testMapStringFuture() {
    mapStringHandler(futureProxy.mapStringFuture()::onComplete);
  }

  @Test
  public void testMapByteHandler() {
    mapByteHandler(proxy::mapByteHandler);
  }

  private void mapByteHandler(Consumer<Handler<AsyncResult<Map<String, Byte>>>> consumer) {
    Map<String, Byte> expected = new HashMap<>();
    expected.put("1", (byte) 1);
    expected.put("2", (byte) 2);
    expected.put("3", (byte) 3);
    consumer.accept(onSuccess(map -> {
      assertEquals(expected, map);
      testComplete();
    }));
    await();
  }

  @Test
  public void testMapByteFuture() {
    mapByteHandler(futureProxy.mapByteFuture()::onComplete);
  }

  @Test
  public void testMapShortHandler() {
    mapShortHandler(proxy::mapShortHandler);
  }

  private void mapShortHandler(Consumer<Handler<AsyncResult<Map<String, Short>>>> consumer) {
    Map<String, Short> expected = new HashMap<>();
    expected.put("1", (short) 11);
    expected.put("2", (short) 12);
    expected.put("3", (short) 13);
    consumer.accept(onSuccess(map -> {
      assertEquals(expected, map);
      testComplete();
    }));
    await();
  }

  @Test
  public void testMapShortFuture() {
    mapShortHandler(futureProxy.mapShortFuture()::onComplete);
  }

  @Test
  public void testMapIntHandler() {
    mapIntHandler(proxy::mapIntHandler);
  }

  private void mapIntHandler(Consumer<Handler<AsyncResult<Map<String, Integer>>>> consumer) {
    Map<String, Integer> expected = new HashMap<>();
    expected.put("1", 100);
    expected.put("2", 101);
    expected.put("3", 102);
    consumer.accept(onSuccess(map -> {
      assertEquals(expected, map);
      testComplete();
    }));
    await();
  }

  @Test
  public void testMapIntegerFuture() {
    mapIntHandler(futureProxy.mapIntFuture()::onComplete);
  }

  @Test
  public void testMapLongHandler() {
    mapLongHandler(proxy::mapLongHandler);
  }

  private void mapLongHandler(Consumer<Handler<AsyncResult<Map<String, Long>>>> consumer) {
    Map<String, Long> expected = new HashMap<>();
    expected.put("1", 1000L);
    expected.put("2", 1001L);
    expected.put("3", 1002L);
    consumer.accept(onSuccess(map -> {
      assertEquals(expected, map);
      testComplete();
    }));
    await();
  }

  @Test
  public void testMapLongFuture() {
    mapLongHandler(futureProxy.mapLongFuture()::onComplete);
  }

  @Test
  public void testMapFloatHandler() {
    mapFloatHandler(proxy::mapFloatHandler);
  }

  private void mapFloatHandler(Consumer<Handler<AsyncResult<Map<String, Float>>>> consumer) {
    Map<String, Float> expected = new HashMap<>();
    expected.put("1", 1.1f);
    expected.put("2", 1.2f);
    expected.put("3", 1.3f);
    consumer.accept(onSuccess(map -> {
      assertEquals(expected, map);
      testComplete();
    }));
    await();
  }

  @Test
  public void testMapFloatFuture() {
    mapFloatHandler(futureProxy.mapFloatFuture()::onComplete);
  }

  @Test
  public void testMapDoubleHandler() {
    mapDoubleHandler(proxy::mapDoubleHandler);
  }

  private void mapDoubleHandler(Consumer<Handler<AsyncResult<Map<String, Double>>>> consumer) {
    Map<String, Double> expected = new HashMap<>();
    expected.put("1", 1.11d);
    expected.put("2", 1.12d);
    expected.put("3", 1.13d);
    consumer.accept(onSuccess(map -> {
      assertEquals(expected, map);
      testComplete();
    }));
    await();
  }

  @Test
  public void testMapDoubleFuture() {
    mapDoubleHandler(futureProxy.mapDoubleFuture()::onComplete);
  }

  @Test
  public void testMapCharHandler() {
    mapCharHandler(proxy::mapCharHandler);
  }

  private void mapCharHandler(Consumer<Handler<AsyncResult<Map<String, Character>>>> consumer) {
    Map<String, Character> expected = new HashMap<>();
    expected.put("1", 'X');
    expected.put("2", 'Y');
    expected.put("3", 'Z');
    consumer.accept(onSuccess(map -> {
      assertEquals(expected, map);
      testComplete();
    }));
    await();
  }

  @Test
  public void testMapCharacterFuture() {
    mapCharHandler(futureProxy.mapCharFuture()::onComplete);
  }

  @Test
  public void testMapBoolHandler() {
    mapBooleanHandler(proxy::mapBoolHandler);
  }

  private void mapBooleanHandler(Consumer<Handler<AsyncResult<Map<String, Boolean>>>> consumer) {
    Map<String, Boolean> expected = new HashMap<>();
    expected.put("1", true);
    expected.put("2", false);
    expected.put("3", true);
    consumer.accept(onSuccess(map -> {
      assertEquals(expected, map);
      testComplete();
    }));
    await();
  }

  @Test
  public void testMapBooleanFuture() {
    mapBooleanHandler(futureProxy.mapBoolFuture()::onComplete);
  }

  @Test
  public void testSetJsonObjectHandler() {
    setJsonObjectHandler(proxy::setJsonObjectHandler);
  }

  private void setJsonObjectHandler(Consumer<Handler<AsyncResult<Set<JsonObject>>>> consumer) {
    consumer.accept(onSuccess(set -> {
      assertTrue(set.contains(new JsonObject().put("a", "foo")));
      assertTrue(set.contains(new JsonObject().put("b", "bar")));
      assertTrue(set.contains(new JsonObject().put("c", "wibble")));
      testComplete();
    }));
    await();
  }

  @Test
  public void testSetJsonObjectFuture() {
    setJsonObjectHandler(futureProxy.setJsonObjectFuture()::onComplete);
  }

  @Test
  public void testMapJsonObjectHandler() {
    mapJsonObjectHandler(proxy::mapJsonObjectHandler);
  }

  private void mapJsonObjectHandler(Consumer<Handler<AsyncResult<Map<String, JsonObject>>>> consumer) {
    Map<String, JsonObject> expected = new HashMap<>();
    expected.put("1", new JsonObject().put("a", "foo"));
    expected.put("2", new JsonObject().put("b", "bar"));
    expected.put("3", new JsonObject().put("c", "wibble"));
    consumer.accept(onSuccess(map -> {
      assertEquals(expected, map);
      testComplete();
    }));
    await();
  }

  @Test
  public void testMapJsonObjectFuture() {
    mapJsonObjectHandler(futureProxy.mapJsonObjectFuture()::onComplete);
  }

  @Test
  public void setSetJsonArrayHandler() {
    setJsonArrayHandler(proxy::setJsonArrayHandler);
  }

  private void setJsonArrayHandler(Consumer<Handler<AsyncResult<Set<JsonArray>>>> consumer) {
    consumer.accept(onSuccess(set -> {
      assertTrue(set.contains(new JsonArray().add("foo")));
      assertTrue(set.contains(new JsonArray().add("bar")));
      assertTrue(set.contains(new JsonArray().add("wibble")));
      testComplete();
    }));
    await();
  }

  @Test
  public void testSetJsonArrayFuture() {
    setJsonArrayHandler(futureProxy.setJsonArrayFuture()::onComplete);
  }

  @Test
  public void testMapJsonArrayHandler() {
    mapJsonArrayHandler(proxy::mapJsonArrayHandler);
  }

  private void mapJsonArrayHandler(Consumer<Handler<AsyncResult<Map<String, JsonArray>>>> consumer) {
    Map<String, JsonArray> expected = new HashMap<>();
    expected.put("1", new JsonArray().add("foo"));
    expected.put("2", new JsonArray().add("bar"));
    expected.put("3", new JsonArray().add("wibble"));
    consumer.accept(onSuccess(map -> {
      assertEquals(expected, map);
      testComplete();
    }));
    await();
  }

  @Test
  public void testMapJsonArrayFuture() {
    mapJsonArrayHandler(futureProxy.mapJsonArrayFuture()::onComplete);
  }

  @Test
  public void testListDataObjectHandler() {
    listDataObjectHandler(proxy::listDataObjectHandler);
  }

  private void listDataObjectHandler(Consumer<Handler<AsyncResult<List<TestDataObject>>>> consumer) {
    consumer.accept(onSuccess(list -> {
      assertEquals(1, list.get(0).getNumber());
      assertEquals("String 1", list.get(0).getString());
      assertEquals(false, list.get(0).isBool());
      assertEquals(2, list.get(1).getNumber());
      assertEquals("String 2", list.get(1).getString());
      assertEquals(true, list.get(1).isBool());
      testComplete();
    }));
    await();
  }

  @Test
  public void testListDataObjectFuture() {
    listDataObjectHandler(futureProxy.listDataObjectFuture()::onComplete);
  }

  @Test
  public void testSetDataObjectHandler() {
    setDataObjectHandler(proxy::setDataObjectHandler);
  }

  private void setDataObjectHandler(Consumer<Handler<AsyncResult<Set<TestDataObject>>>> consumer) {
    consumer.accept(onSuccess(set -> {
      Set<JsonObject> setJson = set.stream().map(d -> d.toJson()).collect(Collectors.toSet());
      assertEquals(2, setJson.size());
      assertTrue(setJson.contains(new JsonObject().put("number", 1).put("string", "String 1").put("bool", false)));
      assertTrue(setJson.contains(new JsonObject().put("number", 2).put("string", "String 2").put("bool", true)));
      testComplete();
    }));
    await();
  }

  @Test
  public void testSetDataObjectFuture() {
    setDataObjectHandler(futureProxy.setDataObjectFuture()::onComplete);
  }

  @Test
  public void testMapDataObjectHandler() {
    mapDataObjectHandler(proxy::mapDataObject);
  }

  private void mapDataObjectHandler(Consumer<Handler<AsyncResult<Map<String, TestDataObject>>>> consumer) {
    Map<String, TestDataObject> expected = new HashMap<>();
    expected.put("do1", new TestDataObject().setNumber(1).setString("String 1").setBool(false));
    expected.put("do2", new TestDataObject().setNumber(2).setString("String 2").setBool(true));
    consumer.accept(onSuccess(map -> {
      assertEquals(expected, map);
      testComplete();
    }));
    await();
  }

  @Test
  public void testMapDataObjectFuture() {
    mapDataObjectHandler(futureProxy.mapDataObjectFuture()::onComplete);
  }

  @Test
  public void testDateTimeHandler() {
    zonedDateTimeHandler(proxy::zonedDateTimeHandler);
  }

  private void zonedDateTimeHandler(Consumer<Handler<AsyncResult<ZonedDateTime>>> consumer) {
    consumer.accept(onSuccess(dateTime -> {
      assertEquals(ZonedDateTime.parse("2019-03-25T17:08:31.069+01:00[Europe/Rome]"), dateTime);
      testComplete();
    }));
    await();
  }

  @Test
  public void testDateTimeFuture() {
    zonedDateTimeHandler(futureProxy.zonedDateTimeFuture()::onComplete);
  }

  @Test
  public void testListDateTimeHandler() {
    listZonedDateTimeHandler(proxy::listZonedDateTimeHandler);
  }

  private void listZonedDateTimeHandler(Consumer<Handler<AsyncResult<List<ZonedDateTime>>>> consumer) {
    consumer.accept(onSuccess(list -> {
      assertEquals(Arrays.asList(
        ZonedDateTime.parse("2019-03-25T17:08:31.069+01:00[Europe/Rome]"),
        ZonedDateTime.parse("2019-03-25T17:08:31.069+01:00[Europe/Rome]").plusHours(1)
      ), list);
      testComplete();
    }));
    await();
  }

  @Test
  public void testListDateTimeFuture() {
    listZonedDateTimeHandler(futureProxy.listZonedDateTimeFuture()::onComplete);
  }

  @Test
  public void testSetDateTimeHandler() {
    setZonedDateTimeHandler(proxy::setZonedDateTimeHandler);
  }

  private void setZonedDateTimeHandler(Consumer<Handler<AsyncResult<Set<ZonedDateTime>>>> consumer) {
    consumer.accept(onSuccess(set -> {
      assertEquals(new HashSet<>(Arrays.asList(
        ZonedDateTime.parse("2019-03-25T17:08:31.069+01:00[Europe/Rome]"),
        ZonedDateTime.parse("2019-03-25T17:08:31.069+01:00[Europe/Rome]").plusHours(1)
      )), set);
      testComplete();
    }));
    await();
  }

  @Test
  public void testSetDateTimeFuture() {
    setZonedDateTimeHandler(futureProxy.setZonedDateTimeFuture()::onComplete);
  }

  @Test
  public void testMapDateTimeHandler() {
    mapZonedDateTimeHandler(proxy::mapZonedDateTimeHandler);
  }

  private void mapZonedDateTimeHandler(Consumer<Handler<AsyncResult<Map<String, ZonedDateTime>>>> consumer) {
    Map<String, ZonedDateTime> expected = new HashMap<>();
    expected.put("date1", ZonedDateTime.parse("2019-03-25T17:08:31.069+01:00[Europe/Rome]"));
    expected.put("date2", ZonedDateTime.parse("2019-03-25T17:08:31.069+01:00[Europe/Rome]").plusHours(1));
    consumer.accept(onSuccess(map -> {
      assertEquals(expected, map);
      testComplete();
    }));
    await();
  }

  @Test
  public void testMapDateTimeFuture() {
    mapZonedDateTimeHandler(futureProxy.mapZonedDateTimeFuture()::onComplete);
  }

  @Test
  public void testProxyIgnore() {
    proxy.ignoredMethod();
    vertx.setTimer(500, id -> testComplete());
    await();
  }

  @Test
  public void testConnection() {
    connectionHandler((str, h) -> proxy.createConnection(str, h));
  }

  private void connectionHandler(BiConsumer<String, Handler<AsyncResult<TestConnection>>> consumer) {
    consumer.accept("foo", onSuccess(conn -> {
      conn.startTransaction(onSuccess(res -> {
        assertEquals("foo", res);
      }));
      conn.insert("blah", new JsonObject(), onSuccess(res -> {
        assertEquals("foo", res);
      }));
      conn.commit(onSuccess(res -> {
        assertEquals("foo", res);
      }));
      vertx.eventBus().consumer("closeCalled").handler(msg -> {
        assertEquals("blah", msg.body());
        testComplete();
      });
      conn.close();

      conn.startTransaction(onFailure(cause -> {
        assertNotNull(cause);
        assertTrue(cause instanceof IllegalStateException);
      }));
    }));
    await();
  }

  @Test
  public void testConnectionReturnFuture() {
    connectionHandler((str, h) -> futureProxy.createConnection(str).onComplete(h));
  }

  @Test
  public void testConnectionTimeout() {

    consumer.unregister();
    consumer = new ServiceBinder(vertx)
      .setAddress(SERVICE_ADDRESS)
      .setTimeoutSeconds(2)
      .register(TestService.class, service);

    checkConnection(proxy, 2L);

    await();
  }

  @Test
  public void testLocalServiceConnectionTimeout() {

    localConsumer.unregister();
    localConsumer = new ServiceBinder(vertx)
      .setAddress(SERVICE_LOCAL_ADDRESS)
      .setTimeoutSeconds(2)
      .register(TestService.class, localService);

    checkConnection(localProxy, 2L);

    await();
  }

  @Test
  public void testLongDelivery1() {
    TestService proxyLong = TestService.createProxyLongDelivery(vertx, SERVICE_ADDRESS);
    longDeliveryHandler1(proxyLong::longDeliverySuccess);
  }

  private void longDeliveryHandler1(Consumer<Handler<AsyncResult<String>>> consumer) {
    consumer.accept(onSuccess(str -> {
      assertEquals("blah", str);
      testComplete();
    }));
    await();
  }

  @Test
  public void testLongDeliveryReturnFuture1() {
    TestFutureService proxyLong = TestFutureService.createProxyLongDelivery(vertx, FUTURE_SERVICE_ADDRESS);
    longDeliveryHandler1(proxyLong.longDeliverySuccess()::onComplete);
  }

  @Test
  public void testLongDelivery2() {
    TestService proxyLong = TestService.createProxyLongDelivery(vertx, SERVICE_ADDRESS);
    longDeliveryHandler2(proxyLong::longDeliveryFailed);
  }

  private void longDeliveryHandler2(Consumer<Handler<AsyncResult<String>>> consumer) {
    consumer.accept(onFailure(t -> {
      assertNotNull(t);
      assertTrue(t instanceof ReplyException);
      assertFalse(t instanceof ServiceException);
      ReplyException re = (ReplyException) t;
      assertEquals(ReplyFailure.TIMEOUT, re.failureType());
      testComplete();
    }));
    await();
  }

  @Test
  public void testLongDeliveryReturnFuture2() {
    TestFutureService proxyLong = TestFutureService.createProxyLongDelivery(vertx, FUTURE_SERVICE_ADDRESS);
    longDeliveryHandler2(proxyLong.longDeliveryFailed()::onComplete);
  }

  @Test
  public void testUnregisteringTheService() {
    proxy.booleanHandler(ar -> {
      consumer.unregister(ar1 -> {
        if (ar1.failed()) fail(ar1.cause());
        else testComplete();
      });
    });
    await();

    AtomicReference<Throwable> caughtError = new AtomicReference<>();
    proxy.booleanHandler(ar -> {
      caughtError.set(ar.cause());
    });
    assertWaitUntil(() -> caughtError.get() != null);
  }

  @Test
  public void testAListContainingNullValues() {
    listDataObjectContainingNullHandler(proxy::listDataObjectContainingNullHandler);
  }

  private void listDataObjectContainingNullHandler(Consumer<Handler<AsyncResult<List<TestDataObject>>>> consumer) {
    consumer.accept(onSuccess(list -> {
      // Entry 1
      assertEquals(1, list.get(0).getNumber());
      assertEquals("String 1", list.get(0).getString());
      assertEquals(false, list.get(0).isBool());

      // Entry 2 is null
      assertNull(list.get(1));

      // Entry 3
      assertEquals(2, list.get(2).getNumber());
      assertEquals("String 2", list.get(2).getString());
      assertEquals(true, list.get(2).isBool());

      testComplete();
    }));
    await();
  }

  @Test
  public void testListDataObjectContainingNullFuture() {
    listDataObjectContainingNullHandler(futureProxy.listDataObjectContainingNullFuture()::onComplete);
  }

  @Test
  public void testASetContainingNullValues() {
    setDataObjectContainingNullHandler(proxy::setDataObjectContainingNullHandler);
  }

  private void setDataObjectContainingNullHandler(Consumer<Handler<AsyncResult<Set<TestDataObject>>>> consumer) {
    consumer.accept(onSuccess(set -> {
      AtomicInteger countNull = new AtomicInteger();
      AtomicInteger countNotNull = new AtomicInteger();
      set.forEach(t -> {
        if (t == null) {
          countNull.incrementAndGet();
        } else {
          countNotNull.incrementAndGet();
        }
      });

      assertEquals(2, countNotNull.get());
      assertEquals(1, countNull.get());

      testComplete();
    }));
    await();
  }

  @Test
  public void testSetDataObjectContainingNullFuture() {
    setDataObjectContainingNullHandler(futureProxy.setDataObjectContainingNullFuture()::onComplete);
  }

  @Test
  public void testLocalServiceFromLocalSender() {
    localProxy.noParams();
    await();
  }

  private void checkConnection(TestService proxy, long timeoutSeconds) {
    proxy.createConnection("foo", onSuccess(conn -> {
      long start = System.nanoTime();

      conn.startTransaction(onSuccess(res -> {
        assertEquals("foo", res);

        vertx.eventBus().consumer("closeCalled").handler(msg -> {
          assertEquals("blah", msg.body());

          long duration = System.nanoTime() - start;
          assertTrue(String.valueOf(duration), duration >= SECONDS.toNanos(timeoutSeconds));

          // Should be closed now
          conn.startTransaction(onFailure(this::checkCause));
        });

      }));
    }));
  }

  private void checkCause(Throwable cause) {
    assertNotNull(cause);
    assertTrue(cause instanceof ReplyException);
    assertFalse(cause instanceof ServiceException);
    ReplyException re = (ReplyException) cause;
    assertEquals(ReplyFailure.NO_HANDLERS, re.failureType());
    testComplete();
  }
}
