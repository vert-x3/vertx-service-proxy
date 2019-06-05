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

import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.junit.Test;

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
import io.vertx.test.core.VertxTestBase;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author lalitrao
 */
public class ServiceProxyTest extends VertxTestBase {

  public final static String SERVICE_ADDRESS = "someaddress";
  public final static String SERVICE_WITH_DEBUG_ADDRESS = "someaddressdebug";
  public final static String SERVICE_LOCAL_ADDRESS = "someaddress.local";
  public final static String TEST_ADDRESS = "testaddress";

  MessageConsumer<JsonObject> consumer, localConsumer, consumerWithDebugEnabled;
  TestService service, localService;
  TestService proxy, localProxy, proxyWithDebug;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    service = TestService.create(vertx);
    localService = TestService.create(vertx);
    
    consumer = new ServiceBinder(vertx).setAddress(SERVICE_ADDRESS)
      .register(TestService.class, service);
    consumerWithDebugEnabled = new ServiceBinder(vertx)
      .setAddress(SERVICE_WITH_DEBUG_ADDRESS)
      .setIncludeDebugInfo(true)
      .register(TestService.class, service);
    localConsumer = new ServiceBinder(vertx).setAddress(SERVICE_LOCAL_ADDRESS)
      .registerLocal(TestService.class, localService);

