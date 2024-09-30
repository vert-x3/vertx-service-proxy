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

package io.vertx.serviceproxy.tests.testmodel;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.ProxyIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import io.vertx.serviceproxy.tests.testmodel.impl.TestServiceImpl;
import io.vertx.serviceproxy.tests.testmodel.sub.TestSubConnection;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author lalitrao
 */
@ProxyGen
@VertxGen
public interface TestService {

  static TestService create(Vertx vertx) throws Exception {
    return new TestServiceImpl(vertx);
  }

  static TestService createProxy(Vertx vertx, String address) {
    return new ServiceProxyBuilder(vertx).setAddress(address).build(TestService.class);
  }

  static TestService createProxyLongDelivery(Vertx vertx, String address) {
    DeliveryOptions options = new DeliveryOptions();
    options.setSendTimeout(20*1000L);
    return new ServiceProxyBuilder(vertx).setAddress(address).setOptions(options).build(TestService.class);
  }

  static TestService createProxyWithOptions(Vertx vertx, String address, DeliveryOptions options) {
    return new ServiceProxyBuilder(vertx).setAddress(address).setOptions(options).build(TestService.class);
  }

  Future<String> longDeliverySuccess() ;

  Future<String> longDeliveryFailed() ;

  Future<TestConnection> createConnection(String str);

  Future<TestSubConnection> createSubConnection(String str);

  void noParams();

  void basicTypes(String str, byte b, short s, int i, long l, float f, double d, char c, boolean bool);

  void basicBoxedTypes(String str, Byte b, Short s, Integer i, Long l, Float f, Double d, Character c,
                                Boolean bool);

  void basicBoxedTypesNull(String str, Byte b, Short s, Integer i, Long l, Float f, Double d, Character c,
      Boolean bool);

  void jsonTypes(JsonObject jsonObject, JsonArray jsonArray);

  void jsonTypesNull(JsonObject jsonObject, JsonArray jsonArray);

  void enumType(SomeEnum someEnum);

  void enumTypeNull(SomeEnum someEnum);

  Future<SomeEnum> enumTypeAsResult() ;

  Future<SomeEnum> enumTypeAsResultNull() ;

  void enumCustomType(SomeEnumWithCustomConstructor someEnum);

  void enumCustomTypeNull(SomeEnumWithCustomConstructor someEnum);

  Future<SomeEnumWithCustomConstructor> enumCustomTypeAsResult() ;

  Future<SomeEnumWithCustomConstructor> enumCustomTypeAsResultNull() ;

  void dataObjectType(TestDataObject options);

  void listdataObjectType(List<TestDataObject> list);

  void setdataObjectType(Set<TestDataObject> set);

  void dataObjectTypeNull(TestDataObject options);

  void dateTimeType(ZonedDateTime dateTime);

  void listDateTimeType(List<ZonedDateTime> list);

  void setDateTimeType(Set<ZonedDateTime> set);

  void mapDateTimeType(Map<String, ZonedDateTime> map);

  void listdataObjectTypeHavingNullValues(List<TestDataObject> list);

  void setdataObjectTypeHavingNullValues(Set<TestDataObject> set);

  void listdataObjectTypeNull(List<TestDataObject> list);

  void setdataObjectTypeNull(Set<TestDataObject> set);

  void mapDataObjectType(Map<String, TestDataObject> map);

  void listParams(List<String> listString, List<Byte> listByte, List<Short> listShort, List<Integer> listInt, List<Long> listLong, List<JsonObject> listJsonObject, List<JsonArray> listJsonArray, List<TestDataObject> listDataObject);

  void setParams(Set<String> setString, Set<Byte> setByte, Set<Short> setShort, Set<Integer> setInt, Set<Long> setLong, Set<JsonObject> setJsonObject, Set<JsonArray> setJsonArray, Set<TestDataObject> setDataObject);

  void mapParams(Map<String, String> mapString, Map<String, Byte> mapByte, Map<String, Short> mapShort, Map<String, Integer> mapInt, Map<String, Long> mapLong, Map<String, JsonObject> mapJsonObject, Map<String, JsonArray> mapJsonArray);

