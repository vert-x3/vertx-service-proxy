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

import io.vertx.core.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceException;
import io.vertx.serviceproxy.test.ServiceProxyTest;
import io.vertx.serviceproxy.testmodel.*;
import io.vertx.serviceproxy.testmodel.sub.TestSubConnection;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;


/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author lalitrao
 */
public class TestServiceImpl implements TestService {

  private final Vertx vertx;

  public TestServiceImpl(Vertx vertx) throws Exception {
    this.vertx = vertx;
  }

  @Override
  public Future<TestConnection> createConnection(String str) {
    return Future.succeededFuture(new TestConnectionImpl(vertx, str));
  }

  @Override
  public Future<TestSubConnection> createSubConnection(String str) {
    return Future.succeededFuture(new TestSubConnectionImpl(vertx, str));
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
  public Future<SomeEnum> enumTypeAsResult()  {
    return Future.succeededFuture(SomeEnum.WIBBLE);
  }

  @Override
  public Future<SomeEnum> enumTypeAsResultNull()  {
    return Future.succeededFuture(null);
  }

  @Override
  public void enumCustomType(SomeEnumWithCustomConstructor someEnum) {
    assertEquals(SomeEnumWithCustomConstructor.ITEST, someEnum);
    vertx.eventBus().send(ServiceProxyTest.TEST_ADDRESS, "ok");
  }

  @Override
  public void enumCustomTypeNull(SomeEnumWithCustomConstructor someEnum) {
    assertNull(someEnum);
    vertx.eventBus().send(ServiceProxyTest.TEST_ADDRESS, "ok");
  }

  @Override
  public Future<SomeEnumWithCustomConstructor> enumCustomTypeAsResult()  {
    return Future.succeededFuture(SomeEnumWithCustomConstructor.ITEST);
  }

  @Override
  public Future<SomeEnumWithCustomConstructor> enumCustomTypeAsResultNull()  {
    return Future.succeededFuture(null);
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
  public Future<String> stringHandler()  {
    return Future.succeededFuture("foobar");
  }

  @Override
  public Future<String> stringNullHandler()  {
    return Future.succeededFuture(null);
  }

  @Override
  public Future<Byte> byteHandler()  {
    return Future.succeededFuture((byte)123);
  }

  @Override
  public Future<Byte> byteNullHandler()  {
    return Future.succeededFuture(null);
  }

  @Override
  public Future<Short> shortHandler()  {
    return Future.succeededFuture((short)1234);
  }

  @Override
  public Future<Short> shortNullHandler()  {
    return Future.succeededFuture(null);
  }

  @Override
  public Future<Integer> intHandler()  {
    return Future.succeededFuture(12345);
  }

  @Override
  public Future<Integer> intNullHandler()  {
    return Future.succeededFuture(null);
  }

  @Override
  public Future<Long> longHandler()  {
    return Future.succeededFuture(123456l);
  }

  @Override
  public Future<Long> longNullHandler()  {
    return Future.succeededFuture(null);
  }

  @Override
  public Future<Float> floatHandler()  {
    return Future.succeededFuture(12.34f);
  }

  @Override
  public Future<Float> floatNullHandler()  {
    return Future.succeededFuture(null);
  }

  @Override
  public Future<Double> doubleHandler()  {
    return Future.succeededFuture(12.3456d);
  }

  @Override
  public Future<Double> doubleNullHandler()  {
    return Future.succeededFuture(null);
  }

  @Override
  public Future<Character> charHandler()  {
    return Future.succeededFuture('X');
  }

  @Override
  public Future<Character> charNullHandler()  {
    return Future.succeededFuture(null);
  }

  @Override
  public Future<Boolean> booleanHandler()  {
    return Future.succeededFuture(true);
  }

  @Override
  public Future<Boolean> booleanNullHandler()  {
    return Future.succeededFuture(null);
  }

  @Override
  public Future<JsonObject> jsonObjectHandler()  {
    return Future.succeededFuture(new JsonObject().put("blah", "wibble"));
  }

  @Override
  public Future<JsonObject> jsonObjectNullHandler()  {
    return Future.succeededFuture(null);
  }

  @Override
  public Future<JsonArray> jsonArrayHandler()  {
    return Future.succeededFuture(new JsonArray().add("blurrg"));
  }

  @Override
  public Future<JsonArray> jsonArrayNullHandler()  {
    return Future.succeededFuture(null);
  }

  @Override
  public Future<TestDataObject> dataObjectHandler()  {
    return Future.succeededFuture(new TestDataObject().setString("foo").setNumber(123).setBool(true));
  }

  @Override
  public Future<TestDataObject> dataObjectNullHandler()  {
    return Future.succeededFuture(null);
  }

  @Override
  public Future<Void> voidHandler()  {
    return Future.succeededFuture((Void) null);
  }

  @Override
  public Future<JsonObject> failingMethod()  {
    return Future.failedFuture(new VertxException("wibble"));
  }

  @Override
  public Future<String> invokeWithMessage(JsonObject object, String str, int i,  char chr, SomeEnum senum) {
    assertEquals("bar", object.getString("foo"));
    assertEquals("blah", str);
    assertEquals(1234, i);
    assertEquals('X', chr);
    assertEquals(SomeEnum.BAR, senum);
    return Future.succeededFuture("goats");
  }

  @Override
  public Future<List<String>> listStringHandler()  {
    List<String> list = Arrays.asList("foo", "bar", "wibble");
    return Future.succeededFuture(list);
  }

  @Override
  public Future<List<Byte>> listByteHandler()  {
    List<Byte> list = Arrays.asList((byte)1, (byte)2, (byte)3);
    return Future.succeededFuture(list);
  }

  @Override
  public Future<List<Short>> listShortHandler()  {
    List<Short> list = Arrays.asList((short)11, (short)12, (short)13);
    return Future.succeededFuture(list);
  }

  @Override
  public Future<List<Integer>> listIntHandler()  {
    List<Integer> list = Arrays.asList(100, 101, 102);
    return Future.succeededFuture(list);
  }

  @Override
  public Future<List<Long>> listLongHandler()  {
    List<Long> list = Arrays.asList(1000l, 1001l, 1002l);
    return Future.succeededFuture(list);
  }

  @Override
  public Future<List<Float>> listFloatHandler()  {
    List<Float> list = Arrays.asList(1.1f, 1.2f, 1.3f);
    return Future.succeededFuture(list);
  }

  @Override
  public Future<List<Double>> listDoubleHandler()  {
    List<Double> list = Arrays.asList(1.11d, 1.12d, 1.13d);
    return Future.succeededFuture(list);
  }

  @Override
  public Future<List<Character>> listCharHandler()  {
    List<Character> list = Arrays.asList('X', 'Y', 'Z');
    return Future.succeededFuture(list);
  }

  @Override
  public Future<List<Boolean>> listBoolHandler()  {
    List<Boolean> list = Arrays.asList(true, false, true);
    return Future.succeededFuture(list);
  }

  @Override
  public Future<List<JsonObject>> listJsonObjectHandler()  {
    List<JsonObject> list = Arrays.asList(new JsonObject().put("a", "foo"),
      new JsonObject().put("b", "bar"), new JsonObject().put("c", "wibble"));
    return Future.succeededFuture(list);
  }

  @Override
  public Future<List<JsonArray>> listJsonArrayHandler()  {
    List<JsonArray> list = Arrays.asList(new JsonArray().add("foo"),
      new JsonArray().add("bar"), new JsonArray().add("wibble"));
    return Future.succeededFuture(list);
  }

  @Override
  public Future<Set<String>> setStringHandler()  {
    Set<String> set = new LinkedHashSet<>(Arrays.asList("foo", "bar", "wibble"));
    return Future.succeededFuture(set);
  }

  @Override
  public Future<Set<Byte>> setByteHandler()  {
    Set<Byte> set = new LinkedHashSet<>(Arrays.asList((byte)1, (byte)2, (byte)3));
    return Future.succeededFuture(set);
  }

  @Override
  public Future<Set<Short>> setShortHandler()  {
    Set<Short> set = new LinkedHashSet<>(Arrays.asList((short)11, (short)12, (short)13));
    return Future.succeededFuture(set);
  }

  @Override
  public Future<Set<Integer>> setIntHandler()  {
    Set<Integer> set = new LinkedHashSet<>(Arrays.asList(100, 101, 102));
    return Future.succeededFuture(set);
  }

  @Override
  public Future<Set<Long>> setLongHandler()  {
    Set<Long> set = new LinkedHashSet<>(Arrays.asList(1000l, 1001l, 1002l));
    return Future.succeededFuture(set);
  }

  @Override
  public Future<Set<Float>> setFloatHandler()  {
    Set<Float> set = new LinkedHashSet<>(Arrays.asList(1.1f, 1.2f, 1.3f));
    return Future.succeededFuture(set);
  }

  @Override
  public Future<Set<Double>> setDoubleHandler()  {
    Set<Double> set = new LinkedHashSet<>(Arrays.asList(1.11d, 1.12d, 1.13d));
    return Future.succeededFuture(set);
  }

  @Override
  public Future<Set<Character>> setCharHandler()  {
    Set<Character> set = new LinkedHashSet<>(Arrays.asList('X', 'Y', 'Z'));
    return Future.succeededFuture(set);
  }

  @Override
  public Future<Set<Boolean>> setBoolHandler()  {
    Set<Boolean> set = new LinkedHashSet<>(Arrays.asList(true, false, true));
    return Future.succeededFuture(set);
  }

  @Override
  public Future<Map<String, String>> mapStringHandler()  {
    Map<String, String> map = new HashMap<>();
    map.put("1", "foo");
    map.put("2", "bar");
    map.put("3", "wibble");
    return Future.succeededFuture(map);
  }

  @Override
  public Future<Map<String, Byte>> mapByteHandler()  {
    Map<String, Byte> map = new HashMap<>();
    map.put("1", (byte)1);
    map.put("2", (byte)2);
    map.put("3", (byte)3);
    return Future.succeededFuture(map);
  }

  @Override
  public Future<Map<String, Short>> mapShortHandler()  {
    Map<String, Short> map = new HashMap<>();
    map.put("1", (short)11);
    map.put("2", (short)12);
    map.put("3", (short)13);
    return Future.succeededFuture(map);
  }

  @Override
  public Future<Map<String, Integer>> mapIntHandler()  {
    Map<String, Integer> map = new HashMap<>();
    map.put("1", 100);
    map.put("2", 101);
    map.put("3", 102);
    return Future.succeededFuture(map);
  }

  @Override
  public Future<Map<String, Long>> mapLongHandler()  {
    Map<String, Long> map = new HashMap<>();
    map.put("1", 1000l);
    map.put("2", 1001l);
    map.put("3", 1002l);
    return Future.succeededFuture(map);
  }

  @Override
  public Future<Map<String, Float>> mapFloatHandler()  {
    Map<String, Float> map = new HashMap<>();
    map.put("1", 1.1f);
    map.put("2", 1.2f);
    map.put("3", 1.3f);
    return Future.succeededFuture(map);
  }

  @Override
  public Future<Map<String, Double>> mapDoubleHandler()  {
    Map<String, Double> map = new HashMap<>();
    map.put("1", 1.11d);
    map.put("2", 1.12d);
    map.put("3", 1.13d);
    return Future.succeededFuture(map);
  }

  @Override
  public Future<Map<String, Character>> mapCharHandler()  {
    Map<String, Character> map = new HashMap<>();
    map.put("1", 'X');
    map.put("2", 'Y');
    map.put("3", 'Z');
    return Future.succeededFuture(map);
  }

  @Override
  public Future<Map<String, Boolean>> mapBoolHandler()  {
    Map<String, Boolean> map = new HashMap<>();
    map.put("1", true);
    map.put("2", false);
    map.put("3", true);
    return Future.succeededFuture(map);
  }

  @Override
  public Future<Set<JsonObject>> setJsonObjectHandler()  {
    Set<JsonObject> set = new LinkedHashSet<>(Arrays.asList(new JsonObject().put("a", "foo"),
      new JsonObject().put("b", "bar"), new JsonObject().put("c", "wibble")));
    return Future.succeededFuture(set);
  }

  @Override
  public Future<Map<String, JsonObject>> mapJsonObjectHandler()  {
    Map<String, JsonObject> map = new HashMap<>();
    map.put("1", new JsonObject().put("a", "foo"));
    map.put("2", new JsonObject().put("b", "bar"));
    map.put("3", new JsonObject().put("c", "wibble"));
    return Future.succeededFuture(map);
  }

  @Override
  public Future<Set<JsonArray>> setJsonArrayHandler()  {
    Set<JsonArray> set = new LinkedHashSet<>(Arrays.asList(new JsonArray().add("foo"),
      new JsonArray().add("bar"), new JsonArray().add("wibble")));
    return Future.succeededFuture(set);
  }

  @Override
  public Future<Map<String, JsonArray>> mapJsonArrayHandler()  {
    Map<String, JsonArray> map = new HashMap<>();
    map.put("1", new JsonArray().add("foo"));
    map.put("2", new JsonArray().add("bar"));
    map.put("3", new JsonArray().add("wibble"));
    return Future.succeededFuture(map);
  }

  @Override
  public void ignoredMethod() {
    vertx.eventBus().send(ServiceProxyTest.TEST_ADDRESS, "called");
  }

  @Override
  public Future<List<TestDataObject>> listDataObjectHandler()  {
    List<TestDataObject> list =
        Arrays.asList(new TestDataObject().setNumber(1).setString("String 1").setBool(false), new TestDataObject().setNumber(2).setString("String 2").setBool(true));
    return Future.succeededFuture(list);
  }

  @Override
  public Future<Set<TestDataObject>> setDataObjectHandler()  {
    Set<TestDataObject> set =
        new LinkedHashSet<>(Arrays.asList(new TestDataObject().setNumber(1).setString("String 1").setBool(false), new TestDataObject().setNumber(2).setString("String 2").setBool(true)));
    return Future.succeededFuture(set);
  }

  @Override
  public Future<Map<String, TestDataObject>> mapDataObject()  {
    Map<String, TestDataObject> map = new HashMap<>();
    map.put("do1", new TestDataObject().setNumber(1).setString("String 1").setBool(false));
    map.put("do2", new TestDataObject().setNumber(2).setString("String 2").setBool(true));
    return Future.succeededFuture(map);
  }

  @Override
  public Future<String> longDeliverySuccess()  {
    return Future.future(p -> {
      vertx.setTimer(10*1000L, tid -> {
        p.complete("blah");
      });
    });
  }

  @Override
  public Future<String> longDeliveryFailed()  {
    return Future.future(p -> {
      vertx.setTimer(30*1000L, tid -> {
        p.complete("blah");
      });
    });
  }

  @Override
  public Future<JsonObject> failingCall(String value) {
    if (value.equals("Fail")) {
      return ServiceException.fail(25, "Call has failed", new JsonObject().put("test", "val"));
    } else if (value.equals("Fail subclass")) {
      return MyServiceException.fail(25, "Call has failed", "some extra");
    } else if (value.equals("Fail with cause")) {
      return Future.failedFuture(new IllegalArgumentException("Failed!").fillInStackTrace());
    } else {
      return Future.succeededFuture(new JsonObject());
    }
  }

  @Override
  public Future<List<TestDataObject>> listDataObjectContainingNullHandler()  {
    List<TestDataObject> list =
      Arrays.asList(
        new TestDataObject().setNumber(1).setString("String 1").setBool(false),
        null,
        new TestDataObject().setNumber(2).setString("String 2").setBool(true));
    return Future.succeededFuture(list);
  }

  @Override
  public Future<Set<TestDataObject>> setDataObjectContainingNullHandler()  {
    Set<TestDataObject> set =
      new LinkedHashSet<>(Arrays.asList(
        new TestDataObject().setNumber(1).setString("String 1").setBool(false),
        null,
        new TestDataObject().setNumber(2).setString("String 2").setBool(true)));
    return Future.succeededFuture(set);
  }

  @Override
  public Future<ZonedDateTime> zonedDateTimeHandler()  {
    return Future.succeededFuture(ZonedDateTime.parse("2019-03-25T17:08:31.069+01:00[Europe/Rome]"));
  }

  @Override
  public Future<List<ZonedDateTime>> listZonedDateTimeHandler()  {
    return Future.succeededFuture(Arrays.asList(
      ZonedDateTime.parse("2019-03-25T17:08:31.069+01:00[Europe/Rome]"),
      ZonedDateTime.parse("2019-03-25T17:08:31.069+01:00[Europe/Rome]").plusHours(1)
    ));
  }

  @Override
  public Future<Set<ZonedDateTime>> setZonedDateTimeHandler()  {
    return Future.succeededFuture(new HashSet<>(Arrays.asList(
      ZonedDateTime.parse("2019-03-25T17:08:31.069+01:00[Europe/Rome]"),
      ZonedDateTime.parse("2019-03-25T17:08:31.069+01:00[Europe/Rome]").plusHours(1)
    )));
  }

  @Override
  public Future<Map<String, ZonedDateTime>> mapZonedDateTimeHandler()  {
    Map<String, ZonedDateTime> zonedDateTimeMap = new HashMap<>();
    zonedDateTimeMap.put("date1", ZonedDateTime.parse("2019-03-25T17:08:31.069+01:00[Europe/Rome]"));
    zonedDateTimeMap.put("date2", ZonedDateTime.parse("2019-03-25T17:08:31.069+01:00[Europe/Rome]").plusHours(1));
    return Future.succeededFuture(zonedDateTimeMap);
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
