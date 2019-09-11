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

package io.vertx.serviceproxy.testmodel.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceException;
import io.vertx.serviceproxy.test.ServiceProxyTest;
import io.vertx.serviceproxy.testmodel.MyServiceException;
import io.vertx.serviceproxy.testmodel.SomeEnum;
import io.vertx.serviceproxy.testmodel.TestConnection;
import io.vertx.serviceproxy.testmodel.TestConnectionWithCloseFuture;
import io.vertx.serviceproxy.testmodel.TestDataObject;
import io.vertx.serviceproxy.testmodel.TestService;
import org.assertj.core.api.ZonedDateTimeAssert;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author lalitrao
 */
public class TestServiceImpl implements TestService {

  private final Vertx vertx;
  private final URI uri1;
  private final URI uri2;

  public TestServiceImpl(Vertx vertx) throws Exception {
    this.vertx = vertx;
    this.uri1 = new URI("http://foo.com");
    this.uri2 = new URI("http://bar.com");
  }

  @Override
  public void createConnection(String str, Handler<AsyncResult<TestConnection>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(new TestConnectionImpl(vertx, str)));
  }

  @Override
  public void createConnectionWithCloseFuture(Handler<AsyncResult<TestConnectionWithCloseFuture>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(new TestConnectionWithCloseFutureImpl(vertx)));
  }

  @Override
  public void noParams() {
    vertx.eventBus().send(ServiceProxyTest.TEST_ADDRESS, "ok");
  }

  @Override
  public void basicTypes(String str, byte b, short s, int i, long l, float f, double d, char c, boolean bool) {
    assertEquals("foo", str);
    assertEquals((byte)123, b);
    assertEquals((short)1234, s);
    assertEquals(12345, i);
    assertEquals(123456l, l);
    assertEquals(12345, i);
    assertEquals(12.34f, f, 0);
    assertEquals(12.3456d, d, 0);
    assertEquals('X', c);
    assertEquals(true, bool);
    vertx.eventBus().send(ServiceProxyTest.TEST_ADDRESS, "ok");
  }

  @Override
  public void basicBoxedTypes(String str, Byte b, Short s, Integer i, Long l, Float f, Double d, Character c, Boolean bool) {
    basicTypes(str, b, s, i, l, f, d, c, bool);
  }

  @Override
  public void basicBoxedTypesNull(String str, Byte b, Short s, Integer i, Long l, Float f, Double d, Character c, Boolean bool) {
    assertNull(str);
    assertNull(b);
    assertNull(s);
    assertNull(i);
    assertNull(l);
    assertNull(f);
    assertNull(d);
    assertNull(c);
    assertNull(bool);
    vertx.eventBus().send(ServiceProxyTest.TEST_ADDRESS, "ok");
  }

  @Override
  public void jsonTypes(JsonObject jsonObject, JsonArray jsonArray) {
    assertEquals("bar", jsonObject.getString("foo"));
    assertEquals("wibble", jsonArray.getString(0));
    vertx.eventBus().send(ServiceProxyTest.TEST_ADDRESS, "ok");
  }

  @Override
  public void jsonTypesNull(JsonObject jsonObject, JsonArray jsonArray) {
    assertNull(jsonObject);
    assertNull(jsonArray);
    vertx.eventBus().send(ServiceProxyTest.TEST_ADDRESS, "ok");
  }

  @Override
  public void enumType(SomeEnum someEnum) {
    assertEquals(SomeEnum.WIBBLE, someEnum);
    vertx.eventBus().send(ServiceProxyTest.TEST_ADDRESS, "ok");
  }

  @Override
  public void enumTypeNull(SomeEnum someEnum) {
    assertNull(someEnum);
    vertx.eventBus().send(ServiceProxyTest.TEST_ADDRESS, "ok");
  }

  @Override
  public void enumTypeAsResult(Handler<AsyncResult<SomeEnum>> handler) {
    handler.handle(Future.succeededFuture(SomeEnum.WIBBLE));
  }

  @Override
  public void enumTypeAsResultNull(Handler<AsyncResult<SomeEnum>> handler) {
    handler.handle(Future.succeededFuture(null));
  }

  @Override
  public void dataObjectType(TestDataObject options) {
    assertEquals(new TestDataObject().setString("foo").setNumber(123).setBool(true), options);
    vertx.eventBus().send(ServiceProxyTest.TEST_ADDRESS, "ok");
  }
  
  @Override
  public void listdataObjectType(List<TestDataObject> list) {
    assertEquals(2, list.size());
    assertEquals(new TestDataObject().setString("foo").setNumber(123).setBool(true), list.get(0));
    assertEquals(new TestDataObject().setString("bar").setNumber(456).setBool(false), list.get(1));
    vertx.eventBus().send(ServiceProxyTest.TEST_ADDRESS, "ok");
  }
  
  @Override
  public void setdataObjectType(Set<TestDataObject> set) {
    Set<JsonObject> setJson = set.stream().map(d -> d.toJson()).collect(Collectors.toSet());
    assertEquals(2, setJson.size());
    assertTrue(setJson.contains(new JsonObject().put("number", 123).put("string", "String foo").put("bool", true)));
    assertTrue(setJson.contains(new JsonObject().put("number", 456).put("string", "String bar").put("bool", false)));
    vertx.eventBus().send(ServiceProxyTest.TEST_ADDRESS, "ok");
  }

  @Override
  public void dataObjectTypeNull(TestDataObject options) {
    assertNull(options);
    vertx.eventBus().send(ServiceProxyTest.TEST_ADDRESS, "ok");
  }

  @Override
  public void dateTimeType(ZonedDateTime dateTime) {
    assertEquals(ZonedDateTime.parse("2019-03-25T17:08:31.069+01:00[Europe/Rome]"), dateTime);
    vertx.eventBus().send(ServiceProxyTest.TEST_ADDRESS, "ok");
  }

  @Override
  public void listDateTimeType(List<ZonedDateTime> list) {
    assertEquals(2, list.size());
    assertEquals(ZonedDateTime.parse("2019-03-25T17:08:31.069+01:00[Europe/Rome]"), list.get(0));
    assertEquals(ZonedDateTime.parse("2019-03-25T17:08:31.069+01:00[Europe/Rome]").plusHours(1), list.get(1));
    vertx.eventBus().send(ServiceProxyTest.TEST_ADDRESS, "ok");
  }

  @Override
  public void setDateTimeType(Set<ZonedDateTime> set) {
    assertEquals(2, set.size());
    assertTrue(set.contains(ZonedDateTime.parse("2019-03-25T17:08:31.069+01:00[Europe/Rome]")));
    assertTrue(set.contains(ZonedDateTime.parse("2019-03-25T17:08:31.069+01:00[Europe/Rome]").plusHours(1)));
    vertx.eventBus().send(ServiceProxyTest.TEST_ADDRESS, "ok");
  }

  @Override
  public void mapDateTimeType(Map<String, ZonedDateTime> map) {
    Map<String, ZonedDateTime> expected = new HashMap<>();
    expected.put("date1", ZonedDateTime.parse("2019-03-25T17:08:31.069+01:00[Europe/Rome]"));
    expected.put("date2", ZonedDateTime.parse("2019-03-25T17:08:31.069+01:00[Europe/Rome]").plusHours(1));
    assertEquals(expected, map);
    vertx.eventBus().send(ServiceProxyTest.TEST_ADDRESS, "ok");
  }

  @Override
  public void uriType(URI uri) {
    assertEquals(uri1, uri);
    vertx.eventBus().send(ServiceProxyTest.TEST_ADDRESS, "ok");
  }

  @Override
  public void listUriType(List<URI> list) {
    assertEquals(2, list.size());
    assertEquals(uri1, list.get(0));
    assertEquals(uri2, list.get(1));
    vertx.eventBus().send(ServiceProxyTest.TEST_ADDRESS, "ok");
  }

  @Override
  public void setUriType(Set<URI> set) {
    assertEquals(2, set.size());
    assertTrue(set.contains(uri1));
    assertTrue(set.contains(uri2));
    vertx.eventBus().send(ServiceProxyTest.TEST_ADDRESS, "ok");
  }

  @Override
  public void mapUriType(Map<String, URI> map) {
    Map<String, URI> expected = new HashMap<>();
    expected.put("uri1", uri1);
    expected.put("uri2", uri2);
    assertEquals(expected, map);
    vertx.eventBus().send(ServiceProxyTest.TEST_ADDRESS, "ok");
  }

  @Override
  public void listdataObjectTypeHavingNullValues(List<TestDataObject> list) {
    assertEquals(3, list.size());
    assertEquals(new TestDataObject().setString("foo").setNumber(123).setBool(true), list.get(0));
    assertNull(list.get(1));
    assertEquals(new TestDataObject().setString("bar").setNumber(456).setBool(false), list.get(2));
    vertx.eventBus().send(ServiceProxyTest.TEST_ADDRESS, "ok");
  }
  
  @Override
  public void setdataObjectTypeHavingNullValues(Set<TestDataObject> set) {
    Set<JsonObject> setJson = set.stream().map(d -> null == d ? null : d.toJson()).collect(Collectors.toSet());
    assertEquals(3, setJson.size());
    assertTrue(setJson.contains(new JsonObject().put("number", 123).put("string", "String foo").put("bool", true)));
    assertTrue(setJson.contains(new JsonObject().put("number", 456).put("string", "String bar").put("bool", false)));
    assertTrue(setJson.contains(null));
    vertx.eventBus().send(ServiceProxyTest.TEST_ADDRESS, "ok");
  }

  @Override
  public void listParams(List<String> listString, List<Byte> listByte, List<Short> listShort, List<Integer> listInt, List<Long> listLong,
                         List<JsonObject> listJsonObject, List<JsonArray> listJsonArray, List<TestDataObject> listDataObject) {
    assertEquals("foo", listString.get(0));
    assertEquals("bar", listString.get(1));
    assertEquals((byte)12, listByte.get(0).byteValue());
    assertEquals((byte)13, listByte.get(1).byteValue());
    assertEquals((short)123, listShort.get(0).shortValue());
    assertEquals((short)134, listShort.get(1).shortValue());
    assertEquals(1234, listInt.get(0).intValue());
    assertEquals(1235, listInt.get(1).intValue());
    assertEquals(12345l, listLong.get(0).longValue());
    assertEquals(12346l, listLong.get(1).longValue());
    assertEquals(new JsonObject().put("foo", "bar"), listJsonObject.get(0));
    assertEquals(new JsonObject().put("blah", "eek"), listJsonObject.get(1));
    assertEquals(new JsonArray().add("foo"), listJsonArray.get(0));
    assertEquals(new JsonArray().add("blah"), listJsonArray.get(1));
    assertEquals(new JsonObject().put("number", 1).put("string", "String 1").put("bool", false), listDataObject.get(0).toJson());
    assertEquals(new JsonObject().put("number", 2).put("string", "String 2").put("bool", true), listDataObject.get(1).toJson());
    vertx.eventBus().send(ServiceProxyTest.TEST_ADDRESS, "ok");
  }

  @Override
  public void setParams(Set<String> setString, Set<Byte> setByte, Set<Short> setShort, Set<Integer> setInt, Set<Long> setLong,
                        Set<JsonObject> setJsonObject, Set<JsonArray> setJsonArray, Set<TestDataObject> setDataObject) {
    assertEquals(2, setString.size());
    assertTrue(setString.contains("foo"));
    assertTrue(setString.contains("bar"));
    assertEquals(2, setByte.size());
    assertTrue(setByte.contains((byte)12));
    assertTrue(setByte.contains((byte)13));
    assertEquals(2, setShort.size());
    assertTrue(setShort.contains((short)123));
    assertTrue(setShort.contains((short)134));
    assertEquals(2, setInt.size());
    assertTrue(setInt.contains(1234));
    assertTrue(setInt.contains(1235));
    assertEquals(2, setLong.size());
    assertTrue(setLong.contains(12345l));
    assertTrue(setLong.contains(12346l));
    assertEquals(2, setJsonObject.size());
    assertTrue(setJsonObject.contains(new JsonObject().put("foo", "bar")));
    assertTrue(setJsonObject.contains(new JsonObject().put("blah", "eek")));
    assertEquals(2, setJsonArray.size());
    assertTrue(setJsonArray.contains(new JsonArray().add("foo")));
    assertTrue(setJsonArray.contains(new JsonArray().add("blah")));
    assertEquals(2, setDataObject.size());
    Set<JsonObject> setDataObjectJson = setDataObject.stream().map(d -> d.toJson()).collect(Collectors.toSet());
    assertTrue(setDataObjectJson.contains(new JsonObject().put("number", 1).put("string", "String 1").put("bool", false)));
    assertTrue(setDataObjectJson.contains(new JsonObject().put("number", 2).put("string", "String 2").put("bool", true)));
    vertx.eventBus().send(ServiceProxyTest.TEST_ADDRESS, "ok");
  }

  @Override
  public void mapParams(Map<String, String> mapString, Map<String, Byte> mapByte, Map<String, Short> mapShort,
                        Map<String, Integer> mapInt, Map<String, Long> mapLong, Map<String, JsonObject> mapJsonObject, Map<String, JsonArray> mapJsonArray) {
    assertEquals("foo", mapString.get("eek"));
    assertEquals("bar", mapString.get("wob"));
    assertEquals((byte)12, mapByte.get("eek").byteValue());
    assertEquals((byte)13, mapByte.get("wob").byteValue());
    assertEquals((short)123, mapShort.get("eek").shortValue());
    assertEquals((short)134, mapShort.get("wob").shortValue());
    assertEquals(1234, mapInt.get("eek").intValue());
    assertEquals(1235, mapInt.get("wob").intValue());
    assertEquals(12345l, mapLong.get("eek").longValue());
    assertEquals(12356l, mapLong.get("wob").longValue());
    assertEquals(new JsonObject().put("foo", "bar"), mapJsonObject.get("eek"));
    assertEquals(new JsonObject().put("blah", "eek"), mapJsonObject.get("wob"));
    assertEquals(new JsonArray().add("foo"), mapJsonArray.get("eek"));
    assertEquals(new JsonArray().add("blah"), mapJsonArray.get("wob"));
    vertx.eventBus().send(ServiceProxyTest.TEST_ADDRESS, "ok");
  }

  @Override
  public void stringHandler(Handler<AsyncResult<String>> resultHandler) {
    resultHandler.handle(Future.succeededFuture("foobar"));
  }

  @Override
  public void stringNullHandler(Handler<AsyncResult<String>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(null));
  }

  @Override
  public void byteHandler(Handler<AsyncResult<Byte>> resultHandler) {
    resultHandler.handle(Future.succeededFuture((byte)123));
  }

  @Override
  public void byteNullHandler(Handler<AsyncResult<Byte>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(null));
  }

  @Override
  public void shortHandler(Handler<AsyncResult<Short>> resultHandler) {
    resultHandler.handle(Future.succeededFuture((short)1234));
  }

  @Override
  public void shortNullHandler(Handler<AsyncResult<Short>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(null));
  }

  @Override
  public void intHandler(Handler<AsyncResult<Integer>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(12345));
  }

  @Override
  public void intNullHandler(Handler<AsyncResult<Integer>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(null));
  }

  @Override
  public void longHandler(Handler<AsyncResult<Long>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(123456l));
  }

  @Override
  public void longNullHandler(Handler<AsyncResult<Long>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(null));
  }

  @Override
  public void floatHandler(Handler<AsyncResult<Float>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(12.34f));
  }

  @Override
  public void floatNullHandler(Handler<AsyncResult<Float>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(null));
  }

  @Override
  public void doubleHandler(Handler<AsyncResult<Double>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(12.3456d));
  }

  @Override
  public void doubleNullHandler(Handler<AsyncResult<Double>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(null));
  }

  @Override
  public void charHandler(Handler<AsyncResult<Character>> resultHandler) {
    resultHandler.handle(Future.succeededFuture('X'));
  }

  @Override
  public void charNullHandler(Handler<AsyncResult<Character>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(null));
  }

  @Override
  public void booleanHandler(Handler<AsyncResult<Boolean>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(true));
  }

  @Override
  public void booleanNullHandler(Handler<AsyncResult<Boolean>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(null));
  }

  @Override
  public void jsonObjectHandler(Handler<AsyncResult<JsonObject>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(new JsonObject().put("blah", "wibble")));
  }

  @Override
  public void jsonObjectNullHandler(Handler<AsyncResult<JsonObject>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(null));
  }

  @Override
  public void jsonArrayHandler(Handler<AsyncResult<JsonArray>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(new JsonArray().add("blurrg")));
  }

  @Override
  public void jsonArrayNullHandler(Handler<AsyncResult<JsonArray>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(null));
  }

  @Override
  public void dataObjectHandler(Handler<AsyncResult<TestDataObject>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(new TestDataObject().setString("foo").setNumber(123).setBool(true)));
  }

  @Override
  public void dataObjectNullHandler(Handler<AsyncResult<TestDataObject>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(null));
  }

  @Override
  public void voidHandler(Handler<AsyncResult<Void>> resultHandler) {
    resultHandler.handle(Future.succeededFuture((Void) null));
  }

  @Override
  public TestService fluentMethod(String str, Handler<AsyncResult<String>> resultHandler) {
    assertEquals("foo", str);
    resultHandler.handle(Future.succeededFuture("bar"));
    return this;
  }

  @Override
  public TestService fluentNoParams() {
    vertx.eventBus().send("fluentReceived", "ok");
    return this;
  }

  @Override
  public void failingMethod(Handler<AsyncResult<JsonObject>> resultHandler) {
    resultHandler.handle(Future.failedFuture(new VertxException("wibble")));
  }

  @Override
  public void invokeWithMessage(JsonObject object, String str, int i,  char chr, SomeEnum senum, Handler<AsyncResult<String>> resultHandler) {
    assertEquals("bar", object.getString("foo"));
    assertEquals("blah", str);
    assertEquals(1234, i);
    assertEquals('X', chr);
    assertEquals(SomeEnum.BAR, senum);
    resultHandler.handle(Future.succeededFuture("goats"));
  }

  @Override
  public void listStringHandler(Handler<AsyncResult<List<String>>> resultHandler) {
    List<String> list = Arrays.asList("foo", "bar", "wibble");
    resultHandler.handle(Future.succeededFuture(list));
  }

  @Override
  public void listByteHandler(Handler<AsyncResult<List<Byte>>> resultHandler) {
    List<Byte> list = Arrays.asList((byte)1, (byte)2, (byte)3);
    resultHandler.handle(Future.succeededFuture(list));
  }

  @Override
  public void listShortHandler(Handler<AsyncResult<List<Short>>> resultHandler) {
    List<Short> list = Arrays.asList((short)11, (short)12, (short)13);
    resultHandler.handle(Future.succeededFuture(list));
  }

  @Override
  public void listIntHandler(Handler<AsyncResult<List<Integer>>> resultHandler) {
    List<Integer> list = Arrays.asList(100, 101, 102);
    resultHandler.handle(Future.succeededFuture(list));
  }

  @Override
  public void listLongHandler(Handler<AsyncResult<List<Long>>> resultHandler) {
    List<Long> list = Arrays.asList(1000l, 1001l, 1002l);
    resultHandler.handle(Future.succeededFuture(list));
  }

  @Override
  public void listFloatHandler(Handler<AsyncResult<List<Float>>> resultHandler) {
    List<Float> list = Arrays.asList(1.1f, 1.2f, 1.3f);
    resultHandler.handle(Future.succeededFuture(list));
  }

  @Override
  public void listDoubleHandler(Handler<AsyncResult<List<Double>>> resultHandler) {
    List<Double> list = Arrays.asList(1.11d, 1.12d, 1.13d);
    resultHandler.handle(Future.succeededFuture(list));
  }

  @Override
  public void listCharHandler(Handler<AsyncResult<List<Character>>> resultHandler) {
    List<Character> list = Arrays.asList('X', 'Y', 'Z');
    resultHandler.handle(Future.succeededFuture(list));
  }

  @Override
  public void listBoolHandler(Handler<AsyncResult<List<Boolean>>> resultHandler) {
    List<Boolean> list = Arrays.asList(true, false, true);
    resultHandler.handle(Future.succeededFuture(list));
  }

  @Override
  public void listJsonObjectHandler(Handler<AsyncResult<List<JsonObject>>> resultHandler) {
    List<JsonObject> list = Arrays.asList(new JsonObject().put("a", "foo"),
      new JsonObject().put("b", "bar"), new JsonObject().put("c", "wibble"));
    resultHandler.handle(Future.succeededFuture(list));
  }

  @Override
  public void listJsonArrayHandler(Handler<AsyncResult<List<JsonArray>>> resultHandler) {
    List<JsonArray> list = Arrays.asList(new JsonArray().add("foo"),
      new JsonArray().add("bar"), new JsonArray().add("wibble"));
    resultHandler.handle(Future.succeededFuture(list));
  }

  @Override
  public void setStringHandler(Handler<AsyncResult<Set<String>>> resultHandler) {
    Set<String> set = new LinkedHashSet<>(Arrays.asList("foo", "bar", "wibble"));
    resultHandler.handle(Future.succeededFuture(set));
  }

  @Override
  public void setByteHandler(Handler<AsyncResult<Set<Byte>>> resultHandler) {
    Set<Byte> set = new LinkedHashSet<>(Arrays.asList((byte)1, (byte)2, (byte)3));
    resultHandler.handle(Future.succeededFuture(set));
  }

  @Override
  public void setShortHandler(Handler<AsyncResult<Set<Short>>> resultHandler) {
    Set<Short> set = new LinkedHashSet<>(Arrays.asList((short)11, (short)12, (short)13));
    resultHandler.handle(Future.succeededFuture(set));
  }

  @Override
  public void setIntHandler(Handler<AsyncResult<Set<Integer>>> resultHandler) {
    Set<Integer> set = new LinkedHashSet<>(Arrays.asList(100, 101, 102));
    resultHandler.handle(Future.succeededFuture(set));
  }

  @Override
  public void setLongHandler(Handler<AsyncResult<Set<Long>>> resultHandler) {
    Set<Long> set = new LinkedHashSet<>(Arrays.asList(1000l, 1001l, 1002l));
    resultHandler.handle(Future.succeededFuture(set));
  }

  @Override
  public void setFloatHandler(Handler<AsyncResult<Set<Float>>> resultHandler) {
    Set<Float> set = new LinkedHashSet<>(Arrays.asList(1.1f, 1.2f, 1.3f));
    resultHandler.handle(Future.succeededFuture(set));
  }

  @Override
  public void setDoubleHandler(Handler<AsyncResult<Set<Double>>> resultHandler) {
    Set<Double> set = new LinkedHashSet<>(Arrays.asList(1.11d, 1.12d, 1.13d));
    resultHandler.handle(Future.succeededFuture(set));
  }

  @Override
  public void setCharHandler(Handler<AsyncResult<Set<Character>>> resultHandler) {
    Set<Character> set = new LinkedHashSet<>(Arrays.asList('X', 'Y', 'Z'));
    resultHandler.handle(Future.succeededFuture(set));
  }

  @Override
  public void setBoolHandler(Handler<AsyncResult<Set<Boolean>>> resultHandler) {
    Set<Boolean> set = new LinkedHashSet<>(Arrays.asList(true, false, true));
    resultHandler.handle(Future.succeededFuture(set));
  }

  @Override
  public void mapStringHandler(Handler<AsyncResult<Map<String, String>>> resultHandler) {
    Map<String, String> map = new HashMap<>();
    map.put("1", "foo");
    map.put("2", "bar");
    map.put("3", "wibble");
    resultHandler.handle(Future.succeededFuture(map));
  }

  @Override
  public void mapByteHandler(Handler<AsyncResult<Map<String, Byte>>> resultHandler) {
    Map<String, Byte> map = new HashMap<>();
    map.put("1", (byte)1);
    map.put("2", (byte)2);
    map.put("3", (byte)3);
    resultHandler.handle(Future.succeededFuture(map));
  }

  @Override
  public void mapShortHandler(Handler<AsyncResult<Map<String, Short>>> resultHandler) {
    Map<String, Short> map = new HashMap<>();
    map.put("1", (short)11);
    map.put("2", (short)12);
    map.put("3", (short)13);
    resultHandler.handle(Future.succeededFuture(map));
  }

  @Override
  public void mapIntHandler(Handler<AsyncResult<Map<String, Integer>>> resultHandler) {
    Map<String, Integer> map = new HashMap<>();
    map.put("1", 100);
    map.put("2", 101);
    map.put("3", 102);
    resultHandler.handle(Future.succeededFuture(map));
  }

  @Override
  public void mapLongHandler(Handler<AsyncResult<Map<String, Long>>> resultHandler) {
    Map<String, Long> map = new HashMap<>();
    map.put("1", 1000l);
    map.put("2", 1001l);
    map.put("3", 1002l);
    resultHandler.handle(Future.succeededFuture(map));
  }

  @Override
  public void mapFloatHandler(Handler<AsyncResult<Map<String, Float>>> resultHandler) {
    Map<String, Float> map = new HashMap<>();
    map.put("1", 1.1f);
    map.put("2", 1.2f);
    map.put("3", 1.3f);
    resultHandler.handle(Future.succeededFuture(map));
  }

  @Override
  public void mapDoubleHandler(Handler<AsyncResult<Map<String, Double>>> resultHandler) {
    Map<String, Double> map = new HashMap<>();
    map.put("1", 1.11d);
    map.put("2", 1.12d);
    map.put("3", 1.13d);
    resultHandler.handle(Future.succeededFuture(map));
  }

  @Override
  public void mapCharHandler(Handler<AsyncResult<Map<String, Character>>> resultHandler) {
    Map<String, Character> map = new HashMap<>();
    map.put("1", 'X');
    map.put("2", 'Y');
    map.put("3", 'Z');
    resultHandler.handle(Future.succeededFuture(map));
  }

  @Override
  public void mapBoolHandler(Handler<AsyncResult<Map<String, Boolean>>> resultHandler) {
    Map<String, Boolean> map = new HashMap<>();
    map.put("1", true);
    map.put("2", false);
    map.put("3", true);
    resultHandler.handle(Future.succeededFuture(map));
  }

  @Override
  public void setJsonObjectHandler(Handler<AsyncResult<Set<JsonObject>>> resultHandler) {
    Set<JsonObject> set = new LinkedHashSet<>(Arrays.asList(new JsonObject().put("a", "foo"),
      new JsonObject().put("b", "bar"), new JsonObject().put("c", "wibble")));
    resultHandler.handle(Future.succeededFuture(set));
  }

  @Override
  public void mapJsonObjectHandler(Handler<AsyncResult<Map<String, JsonObject>>> resultHandler) {
    Map<String, JsonObject> map = new HashMap<>();
    map.put("1", new JsonObject().put("a", "foo"));
    map.put("2", new JsonObject().put("b", "bar"));
    map.put("3", new JsonObject().put("c", "wibble"));
    resultHandler.handle(Future.succeededFuture(map));
  }

  @Override
  public void setJsonArrayHandler(Handler<AsyncResult<Set<JsonArray>>> resultHandler) {
    Set<JsonArray> set = new LinkedHashSet<>(Arrays.asList(new JsonArray().add("foo"),
      new JsonArray().add("bar"), new JsonArray().add("wibble")));
    resultHandler.handle(Future.succeededFuture(set));
  }

  @Override
  public void mapJsonArrayHandler(Handler<AsyncResult<Map<String, JsonArray>>> resultHandler) {
    Map<String, JsonArray> map = new HashMap<>();
    map.put("1", new JsonArray().add("foo"));
    map.put("2", new JsonArray().add("bar"));
    map.put("3", new JsonArray().add("wibble"));
    resultHandler.handle(Future.succeededFuture(map));
  }

  @Override
  public void ignoredMethod() {
    vertx.eventBus().send(ServiceProxyTest.TEST_ADDRESS, "called");
  }

  @Override
  public void listDataObjectHandler(Handler<AsyncResult<List<TestDataObject>>> resultHandler) {
    List<TestDataObject> list =
        Arrays.asList(new TestDataObject().setNumber(1).setString("String 1").setBool(false), new TestDataObject().setNumber(2).setString("String 2").setBool(true));
    resultHandler.handle(Future.succeededFuture(list));
  }

  @Override
  public void setDataObjectHandler(Handler<AsyncResult<Set<TestDataObject>>> resultHandler) {
    Set<TestDataObject> set =
        new LinkedHashSet<>(Arrays.asList(new TestDataObject().setNumber(1).setString("String 1").setBool(false), new TestDataObject().setNumber(2).setString("String 2").setBool(true)));
    resultHandler.handle(Future.succeededFuture(set));
  }

  @Override
  public void mapDataObject(Handler<AsyncResult<Map<String, TestDataObject>>> resultHandler) {
    Map<String, TestDataObject> map = new HashMap<>();
    map.put("do1", new TestDataObject().setNumber(1).setString("String 1").setBool(false));
    map.put("do2", new TestDataObject().setNumber(2).setString("String 2").setBool(true));
    resultHandler.handle(Future.succeededFuture(map));
  }

  @Override
  public void longDeliverySuccess(Handler<AsyncResult<String>> resultHandler) {
    vertx.setTimer(10*1000L, tid -> {
      resultHandler.handle(Future.succeededFuture("blah"));
    });
  }

  @Override
  public void longDeliveryFailed(Handler<AsyncResult<String>> resultHandler) {
    vertx.setTimer(30*1000L, tid -> {
      resultHandler.handle(Future.succeededFuture("blah"));
    });
  }

  @Override
  public void failingCall(String value, Handler<AsyncResult<JsonObject>> resultHandler) {
    if (value.equals("Fail")) {
      resultHandler.handle(ServiceException.fail(25, "Call has failed", new JsonObject().put("test", "val")));
    } else if (value.equals("Fail subclass")) {
      resultHandler.handle(MyServiceException.fail(25, "Call has failed", "some extra"));
    } else if (value.equals("Fail with cause")) {
      resultHandler.handle(Future.failedFuture(new IllegalArgumentException("Failed!").fillInStackTrace()));
    } else {
      resultHandler.handle(Future.succeededFuture(new JsonObject()));
    }
  }

  @Override
  public void listDataObjectContainingNullHandler(Handler<AsyncResult<List<TestDataObject>>> resultHandler) {
    List<TestDataObject> list =
      Arrays.asList(
        new TestDataObject().setNumber(1).setString("String 1").setBool(false),
        null,
        new TestDataObject().setNumber(2).setString("String 2").setBool(true));
    resultHandler.handle(Future.succeededFuture(list));
  }

  @Override
  public void setDataObjectContainingNullHandler(Handler<AsyncResult<Set<TestDataObject>>> resultHandler) {
    Set<TestDataObject> set =
      new LinkedHashSet<>(Arrays.asList(
        new TestDataObject().setNumber(1).setString("String 1").setBool(false),
        null,
        new TestDataObject().setNumber(2).setString("String 2").setBool(true)));
    resultHandler.handle(Future.succeededFuture(set));
  }

  @Override
  public void zonedDateTimeHandler(Handler<AsyncResult<ZonedDateTime>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(ZonedDateTime.parse("2019-03-25T17:08:31.069+01:00[Europe/Rome]")));
  }

  @Override
  public void listZonedDateTimeHandler(Handler<AsyncResult<List<ZonedDateTime>>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(Arrays.asList(
      ZonedDateTime.parse("2019-03-25T17:08:31.069+01:00[Europe/Rome]"),
      ZonedDateTime.parse("2019-03-25T17:08:31.069+01:00[Europe/Rome]").plusHours(1)
    )));
  }

  @Override
  public void setZonedDateTimeHandler(Handler<AsyncResult<Set<ZonedDateTime>>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(new HashSet<>(Arrays.asList(
      ZonedDateTime.parse("2019-03-25T17:08:31.069+01:00[Europe/Rome]"),
      ZonedDateTime.parse("2019-03-25T17:08:31.069+01:00[Europe/Rome]").plusHours(1)
    ))));
  }

  @Override
  public void mapZonedDateTimeHandler(Handler<AsyncResult<Map<String, ZonedDateTime>>> resultHandler) {
    Map<String, ZonedDateTime> zonedDateTimeMap = new HashMap<>();
    zonedDateTimeMap.put("date1", ZonedDateTime.parse("2019-03-25T17:08:31.069+01:00[Europe/Rome]"));
    zonedDateTimeMap.put("date2", ZonedDateTime.parse("2019-03-25T17:08:31.069+01:00[Europe/Rome]").plusHours(1));
    resultHandler.handle(Future.succeededFuture(zonedDateTimeMap));
  }

  @Override
  public void uriHandler(Handler<AsyncResult<URI>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(uri1));
  }

  @Override
  public void listUriHandler(Handler<AsyncResult<List<URI>>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(Arrays.asList(
      uri1,
      uri2
    )));
  }

  @Override
  public void setUriHandler(Handler<AsyncResult<Set<URI>>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(new HashSet<>(Arrays.asList(
      uri1,
      uri2
    ))));
  }

  @Override
  public void mapUriHandler(Handler<AsyncResult<Map<String, URI>>> resultHandler) {
    Map<String, URI> zonedDateTimeMap = new HashMap<>();
    zonedDateTimeMap.put("uri1", uri1);
    zonedDateTimeMap.put("uri2", uri2);
    resultHandler.handle(Future.succeededFuture(zonedDateTimeMap));
  }

  @Override
  public void listdataObjectTypeNull(List<TestDataObject> list) {
    assertTrue(list.isEmpty());
    vertx.eventBus().send(ServiceProxyTest.TEST_ADDRESS, "ok");
  }

  @Override
  public void setdataObjectTypeNull(Set<TestDataObject> set) {
    assertTrue(set.isEmpty());
    vertx.eventBus().send(ServiceProxyTest.TEST_ADDRESS, "ok");
  }

  @Override
  public void mapDataObjectType(Map<String, TestDataObject> map) {
    Map<String, TestDataObject> expected = new HashMap<>();
    expected.put("do1", new TestDataObject().setNumber(1).setString("String 1").setBool(false));
    expected.put("do2", new TestDataObject().setNumber(2).setString("String 2").setBool(true));
    assertEquals(expected, map);
    vertx.eventBus().send(ServiceProxyTest.TEST_ADDRESS, "ok");
  }

}