  Future<String> stringHandler() ;

  Future<String> stringNullHandler() ;

  Future<Byte> byteHandler() ;

  Future<Byte> byteNullHandler() ;

  Future<Short> shortHandler() ;

  Future<Short> shortNullHandler() ;

  Future<Integer> intHandler() ;

  Future<Integer> intNullHandler() ;

  Future<Long> longHandler() ;

  Future<Long> longNullHandler() ;

  Future<Float> floatHandler() ;

  Future<Float> floatNullHandler() ;

  Future<Double> doubleHandler() ;

  Future<Double> doubleNullHandler() ;

  Future<Character> charHandler() ;

  Future<Character> charNullHandler() ;

  Future<Boolean> booleanHandler() ;

  Future<Boolean> booleanNullHandler() ;

  Future<JsonObject> jsonObjectHandler() ;

  Future<JsonObject> jsonObjectNullHandler() ;

  Future<JsonArray> jsonArrayHandler() ;

  Future<JsonArray> jsonArrayNullHandler() ;

  Future<TestDataObject> dataObjectHandler() ;

  Future<TestDataObject> dataObjectNullHandler() ;

  Future<Void> voidHandler() ;

  Future<JsonObject> failingMethod() ;

  Future<String> invokeWithMessage(JsonObject object, String str, int i, char chr, SomeEnum senum);

  Future<List<String>> listStringHandler() ;

  Future<List<Byte>> listByteHandler() ;

  Future<List<Short>> listShortHandler() ;

  Future<List<Integer>> listIntHandler() ;

  Future<List<Long>> listLongHandler() ;

  Future<List<Float>> listFloatHandler() ;

  Future<List<Double>> listDoubleHandler() ;

  Future<List<Character>> listCharHandler() ;

  Future<List<Boolean>> listBoolHandler() ;

  Future<List<JsonObject>> listJsonObjectHandler() ;

  Future<List<JsonArray>> listJsonArrayHandler() ;

  Future<List<TestDataObject>> listDataObjectHandler() ;

  Future<Set<String>> setStringHandler() ;

  Future<Set<Byte>> setByteHandler() ;

  Future<Set<Short>> setShortHandler() ;

  Future<Set<Integer>> setIntHandler() ;

  Future<Set<Long>> setLongHandler() ;

  Future<Set<Float>> setFloatHandler() ;

  Future<Set<Double>> setDoubleHandler() ;

  Future<Set<Character>> setCharHandler() ;

  Future<Set<Boolean>> setBoolHandler() ;

  Future<Map<String, String>> mapStringHandler() ;

  Future<Map<String, Byte>> mapByteHandler() ;

  Future<Map<String, Short>> mapShortHandler() ;

  Future<Map<String, Integer>> mapIntHandler() ;

  Future<Map<String, Long>> mapLongHandler() ;

  Future<Map<String, Float>> mapFloatHandler() ;

  Future<Map<String, Double>> mapDoubleHandler() ;

  Future<Map<String, Character>> mapCharHandler() ;

  Future<Map<String, Boolean>> mapBoolHandler() ;

  Future<Set<JsonObject>> setJsonObjectHandler() ;

  Future<Map<String, JsonObject>> mapJsonObjectHandler() ;

  Future<Set<JsonArray>> setJsonArrayHandler() ;

  Future<Map<String, JsonArray>> mapJsonArrayHandler() ;

  Future<Set<TestDataObject>> setDataObjectHandler() ;

  Future<Map<String, TestDataObject>> mapDataObject() ;

  Future<JsonObject> failingCall(String value);

  Future<List<TestDataObject>> listDataObjectContainingNullHandler() ;

  Future<Set<TestDataObject>> setDataObjectContainingNullHandler() ;

  Future<ZonedDateTime> zonedDateTimeHandler() ;

  Future<List<ZonedDateTime>> listZonedDateTimeHandler() ;

  Future<Set<ZonedDateTime>> setZonedDateTimeHandler() ;

  Future<Map<String, ZonedDateTime>> mapZonedDateTimeHandler() ;

  @ProxyIgnore
  void ignoredMethod();
}
