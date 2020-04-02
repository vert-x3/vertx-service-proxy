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
import io.vertx.serviceproxy.testmodel.*;

import java.time.ZonedDateTime;
import java.util.*;
/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author lalitrao
 */
public class TestFutureServiceImpl implements TestFutureService {

  private final Vertx vertx;
  private TestService service;

  public TestFutureServiceImpl(Vertx vertx, TestService service) {
    this.vertx = vertx;
    this.service = service;
  }

  @Override
  public Future<TestConnection> createConnection(String str) {
    Promise<TestConnection> promise = Promise.promise();
    service.createConnection(str, promise);
    return promise.future();
  }

  @Override
  public Future<TestConnectionWithCloseFuture> createConnectionWithCloseFuture() {
    Promise<TestConnectionWithCloseFuture> promise = Promise.promise();
    service.createConnectionWithCloseFuture(promise);
    return promise.future();
  }

  @Override
  public Future<SomeEnum> enumTypeAsResult() {
    Promise<SomeEnum> promise = Promise.promise();
    service.enumTypeAsResult(promise);
    return promise.future();
  }

  @Override
  public Future<SomeEnum> enumTypeAsResultNull() {
    Promise<SomeEnum> promise = Promise.promise();
    service.enumTypeAsResultNull(promise);
    return promise.future();
  }

  @Override
  public Future<String> stringFuture() {
    Promise<String> promise = Promise.promise();
    service.stringHandler(promise);
    return promise.future();
  }

  @Override
  public Future<String> stringNullFuture() {
    Promise<String> promise = Promise.promise();
    service.stringNullHandler(promise);
    return promise.future();
  }

  @Override
  public Future<Byte> byteFuture() {
    Promise<Byte> promise = Promise.promise();
    service.byteHandler(promise);
    return promise.future();
  }

  @Override
  public Future<Byte> byteNullFuture() {
    Promise<Byte> promise = Promise.promise();
    service.byteNullHandler(promise);
    return promise.future();
  }

  @Override
  public Future<Short> shortFuture() {
    Promise<Short> promise = Promise.promise();
    service.shortHandler(promise);
    return promise.future();
  }

  @Override
  public Future<Short> shortNullFuture() {
    Promise<Short> promise = Promise.promise();
    service.shortNullHandler(promise);
    return promise.future();
  }

  @Override
  public Future<Integer> intFuture() {
    Promise<Integer> promise = Promise.promise();
    service.intHandler(promise);
    return promise.future();
  }

  @Override
  public Future<Integer> intNullFuture() {
    Promise<Integer> promise = Promise.promise();
    service.intNullHandler(promise);
    return promise.future();
  }

  @Override
  public Future<Long> longFuture() {
    Promise<Long> promise = Promise.promise();
    service.longHandler(promise);
    return promise.future();
  }

  @Override
  public Future<Long> longNullFuture() {
    Promise<Long> promise = Promise.promise();
    service.longNullHandler(promise);
    return promise.future();
  }

  @Override
  public Future<Float> floatFuture() {
    Promise<Float> promise = Promise.promise();
    service.floatHandler(promise);
    return promise.future();
  }

  @Override
  public Future<Float> floatNullFuture() {
    Promise<Float> promise = Promise.promise();
    service.floatNullHandler(promise);
    return promise.future();
  }

  @Override
  public Future<Double> doubleFuture() {
    Promise<Double> promise = Promise.promise();
    service.doubleHandler(promise);
    return promise.future();
  }

  @Override
  public Future<Double> doubleNullFuture() {
    Promise<Double> promise = Promise.promise();
    service.doubleNullHandler(promise);
    return promise.future();
  }

  @Override
  public Future<Character> charFuture() {
    Promise<Character> promise = Promise.promise();
    service.charHandler(promise);
    return promise.future();
  }

  @Override
  public Future<Character> charNullFuture() {
    Promise<Character> promise = Promise.promise();
    service.charNullHandler(promise);
    return promise.future();
  }

  @Override
  public Future<Boolean> booleanFuture() {
    Promise<Boolean> promise = Promise.promise();
    service.booleanHandler(promise);
    return promise.future();
  }

  @Override
  public Future<Boolean> booleanNullFuture() {
    Promise<Boolean> promise = Promise.promise();
    service.booleanNullHandler(promise);
    return promise.future();
  }

  @Override
  public Future<JsonObject> jsonObjectFuture() {
    Promise<JsonObject> promise = Promise.promise();
    service.jsonObjectHandler(promise);
    return promise.future();
  }

  @Override
  public Future<JsonObject> jsonObjectNullFuture() {
    Promise<JsonObject> promise = Promise.promise();
    service.jsonObjectNullHandler(promise);
    return promise.future();
  }

  @Override
  public Future<JsonArray> jsonArrayFuture() {
    Promise<JsonArray> promise = Promise.promise();
    service.jsonArrayHandler(promise);
    return promise.future();
  }

  @Override
  public Future<JsonArray> jsonArrayNullFuture() {
    Promise<JsonArray> promise = Promise.promise();
    service.jsonArrayNullHandler(promise);
    return promise.future();
  }

