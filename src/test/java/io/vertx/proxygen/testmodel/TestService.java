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

package io.vertx.proxygen.testmodel;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.ProxyIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.proxygen.ProxyHelper;
import io.vertx.proxygen.testmodel.impl.TestServiceImpl;

import java.util.List;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@ProxyGen
@VertxGen
public interface TestService {

  static TestService create(Vertx vertx) {
    return new TestServiceImpl(vertx);
  }

  static TestService createProxy(Vertx vertx, String address) {
    return ProxyHelper.createProxy(TestService.class, vertx, address);
  }

  void noParams();

  void basicTypes(String str, byte b, short s, int i, long l, float f, double d, char c, boolean bool);

  void basicBoxedTypes(String str, Byte b, Short s, Integer i, Long l, Float f, Double d, Character c,
                                Boolean bool);

  void jsonTypes(JsonObject jsonObject, JsonArray jsonArray);

  void enumType(SomeEnum someEnum);

  void stringHandler(Handler<AsyncResult<String>> resultHandler);

  void byteHandler(Handler<AsyncResult<Byte>> resultHandler);

  void shortHandler(Handler<AsyncResult<Short>> resultHandler);

  void intHandler(Handler<AsyncResult<Integer>> resultHandler);

  void longHandler(Handler<AsyncResult<Long>> resultHandler);

  void floatHandler(Handler<AsyncResult<Float>> resultHandler);

  void doubleHandler(Handler<AsyncResult<Double>> resultHandler);

  void charHandler(Handler<AsyncResult<Character>> resultHandler);

  void booleanHandler(Handler<AsyncResult<Boolean>> resultHandler);

  void jsonObjectHandler(Handler<AsyncResult<JsonObject>> resultHandler);

  void jsonArrayHandler(Handler<AsyncResult<JsonArray>> resultHandler);

  void voidHandler(Handler<AsyncResult<Void>> resultHandler);

  @Fluent
  TestService fluentMethod(String str, Handler<AsyncResult<String>> resultHandler);

  @Fluent
  TestService fluentNoParams();

  void failingMethod(Handler<AsyncResult<JsonObject>> resultHandler);

  void invokeWithMessage(JsonObject object, String str, int i, char chr, SomeEnum senum, Handler<AsyncResult<String>> resultHandler);

  void listStringHandler(Handler<AsyncResult<List<String>>> resultHandler);

  void listByteHandler(Handler<AsyncResult<List<Byte>>> resultHandler);

  void listShortHandler(Handler<AsyncResult<List<Short>>> resultHandler);

  void listIntHandler(Handler<AsyncResult<List<Integer>>> resultHandler);

  void listLongHandler(Handler<AsyncResult<List<Long>>> resultHandler);

  void listFloatHandler(Handler<AsyncResult<List<Float>>> resultHandler);

  void listDoubleHandler(Handler<AsyncResult<List<Double>>> resultHandler);

  void listCharHandler(Handler<AsyncResult<List<Character>>> resultHandler);

  void listBoolHandler(Handler<AsyncResult<List<Boolean>>> resultHandler);

  void listJsonObjectHandler(Handler<AsyncResult<List<JsonObject>>> resultHandler);

  void listJsonArrayHandler(Handler<AsyncResult<List<JsonArray>>> resultHandler);

  @ProxyIgnore
  void ignoredMethod();
}