    proxy = TestService.createProxy(vertx, SERVICE_ADDRESS);
    localProxy = TestService.createProxy(vertx, SERVICE_LOCAL_ADDRESS);
    proxyWithDebug = TestService.createProxy(vertx, SERVICE_WITH_DEBUG_ADDRESS);
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
    proxy.failingCall("Fail", handler -> {
      assertTrue(handler.cause() instanceof ServiceException);
      assertEquals("Call has failed", handler.cause().getMessage());
      assertEquals(25, ((ServiceException)handler.cause()).failureCode());
      assertEquals(new JsonObject().put("test", "val"), ((ServiceException)handler.cause()).getDebugInfo());
      testComplete();
    });
    await();
  }

  @Test
  public void testErrorHandlingServiceExceptionSubclass() {
    vertx.eventBus().registerDefaultCodec(MyServiceException.class,
      new MyServiceExceptionMessageCodec());
    proxy.failingCall("Fail subclass", handler -> {
      assertTrue(handler.cause() instanceof MyServiceException);
      assertEquals("Call has failed", handler.cause().getMessage());
      assertEquals(25, ((MyServiceException)handler.cause()).failureCode());
      assertEquals("some extra", ((MyServiceException)handler.cause()).getExtra());
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
    proxy.enumTypeAsResult(ar -> {
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
  public void testEnumTypeAsResultWithNull() {
    proxy.enumTypeAsResultNull(ar -> {
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
    proxy.stringHandler(onSuccess(res -> {
      assertEquals("foobar", res);
      testComplete();
    }));
    await();
  }

  @Test
  public void testStringNullHandler() {
    proxy.stringNullHandler(onSuccess(res -> {
      assertNull(res);
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
  public void testByteNullHandler() {
    proxy.byteNullHandler(onSuccess(res -> {
      assertNull(res);
      testComplete();
    }));
    await();
  }

  @Test
  public void testShortHandler() {
    proxy.shortHandler(onSuccess(res -> {
      assertEquals(Short.valueOf((short) 1234), res);
      testComplete();
    }));
    await();
  }

  @Test
  public void testShortNullHandler() {
    proxy.shortNullHandler(onSuccess(res -> {
      assertNull(res);
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
  public void testIntNullHandler() {
    proxy.intNullHandler(onSuccess(res -> {
      assertNull(res);
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
  public void testLongNullHandler() {
    proxy.longNullHandler(onSuccess(res -> {
      assertNull(res);
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
  public void testFloatNullHandler() {
    proxy.floatNullHandler(onSuccess(res -> {
      assertNull(res);
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
  public void testDoubleNullHandler() {
    proxy.doubleNullHandler(onSuccess(res -> {
      assertNull(res);
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
  public void testCharNullHandler() {
    proxy.charNullHandler(onSuccess(res -> {
      assertNull(res);
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
  public void testBooleanNullHandler() {
    proxy.booleanNullHandler(onSuccess(res -> {
      assertNull(res);
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
  public void testJsonObjectNullHandler() {
    proxy.jsonObjectNullHandler(onSuccess(res -> {
      assertNull(res);
      testComplete();
    }));
    await();
  }

  @Test
  public void testJsonArrayHandler() {
    proxy.jsonArrayHandler(onSuccess(res -> {
      assertEquals("blurrg", res.getString(0));
      testComplete();
    }));
    await();
  }

  @Test
  public void testJsonArrayNullHandler() {
    proxy.jsonArrayNullHandler(onSuccess(res -> {
      assertNull(res);
      testComplete();
    }));
    await();
  }

  @Test
  public void testDataObjectHandler() {
    proxy.dataObjectHandler(onSuccess(res -> {
      assertEquals(new TestDataObject().setString("foo").setNumber(123).setBool(true), res);
      testComplete();
    }));
    await();
  }

  @Test
  public void testDataObjectNullHandler() {
    proxy.dataObjectNullHandler(onSuccess(res -> {
      assertNull(res);
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
    vertx.eventBus().consumer("fluentReceived").handler(msg -> {
      testComplete();
    });
    assertSame(proxy, proxy.fluentNoParams());
    await();
  }

  @Test
  public void testFailingMethod() {
    proxy.failingMethod(onFailure(t -> {
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
  public void testCallWithMessage() {
    JsonObject message = new JsonObject();
    message.put("object", new JsonObject().put("foo", "bar"));
    message.put("str", "blah");
    message.put("i", 1234);
    message.put("chr", (int)'X'); // chars are mapped to ints
    message.put("senum", SomeEnum.BAR.toString()); // enums are mapped to strings
    vertx.eventBus().<String>send("someaddress", message, new DeliveryOptions().addHeader("action", "invokeWithMessage"), onSuccess(res -> {
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
    vertx.eventBus().send(SERVICE_WITH_DEBUG_ADDRESS, message, new DeliveryOptions().addHeader("action", "yourmum").setSendTimeout(500), onFailure(t -> {
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
    message.put("char", (int)'X'); // chars are mapped to ints
    message.put("enum", SomeEnum.BAR.toString()); // enums are mapped to strings
    vertx.eventBus().send(SERVICE_WITH_DEBUG_ADDRESS, message, new DeliveryOptions().addHeader("action", "invokeWithMessage").setSendTimeout(500), onFailure(t -> {
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
      assertEquals(Short.valueOf((short) 11), list.get(0));
      assertEquals(Short.valueOf((short) 12), list.get(1));
      assertEquals(Short.valueOf((short) 13), list.get(2));
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
      assertEquals("foo", list.get(0).getString(0));
      assertEquals("bar", list.get(1).getString(0));
      assertEquals("wibble", list.get(2).getString(0));
      testComplete();
    }));
    await();
  }

  @Test
  public void testSetStringHandler() {
    proxy.setStringHandler(onSuccess(set -> {
      assertTrue(set.contains("foo"));
      assertTrue(set.contains("bar"));
      assertTrue(set.contains("wibble"));
      testComplete();
    }));
    await();
  }

  @Test
  public void testSetByteHandler() {
    proxy.setByteHandler(onSuccess(set -> {
      assertTrue(set.contains((byte) 1));
      assertTrue(set.contains((byte) 2));
      assertTrue(set.contains((byte) 3));
      testComplete();
    }));
    await();
  }

  @Test
  public void testSetShortHandler() {
    proxy.setShortHandler(onSuccess(set -> {
      assertTrue(set.contains((short) 11));
      assertTrue(set.contains((short) 12));
      assertTrue(set.contains((short) 13));
      testComplete();
    }));
    await();
  }

  @Test
  public void testSetIntHandler() {
    proxy.setIntHandler(onSuccess(set -> {
      assertTrue(set.contains(100));
      assertTrue(set.contains(101));
      assertTrue(set.contains(102));
      testComplete();
    }));
    await();
  }

  @Test
  public void testSetLongHandler() {
    proxy.setLongHandler(onSuccess(set -> {
      assertTrue(set.contains(1000l));
      assertTrue(set.contains(1001l));
      assertTrue(set.contains(1002l));
      testComplete();
    }));
    await();
  }

  @Test
  public void testSetFloatHandler() {
    proxy.setFloatHandler(onSuccess(set -> {
      assertTrue(set.contains(1.1f));
      assertTrue(set.contains(1.2f));
      assertTrue(set.contains(1.3f));
      testComplete();
    }));
    await();
  }

  @Test
  public void testSetDoubleHandler() {
    proxy.setDoubleHandler(onSuccess(set -> {
      assertTrue(set.contains(1.11d));
      assertTrue(set.contains(1.12d));
      assertTrue(set.contains(1.13d));
      testComplete();
    }));
    await();
  }

  @Test
  public void testSetCharHandler() {
    proxy.setCharHandler(onSuccess(set -> {
      assertTrue(set.contains('X'));
      assertTrue(set.contains('Y'));
      assertTrue(set.contains('Z'));
      testComplete();
    }));
    await();
  }

  @Test
  public void testSetBoolHandler() {
    proxy.setBoolHandler(onSuccess(set -> {
      assertTrue(set.contains(true));
      assertTrue(set.contains(false));
      testComplete();
    }));
    await();
  }

  @Test
  public void testMapStringHandler() {
    Map<String, String> expected = new HashMap<>();
    expected.put("1", "foo");
    expected.put("2", "bar");
    expected.put("3", "wibble");
    proxy.mapStringHandler(onSuccess(map -> {
      assertEquals(expected, map);
      testComplete();
    }));
    await();
  }

  @Test
  public void testMapByteHandler() {
    Map<String, Byte> expected = new HashMap<>();
    expected.put("1", (byte)1);
    expected.put("2", (byte)2);
    expected.put("3", (byte)3);
    proxy.mapByteHandler(onSuccess(map -> {
      assertEquals(expected, map);
      testComplete();
    }));
    await();
  }

  @Test
  public void testMapShortHandler() {
    Map<String, Short> expected = new HashMap<>();
    expected.put("1", (short)11);
    expected.put("2", (short)12);
    expected.put("3", (short)13);
    proxy.mapShortHandler(onSuccess(map -> {
      assertEquals(expected, map);
      testComplete();
    }));
    await();
  }

  @Test
  public void testMapIntHandler() {
    Map<String, Integer> expected = new HashMap<>();
    expected.put("1", 100);
    expected.put("2", 101);
    expected.put("3", 102);
    proxy.mapIntHandler(onSuccess(map -> {
      assertEquals(expected, map);
      testComplete();
    }));
    await();
  }

  @Test
  public void testMapLongHandler() {
    Map<String, Long> expected = new HashMap<>();
    expected.put("1", 1000l);
    expected.put("2", 1001l);
    expected.put("3", 1002l);
    proxy.mapLongHandler(onSuccess(map -> {
      assertEquals(expected, map);
      testComplete();
    }));
    await();
  }

  @Test
  public void testMapFloatHandler() {
    Map<String, Float> expected = new HashMap<>();
    expected.put("1", 1.1f);
    expected.put("2", 1.2f);
    expected.put("3", 1.3f);
    proxy.mapFloatHandler(onSuccess(map -> {
      assertEquals(expected, map);
      testComplete();
    }));
    await();
  }

  @Test
  public void testMapDoubleHandler() {
    Map<String, Double> expected = new HashMap<>();
    expected.put("1", 1.11d);
    expected.put("2", 1.12d);
    expected.put("3", 1.13d);
    proxy.mapDoubleHandler(onSuccess(map -> {
      assertEquals(expected, map);
      testComplete();
    }));
    await();
  }

  @Test
  public void testMapCharHandler() {
    Map<String, Character> expected = new HashMap<>();
    expected.put("1", 'X');
    expected.put("2", 'Y');
    expected.put("3", 'Z');
    proxy.mapCharHandler(onSuccess(map -> {
      assertEquals(expected, map);
      testComplete();
    }));
    await();
  }

  @Test
  public void testMapBoolHandler() {
    Map<String, Boolean> expected = new HashMap<>();
    expected.put("1", true);
    expected.put("2", false);
    expected.put("3", true);
    proxy.mapBoolHandler(onSuccess(map -> {
      assertEquals(expected, map);
      testComplete();
    }));
    await();
  }

  @Test
  public void testSetJsonObjectHandler() {
    proxy.setJsonObjectHandler(onSuccess(set -> {
      assertTrue(set.contains(new JsonObject().put("a", "foo")));
      assertTrue(set.contains(new JsonObject().put("b", "bar")));
      assertTrue(set.contains(new JsonObject().put("c", "wibble")));
      testComplete();
    }));
    await();
  }

  @Test
  public void testMapJsonObjectHandler() {
    Map<String, JsonObject> expected = new HashMap<>();
    expected.put("1", new JsonObject().put("a", "foo"));
    expected.put("2", new JsonObject().put("b", "bar"));
    expected.put("3", new JsonObject().put("c", "wibble"));
    proxy.mapJsonObjectHandler(onSuccess(map -> {
      assertEquals(expected, map);
      testComplete();
    }));
    await();
  }

  @Test
  public void setSetJsonArrayHandler() {
    proxy.setJsonArrayHandler(onSuccess(set -> {
      assertTrue(set.contains(new JsonArray().add("foo")));
      assertTrue(set.contains(new JsonArray().add("bar")));
      assertTrue(set.contains(new JsonArray().add("wibble")));
      testComplete();
    }));
    await();
  }

  @Test
  public void testMapJsonArrayHandler() {
    Map<String, JsonArray> expected = new HashMap<>();
    expected.put("1", new JsonArray().add("foo"));
    expected.put("2", new JsonArray().add("bar"));
    expected.put("3", new JsonArray().add("wibble"));
    proxy.mapJsonArrayHandler(onSuccess(map -> {
      assertEquals(expected, map);
      testComplete();
    }));
    await();
  }

  @Test
  public void testListDataObjectHandler() {
    proxy.listDataObjectHandler(onSuccess(list -> {
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
  public void testSetDataObjectHandler() {
    proxy.setDataObjectHandler(onSuccess(set -> {
      Set<JsonObject> setJson = set.stream().map(d -> d.toJson()).collect(Collectors.toSet());
      assertEquals(2, setJson.size());
      assertTrue(setJson.contains(new JsonObject().put("number", 1).put("string", "String 1").put("bool", false)));
      assertTrue(setJson.contains(new JsonObject().put("number", 2).put("string", "String 2").put("bool", true)));
      testComplete();
    }));
    await();
  }

  @Test
  public void testMapDataObjectHandler() {
    Map<String, TestDataObject> expected = new HashMap<>();
    expected.put("do1", new TestDataObject().setNumber(1).setString("String 1").setBool(false));
    expected.put("do2", new TestDataObject().setNumber(2).setString("String 2").setBool(true));
    proxy.mapDataObject(onSuccess(map -> {
      assertEquals(expected, map);
      testComplete();
    }));
    await();
  }

  @Test
  public void testDateTimeHandler() {
    proxy.zonedDateTimeHandler(onSuccess(dateTime -> {
      assertEquals(ZonedDateTime.parse("2019-03-25T17:08:31.069+01:00[Europe/Rome]"), dateTime);
      testComplete();
    }));
    await();
  }

  @Test
  public void testListDateTimeHandler() {
    proxy.listZonedDateTimeHandler(onSuccess(list -> {
      assertEquals(Arrays.asList(
        ZonedDateTime.parse("2019-03-25T17:08:31.069+01:00[Europe/Rome]"),
        ZonedDateTime.parse("2019-03-25T17:08:31.069+01:00[Europe/Rome]").plusHours(1)
      ), list);
      testComplete();
    }));
    await();
  }

  @Test
  public void testSetDateTimeHandler() {
    proxy.setZonedDateTimeHandler(onSuccess(set -> {
      assertEquals(new HashSet<>(Arrays.asList(
        ZonedDateTime.parse("2019-03-25T17:08:31.069+01:00[Europe/Rome]"),
        ZonedDateTime.parse("2019-03-25T17:08:31.069+01:00[Europe/Rome]").plusHours(1)
      )), set);
      testComplete();
    }));
    await();
  }

  @Test
  public void testMapDateTimeHandler() {
    Map<String, ZonedDateTime> expected = new HashMap<>();
    expected.put("date1", ZonedDateTime.parse("2019-03-25T17:08:31.069+01:00[Europe/Rome]"));
    expected.put("date2", ZonedDateTime.parse("2019-03-25T17:08:31.069+01:00[Europe/Rome]").plusHours(1));
    proxy.mapZonedDateTimeHandler(onSuccess(map -> {
      assertEquals(expected, map);
      testComplete();
    }));
    await();
  }

  @Test
  public void testProxyIgnore() {
    proxy.ignoredMethod();
    vertx.setTimer(500, id -> testComplete());
    await();
  }

  @Test
  public void testConnection() {
    proxy.createConnection("foo", onSuccess(conn -> {
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
  public void testConnectionWithCloseFutureTimeout() {

    consumer.unregister();
    consumer = new ServiceBinder(vertx)
      .setAddress(SERVICE_ADDRESS)
      .setTimeoutSeconds(2)
      .register(TestService.class, service);

    checkConnectionWithCloseFuture(proxy, System.currentTimeMillis(), 2L);

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
  public void testLocalServiceConnectionWithCloseFutureTimeout() {

    localConsumer.unregister();
    localConsumer = new ServiceBinder(vertx)
      .setAddress(SERVICE_LOCAL_ADDRESS)
      .setTimeoutSeconds(2)
      .register(TestService.class, localService);

    checkConnectionWithCloseFuture(localProxy, System.currentTimeMillis(), 2L);

    await();
  }

  @Test
  public void testLongDelivery1() {
    TestService proxyLong = TestService.createProxyLongDelivery(vertx, SERVICE_ADDRESS);
    proxyLong.longDeliverySuccess(onSuccess(str -> {
      assertEquals("blah", str);
      testComplete();
    }));
    await();
  }

  @Test
  public void testLongDelivery2() {
    TestService proxyLong = TestService.createProxyLongDelivery(vertx, SERVICE_ADDRESS);
    proxyLong.longDeliveryFailed(onFailure(t -> {
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
    proxy.listDataObjectContainingNullHandler(onSuccess(list -> {
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
  public void testASetContainingNullValues() {
    proxy.setDataObjectContainingNullHandler(onSuccess(set -> {
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

  private void checkConnectionWithCloseFuture(TestService proxy, long start, long timeoutSeconds) {
    proxy.createConnectionWithCloseFuture(onSuccess(conn -> {

      vertx.eventBus().consumer("closeCalled").handler(msg -> {
        assertEquals("blah", msg.body());

        long now = System.currentTimeMillis();
        assertTrue(now - start > timeoutSeconds * 1000);

        // Should be closed now
        conn.someMethod(onFailure(this::checkCause));
      });
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