  @Override
  public Future<TestDataObject> dataObjectFuture() {
    Promise<TestDataObject> promise = Promise.promise();
    service.dataObjectHandler(promise);
    return promise.future();
  }

  @Override
  public Future<TestDataObject> dataObjectNullFuture() {
    Promise<TestDataObject> promise = Promise.promise();
    service.dataObjectNullHandler(promise);
    return promise.future();
  }

  @Override
  public Future<Void> voidFuture() {
    Promise<Void> promise = Promise.promise();
    service.voidHandler(promise);
    return promise.future();
  }

  @Override
  public Future<JsonObject> failingFuture() {
    Promise<JsonObject> promise = Promise.promise();
    service.failingMethod(promise);
    return promise.future();
  }

  @Override
  public Future<List<String>> listStringFuture() {
    Promise<List<String>> promise = Promise.promise();
    service.listStringHandler(promise);
    return promise.future();
  }

  @Override
  public Future<List<Byte>> listByteFuture() {
    Promise<List<Byte>> promise = Promise.promise();
    service.listByteHandler(promise);
    return promise.future();
  }

  @Override
  public Future<List<Short>> listShortFuture() {
    Promise<List<Short>> promise = Promise.promise();
    service.listShortHandler(promise);
    return promise.future();
  }

  @Override
  public Future<List<Integer>> listIntFuture() {
    Promise<List<Integer>> promise = Promise.promise();
    service.listIntHandler(promise);
    return promise.future();
  }

  @Override
  public Future<List<Long>> listLongFuture() {
    Promise<List<Long>> promise = Promise.promise();
    service.listLongHandler(promise);
    return promise.future();
  }

  @Override
  public Future<List<Float>> listFloatFuture() {
    Promise<List<Float>> promise = Promise.promise();
    service.listFloatHandler(promise);
    return promise.future();
  }

  @Override
  public Future<List<Double>> listDoubleFuture() {
    Promise<List<Double>> promise = Promise.promise();
    service.listDoubleHandler(promise);
    return promise.future();
  }

  @Override
  public Future<List<Character>> listCharFuture() {
    Promise<List<Character>> promise = Promise.promise();
    service.listCharHandler(promise);
    return promise.future();
  }

  @Override
  public Future<List<Boolean>> listBoolFuture() {
    Promise<List<Boolean>> promise = Promise.promise();
    service.listBoolHandler(promise);
    return promise.future();
  }

  @Override
  public Future<List<JsonObject>> listJsonObjectFuture() {
    Promise<List<JsonObject>> promise = Promise.promise();
    service.listJsonObjectHandler(promise);
    return promise.future();
  }

  @Override
  public Future<List<JsonArray>> listJsonArrayFuture() {
    Promise<List<JsonArray>> promise = Promise.promise();
    service.listJsonArrayHandler(promise);
    return promise.future();
  }

  @Override
  public Future<Set<String>> setStringFuture() {
    Promise<Set<String>> promise = Promise.promise();
    service.setStringHandler(promise);
    return promise.future();
  }

  @Override
  public Future<Set<Byte>> setByteFuture() {
    Promise<Set<Byte>> promise = Promise.promise();
    service.setByteHandler(promise);
    return promise.future();
  }

  @Override
  public Future<Set<Short>> setShortFuture() {
    Promise<Set<Short>> promise = Promise.promise();
    service.setShortHandler(promise);
    return promise.future();
  }

  @Override
  public Future<Set<Integer>> setIntFuture() {
    Promise<Set<Integer>> promise = Promise.promise();
    service.setIntHandler(promise);
    return promise.future();
  }

  @Override
  public Future<Set<Long>> setLongFuture() {
    Promise<Set<Long>> promise = Promise.promise();
    service.setLongHandler(promise);
    return promise.future();
  }

  @Override
  public Future<Set<Float>> setFloatFuture() {
    Promise<Set<Float>> promise = Promise.promise();
    service.setFloatHandler(promise);
    return promise.future();
  }

  @Override
  public Future<Set<Double>> setDoubleFuture() {
    Promise<Set<Double>> promise = Promise.promise();
    service.setDoubleHandler(promise);
    return promise.future();
  }

  @Override
  public Future<Set<Character>> setCharFuture() {
    Promise<Set<Character>> promise = Promise.promise();
    service.setCharHandler(promise);
    return promise.future();
  }

  @Override
  public Future<Set<Boolean>> setBoolFuture() {
    Promise<Set<Boolean>> promise = Promise.promise();
    service.setBoolHandler(promise);
    return promise.future();
  }

  @Override
  public Future<Set<JsonObject>> setJsonObjectFuture() {
    Promise<Set<JsonObject>> promise = Promise.promise();
    service.setJsonObjectHandler(promise);
    return promise.future();
  }

  @Override
  public Future<Set<JsonArray>> setJsonArrayFuture() {
    Promise<Set<JsonArray>> promise = Promise.promise();
    service.setJsonArrayHandler(promise);
    return promise.future();
  }

