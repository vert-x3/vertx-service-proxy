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

package io.vertx.serviceproxy.testmodel;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import io.vertx.serviceproxy.testmodel.impl.TestFutureServiceImpl;

import java.util.List;
import java.util.Set;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author lalitrao
 */
@ProxyGen
public interface TestFutureService {

  static TestFutureService create(Vertx vertx, TestService service) {
    return new TestFutureServiceImpl(vertx, service);
  }

  static TestFutureService createProxy(Vertx vertx, String address) {
    return new ServiceProxyBuilder(vertx).setAddress(address).build(TestFutureService.class);
  }

  static TestFutureService createProxyLongDelivery(Vertx vertx, String address) {
    DeliveryOptions options = new DeliveryOptions();
    options.setSendTimeout(20*1000L);
    return new ServiceProxyBuilder(vertx).setAddress(address).setOptions(options).build(TestFutureService.class);
  }

  Future<String> longDeliverySuccess();

  Future<String> longDeliveryFailed();

  Future<TestConnection> createConnection(String str);

  Future<TestConnectionWithCloseFuture> createConnectionWithCloseFuture();

  Future<SomeEnum> enumTypeAsResult();

  Future<SomeEnum> enumTypeAsResultNull();

  Future<String> stringFuture();

  Future<String> stringNullFuture();

  Future<Byte> byteFuture();

  Future<Byte> byteNullFuture();

  Future<Short> shortFuture();

  Future<Short> shortNullFuture();

  Future<Integer> intFuture();

  Future<Integer> intNullFuture();

  Future<Long> longFuture();

  Future<Long> longNullFuture();

  Future<Float> floatFuture();

  Future<Float> floatNullFuture();

  Future<Double> doubleFuture();

  Future<Double> doubleNullFuture();

  Future<Character> charFuture();

  Future<Character> charNullFuture();

  Future<Boolean> booleanFuture();

  Future<Boolean> booleanNullFuture();

  Future<JsonObject> jsonObjectFuture();

  Future<JsonObject> jsonObjectNullFuture();

  Future<JsonArray> jsonArrayFuture();

  Future<JsonArray> jsonArrayNullFuture();

  Future<TestDataObject> dataObjectFuture();

  Future<TestDataObject> dataObjectNullFuture();

  Future<Void> voidFuture();

  Future<JsonObject> failingFuture();

  Future<List<String>> listStringFuture();

  Future<List<Byte>> listByteFuture();

  Future<List<Short>> listShortFuture();

  Future<List<Integer>> listIntFuture();

  Future<List<Long>> listLongFuture();

  Future<List<Float>> listFloatFuture();

  Future<List<Double>> listDoubleFuture();

  Future<List<Character>> listCharFuture();

  Future<List<Boolean>> listBoolFuture();

  Future<List<JsonObject>> listJsonObjectFuture();

  Future<List<JsonArray>> listJsonArrayFuture();

  Future<List<TestDataObject>> listDataObjectFuture();

  Future<Set<String>> setStringFuture();

  Future<Set<Byte>> setByteFuture();

  Future<Set<Short>> setShortFuture();

  Future<Set<Integer>> setIntFuture();

  Future<Set<Long>> setLongFuture();

  Future<Set<Float>> setFloatFuture();

  Future<Set<Double>> setDoubleFuture();

  Future<Set<Character>> setCharFuture();

  Future<Set<Boolean>> setBoolFuture();

  Future<Set<JsonObject>> setJsonObjectFuture();

  Future<Set<JsonArray>> setJsonArrayFuture();

  Future<Set<TestDataObject>> setDataObjectFuture();

  Future<JsonObject> failingCall(String value);

  Future<List<TestDataObject>> listDataObjectContainingNullFuture();

  Future<Set<TestDataObject>> setDataObjectContainingNullFuture();

}
