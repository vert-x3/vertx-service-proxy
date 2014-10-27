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

package io.vertx.proxygen.testmodel.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.proxygen.test.ServiceProxyTest;
import io.vertx.proxygen.testmodel.SomeEnum;
import io.vertx.proxygen.testmodel.TestOptions;
import io.vertx.proxygen.testmodel.TestService;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class TestServiceImpl implements TestService {

  private final Vertx vertx;

  public TestServiceImpl(Vertx vertx) {
    this.vertx = vertx;
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
  public void jsonTypes(JsonObject jsonObject, JsonArray jsonArray) {
    assertEquals("bar", jsonObject.getString("foo"));
    assertEquals("wibble", jsonArray.get(0));
    vertx.eventBus().send(ServiceProxyTest.TEST_ADDRESS, "ok");
  }

  @Override
  public void enumType(SomeEnum someEnum) {
    assertEquals(SomeEnum.WIBBLE, someEnum);
    vertx.eventBus().send(ServiceProxyTest.TEST_ADDRESS, "ok");
  }

  @Override
  public void optionType(TestOptions options) {
    assertEquals(new TestOptions().setString("foo").setNumber(123).setBool(true), options);
    vertx.eventBus().send(ServiceProxyTest.TEST_ADDRESS, "ok");
  }

  @Override
  public void stringHandler(Handler<AsyncResult<String>> resultHandler) {
    resultHandler.handle(Future.completedFuture("foobar"));
  }

  @Override
  public void byteHandler(Handler<AsyncResult<Byte>> resultHandler) {
    resultHandler.handle(Future.completedFuture((byte)123));
  }

  @Override
  public void shortHandler(Handler<AsyncResult<Short>> resultHandler) {
    resultHandler.handle(Future.completedFuture((short)1234));
  }

  @Override
  public void intHandler(Handler<AsyncResult<Integer>> resultHandler) {
    resultHandler.handle(Future.completedFuture(12345));
  }

  @Override
  public void longHandler(Handler<AsyncResult<Long>> resultHandler) {
    resultHandler.handle(Future.completedFuture(123456l));
  }

  @Override
  public void floatHandler(Handler<AsyncResult<Float>> resultHandler) {
    resultHandler.handle(Future.completedFuture(12.34f));
  }

  @Override
  public void doubleHandler(Handler<AsyncResult<Double>> resultHandler) {
    resultHandler.handle(Future.completedFuture(12.3456d));
  }

  @Override
  public void charHandler(Handler<AsyncResult<Character>> resultHandler) {
    resultHandler.handle(Future.completedFuture('X'));
  }

  @Override
  public void booleanHandler(Handler<AsyncResult<Boolean>> resultHandler) {
    resultHandler.handle(Future.completedFuture(true));
  }

  @Override
  public void jsonObjectHandler(Handler<AsyncResult<JsonObject>> resultHandler) {
    resultHandler.handle(Future.completedFuture(new JsonObject().putString("blah", "wibble")));
  }

  @Override
  public void jsonArrayHandler(Handler<AsyncResult<JsonArray>> resultHandler) {
    resultHandler.handle(Future.completedFuture(new JsonArray().add("blurrg")));
  }

  @Override
  public void voidHandler(Handler<AsyncResult<Void>> resultHandler) {
    resultHandler.handle(Future.completedFuture((Void) null));
  }

  @Override
  public TestService fluentMethod(String str, Handler<AsyncResult<String>> resultHandler) {
    assertEquals("foo", str);
    resultHandler.handle(Future.completedFuture("bar"));
    return this;
  }

  @Override
  public TestService fluentNoParams() {
    vertx.eventBus().send(ServiceProxyTest.TEST_ADDRESS, "ok");
    return this;
  }

  @Override
  public void failingMethod(Handler<AsyncResult<JsonObject>> resultHandler) {
    resultHandler.handle(Future.completedFuture(new VertxException("wibble")));
  }

  @Override
  public void invokeWithMessage(JsonObject object, String str, int i,  char chr, SomeEnum senum, Handler<AsyncResult<String>> resultHandler) {
    assertEquals("bar", object.getString("foo"));
    assertEquals("blah", str);
    assertEquals(1234, i);
    assertEquals('X', chr);
    assertEquals(SomeEnum.BAR, senum);
    resultHandler.handle(Future.completedFuture("goats"));
  }

  @Override
  public void listStringHandler(Handler<AsyncResult<List<String>>> resultHandler) {
    List<String> list = Arrays.asList("foo", "bar", "wibble");
    resultHandler.handle(Future.completedFuture(list));
  }

  @Override
  public void listByteHandler(Handler<AsyncResult<List<Byte>>> resultHandler) {
    List<Byte> list = Arrays.asList((byte)1, (byte)2, (byte)3);
    resultHandler.handle(Future.completedFuture(list));
  }

  @Override
  public void listShortHandler(Handler<AsyncResult<List<Short>>> resultHandler) {
    List<Short> list = Arrays.asList((short)11, (short)12, (short)13);
    resultHandler.handle(Future.completedFuture(list));
  }

  @Override
  public void listIntHandler(Handler<AsyncResult<List<Integer>>> resultHandler) {
    List<Integer> list = Arrays.asList(100, 101, 102);
    resultHandler.handle(Future.completedFuture(list));
  }

  @Override
  public void listLongHandler(Handler<AsyncResult<List<Long>>> resultHandler) {
    List<Long> list = Arrays.asList(1000l, 1001l, 1002l);
    resultHandler.handle(Future.completedFuture(list));
  }

  @Override
  public void listFloatHandler(Handler<AsyncResult<List<Float>>> resultHandler) {
    List<Float> list = Arrays.asList(1.1f, 1.2f, 1.3f);
    resultHandler.handle(Future.completedFuture(list));
  }

  @Override
  public void listDoubleHandler(Handler<AsyncResult<List<Double>>> resultHandler) {
    List<Double> list = Arrays.asList(1.11d, 1.12d, 1.13d);
    resultHandler.handle(Future.completedFuture(list));
  }

  @Override
  public void listCharHandler(Handler<AsyncResult<List<Character>>> resultHandler) {
    List<Character> list = Arrays.asList('X', 'Y', 'Z');
    resultHandler.handle(Future.completedFuture(list));
  }

  @Override
  public void listBoolHandler(Handler<AsyncResult<List<Boolean>>> resultHandler) {
    List<Boolean> list = Arrays.asList(true, false, true);
    resultHandler.handle(Future.completedFuture(list));
  }

  @Override
  public void listJsonObjectHandler(Handler<AsyncResult<List<JsonObject>>> resultHandler) {
    List<JsonObject> list = Arrays.asList(new JsonObject().putString("a", "foo"),
      new JsonObject().putString("b", "bar"), new JsonObject().putString("c", "wibble"));
    resultHandler.handle(Future.completedFuture(list));
  }

  @Override
  public void listJsonArrayHandler(Handler<AsyncResult<List<JsonArray>>> resultHandler) {
    List<JsonArray> list = Arrays.asList(new JsonArray().add("foo"),
      new JsonArray().add("bar"), new JsonArray().add("wibble"));
    resultHandler.handle(Future.completedFuture(list));
  }

  @Override
  public void ignoredMethod() {
    vertx.eventBus().send(ServiceProxyTest.TEST_ADDRESS, "called");
  }
}