  @Override
  public Future<List<TestDataObject>> listDataObjectFuture() {
    Promise<List<TestDataObject>> promise = Promise.promise();
    service.listDataObjectHandler(promise);
    return promise.future();
  }

  @Override
  public Future<Set<TestDataObject>> setDataObjectFuture() {
    Promise<Set<TestDataObject>> promise = Promise.promise();
    service.setDataObjectHandler(promise);
    return promise.future();
  }

  @Override
  public Future<String> longDeliverySuccess() {
    Promise<String> promise = Promise.promise();
    service.longDeliverySuccess(promise);
    return promise.future();
  }

  @Override
  public Future<String> longDeliveryFailed() {
    Promise<String> promise = Promise.promise();
    service.longDeliveryFailed(promise);
    return promise.future();
  }

  @Override
  public Future<JsonObject> failingCall(String value) {
    Promise<JsonObject> promise = Promise.promise();
    service.failingCall(value, promise);
    return promise.future();
  }

  @Override
  public Future<List<TestDataObject>> listDataObjectContainingNullFuture() {
    Promise<List<TestDataObject>> promise = Promise.promise();
    service.listDataObjectContainingNullHandler(promise);
    return promise.future();
  }

  @Override
  public Future<Set<TestDataObject>> setDataObjectContainingNullFuture() {
    Promise<Set<TestDataObject>> promise = Promise.promise();
    service.setDataObjectContainingNullHandler(promise);
    return promise.future();
  }

  @Override
  public Future<Map<String, String>> mapStringFuture() {
    Promise<Map<String, String>> promise = Promise.promise();
    service.mapStringHandler(promise);
    return promise.future();
  }

  @Override
  public Future<Map<String, Byte>> mapByteFuture() {
    Promise<Map<String, Byte>> promise = Promise.promise();
    service.mapByteHandler(promise);
    return promise.future();
  }

  @Override
  public Future<Map<String, Short>> mapShortFuture() {
    Promise<Map<String, Short>> promise = Promise.promise();
    service.mapShortHandler(promise);
    return promise.future();
  }

  @Override
  public Future<Map<String, Integer>> mapIntFuture() {
    Promise<Map<String, Integer>> promise = Promise.promise();
    service.mapIntHandler(promise);
    return promise.future();
  }

  @Override
  public Future<Map<String, Long>> mapLongFuture() {
    Promise<Map<String, Long>> promise = Promise.promise();
    service.mapLongHandler(promise);
    return promise.future();
  }

  @Override
  public Future<Map<String, Float>> mapFloatFuture() {
    Promise<Map<String, Float>> promise = Promise.promise();
    service.mapFloatHandler(promise);
    return promise.future();
  }

  @Override
  public Future<Map<String, Double>> mapDoubleFuture() {
    Promise<Map<String, Double>> promise = Promise.promise();
    service.mapDoubleHandler(promise);
    return promise.future();
  }

  @Override
  public Future<Map<String, Character>> mapCharFuture() {
    Promise<Map<String, Character>> promise = Promise.promise();
    service.mapCharHandler(promise);
    return promise.future();
  }

  @Override
  public Future<Map<String, Boolean>> mapBoolFuture() {
    Promise<Map<String, Boolean>> promise = Promise.promise();
    service.mapBoolHandler(promise);
    return promise.future();
  }

  @Override
  public Future<Map<String, JsonObject>> mapJsonObjectFuture() {
    Promise<Map<String, JsonObject>> promise = Promise.promise();
    service.mapJsonObjectHandler(promise);
    return promise.future();
  }

  @Override
  public Future<Map<String, JsonArray>> mapJsonArrayFuture() {
    Promise<Map<String, JsonArray>> promise = Promise.promise();
    service.mapJsonArrayHandler(promise);
    return promise.future();
  }

  @Override
  public Future<Map<String, TestDataObject>> mapDataObjectFuture() {
    Promise<Map<String, TestDataObject>> promise = Promise.promise();
    service.mapDataObject(promise);
    return promise.future();
  }

  @Override
  public Future<ZonedDateTime> zonedDateTimeFuture() {
    Promise<ZonedDateTime> promise = Promise.promise();
    service.zonedDateTimeHandler(promise);
    return promise.future();
  }

  @Override
  public Future<List<ZonedDateTime>> listZonedDateTimeFuture() {
    Promise<List<ZonedDateTime>> promise = Promise.promise();
    service.listZonedDateTimeHandler(promise);
    return promise.future();
  }

  @Override
  public Future<Set<ZonedDateTime>> setZonedDateTimeFuture() {
    Promise<Set<ZonedDateTime>> promise = Promise.promise();
    service.setZonedDateTimeHandler(promise);
    return promise.future();
  }

  @Override
  public Future<Map<String, ZonedDateTime>> mapZonedDateTimeFuture() {
    Promise<Map<String, ZonedDateTime>> promise = Promise.promise();
    service.mapZonedDateTimeHandler(promise);
    return promise.future();
  }
}
