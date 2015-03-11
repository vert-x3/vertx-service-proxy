/*
* Copyright 2014 Red Hat, Inc.
*
* Red Hat licenses this file to you under the Apache License, version 2.0
* (the "License"); you may not use this file except in compliance with the
* License. You may obtain a copy of the License at:
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
* WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
* License for the specific language governing permissions and limitations
* under the License.
*/

package io.vertx.proxygen.testmodel;

import io.vertx.proxygen.testmodel.TestService;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.Vertx;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.util.List;
import java.util.ArrayList;
import io.vertx.proxygen.ProxyHelper;
import io.vertx.proxygen.testmodel.SomeEnum;
import io.vertx.proxygen.testmodel.TestService;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import java.util.List;
import io.vertx.proxygen.testmodel.TestOptions;
import io.vertx.proxygen.testmodel.TestConnection;
import io.vertx.core.json.JsonObject;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/*
  Generated Proxy code - DO NOT EDIT
  @author Roger the Robot
*/
public class TestServiceVertxEBProxy implements TestService {

  private Vertx _vertx;
  private String _address;
  private boolean closed;

  public TestServiceVertxEBProxy(Vertx vertx, String address) {
    this._vertx = vertx;
    this._address = address;
  }

  public TestConnection createConnection(String str) {
    checkClosed();
    JsonObject _json = new JsonObject();
    String addr = java.util.UUID.randomUUID().toString();
    TestConnection proxy = ProxyHelper.createProxy(io.vertx.proxygen.testmodel.TestConnection.class, _vertx, addr);
    _json.put("str", str);
    DeliveryOptions _deliveryOptions = new DeliveryOptions();
    _deliveryOptions.addHeader("action", "createConnection");
    _deliveryOptions.addHeader("newproxyaddr", addr);
    _vertx.eventBus().send(_address, _json, _deliveryOptions);
    return proxy;
  }

  public void noParams() {
    checkClosed();
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = new DeliveryOptions();
    _deliveryOptions.addHeader("action", "noParams");
    _vertx.eventBus().send(_address, _json, _deliveryOptions);
  }

  public void basicTypes(String str, byte b, short s, int i, long l, float f, double d, char c, boolean bool) {
    checkClosed();
    JsonObject _json = new JsonObject();
    _json.put("str", str);
    _json.put("b", b);
    _json.put("s", s);
    _json.put("i", i);
    _json.put("l", l);
    _json.put("f", f);
    _json.put("d", d);
    _json.put("c", (int)c);
    _json.put("bool", bool);
    DeliveryOptions _deliveryOptions = new DeliveryOptions();
    _deliveryOptions.addHeader("action", "basicTypes");
    _vertx.eventBus().send(_address, _json, _deliveryOptions);
  }

  public void basicBoxedTypes(String str, Byte b, Short s, Integer i, Long l, Float f, Double d, Character c, Boolean bool) {
    checkClosed();
    JsonObject _json = new JsonObject();
    _json.put("str", str);
    _json.put("b", b);
    _json.put("s", s);
    _json.put("i", i);
    _json.put("l", l);
    _json.put("f", f);
    _json.put("d", d);
    _json.put("c", (int)c);
    _json.put("bool", bool);
    DeliveryOptions _deliveryOptions = new DeliveryOptions();
    _deliveryOptions.addHeader("action", "basicBoxedTypes");
    _vertx.eventBus().send(_address, _json, _deliveryOptions);
  }

  public void jsonTypes(JsonObject jsonObject, JsonArray jsonArray) {
    checkClosed();
    JsonObject _json = new JsonObject();
    _json.put("jsonObject", jsonObject);
    _json.put("jsonArray", jsonArray);
    DeliveryOptions _deliveryOptions = new DeliveryOptions();
    _deliveryOptions.addHeader("action", "jsonTypes");
    _vertx.eventBus().send(_address, _json, _deliveryOptions);
  }

  public void enumType(SomeEnum someEnum) {
    checkClosed();
    JsonObject _json = new JsonObject();
    _json.put("someEnum", someEnum.toString());
    DeliveryOptions _deliveryOptions = new DeliveryOptions();
    _deliveryOptions.addHeader("action", "enumType");
    _vertx.eventBus().send(_address, _json, _deliveryOptions);
  }

  public void optionType(TestOptions options) {
    checkClosed();
    JsonObject _json = new JsonObject();
    _json.put("options", options.toJson());
    DeliveryOptions _deliveryOptions = new DeliveryOptions();
    _deliveryOptions.addHeader("action", "optionType");
    _vertx.eventBus().send(_address, _json, _deliveryOptions);
  }

  public void stringHandler(Handler<AsyncResult<String>> resultHandler) {
    checkClosed();
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = new DeliveryOptions();
    _deliveryOptions.addHeader("action", "stringHandler");
    _vertx.eventBus().<String>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  public void byteHandler(Handler<AsyncResult<Byte>> resultHandler) {
    checkClosed();
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = new DeliveryOptions();
    _deliveryOptions.addHeader("action", "byteHandler");
    _vertx.eventBus().<Byte>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  public void shortHandler(Handler<AsyncResult<Short>> resultHandler) {
    checkClosed();
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = new DeliveryOptions();
    _deliveryOptions.addHeader("action", "shortHandler");
    _vertx.eventBus().<Short>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  public void intHandler(Handler<AsyncResult<Integer>> resultHandler) {
    checkClosed();
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = new DeliveryOptions();
    _deliveryOptions.addHeader("action", "intHandler");
    _vertx.eventBus().<Integer>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  public void longHandler(Handler<AsyncResult<Long>> resultHandler) {
    checkClosed();
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = new DeliveryOptions();
    _deliveryOptions.addHeader("action", "longHandler");
    _vertx.eventBus().<Long>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  public void floatHandler(Handler<AsyncResult<Float>> resultHandler) {
    checkClosed();
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = new DeliveryOptions();
    _deliveryOptions.addHeader("action", "floatHandler");
    _vertx.eventBus().<Float>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  public void doubleHandler(Handler<AsyncResult<Double>> resultHandler) {
    checkClosed();
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = new DeliveryOptions();
    _deliveryOptions.addHeader("action", "doubleHandler");
    _vertx.eventBus().<Double>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  public void charHandler(Handler<AsyncResult<Character>> resultHandler) {
    checkClosed();
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = new DeliveryOptions();
    _deliveryOptions.addHeader("action", "charHandler");
    _vertx.eventBus().<Character>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  public void booleanHandler(Handler<AsyncResult<Boolean>> resultHandler) {
    checkClosed();
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = new DeliveryOptions();
    _deliveryOptions.addHeader("action", "booleanHandler");
    _vertx.eventBus().<Boolean>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  public void jsonObjectHandler(Handler<AsyncResult<JsonObject>> resultHandler) {
    checkClosed();
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = new DeliveryOptions();
    _deliveryOptions.addHeader("action", "jsonObjectHandler");
    _vertx.eventBus().<JsonObject>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  public void jsonArrayHandler(Handler<AsyncResult<JsonArray>> resultHandler) {
    checkClosed();
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = new DeliveryOptions();
    _deliveryOptions.addHeader("action", "jsonArrayHandler");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  public void voidHandler(Handler<AsyncResult<Void>> resultHandler) {
    checkClosed();
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = new DeliveryOptions();
    _deliveryOptions.addHeader("action", "voidHandler");
    _vertx.eventBus().<Void>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  public TestService fluentMethod(String str, Handler<AsyncResult<String>> resultHandler) {
    checkClosed();
    JsonObject _json = new JsonObject();
    _json.put("str", str);
    DeliveryOptions _deliveryOptions = new DeliveryOptions();
    _deliveryOptions.addHeader("action", "fluentMethod");
    _vertx.eventBus().<String>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
    return this;
  }

  public TestService fluentNoParams() {
    checkClosed();
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = new DeliveryOptions();
    _deliveryOptions.addHeader("action", "fluentNoParams");
    _vertx.eventBus().send(_address, _json, _deliveryOptions);
    return this;
  }

  public void failingMethod(Handler<AsyncResult<JsonObject>> resultHandler) {
    checkClosed();
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = new DeliveryOptions();
    _deliveryOptions.addHeader("action", "failingMethod");
    _vertx.eventBus().<JsonObject>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  public void invokeWithMessage(JsonObject object, String str, int i, char chr, SomeEnum senum, Handler<AsyncResult<String>> resultHandler) {
    checkClosed();
    JsonObject _json = new JsonObject();
    _json.put("object", object);
    _json.put("str", str);
    _json.put("i", i);
    _json.put("chr", (int)chr);
    _json.put("senum", senum.toString());
    DeliveryOptions _deliveryOptions = new DeliveryOptions();
    _deliveryOptions.addHeader("action", "invokeWithMessage");
    _vertx.eventBus().<String>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  public void listStringHandler(Handler<AsyncResult<List<String>>> resultHandler) {
    checkClosed();
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = new DeliveryOptions();
    _deliveryOptions.addHeader("action", "listStringHandler");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body().getList()));
      }
    });
  }

  public void listByteHandler(Handler<AsyncResult<List<Byte>>> resultHandler) {
    checkClosed();
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = new DeliveryOptions();
    _deliveryOptions.addHeader("action", "listByteHandler");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body().getList()));
      }
    });
  }

  public void listShortHandler(Handler<AsyncResult<List<Short>>> resultHandler) {
    checkClosed();
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = new DeliveryOptions();
    _deliveryOptions.addHeader("action", "listShortHandler");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body().getList()));
      }
    });
  }

  public void listIntHandler(Handler<AsyncResult<List<Integer>>> resultHandler) {
    checkClosed();
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = new DeliveryOptions();
    _deliveryOptions.addHeader("action", "listIntHandler");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body().getList()));
      }
    });
  }

  public void listLongHandler(Handler<AsyncResult<List<Long>>> resultHandler) {
    checkClosed();
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = new DeliveryOptions();
    _deliveryOptions.addHeader("action", "listLongHandler");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body().getList()));
      }
    });
  }

  public void listFloatHandler(Handler<AsyncResult<List<Float>>> resultHandler) {
    checkClosed();
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = new DeliveryOptions();
    _deliveryOptions.addHeader("action", "listFloatHandler");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body().getList()));
      }
    });
  }

  public void listDoubleHandler(Handler<AsyncResult<List<Double>>> resultHandler) {
    checkClosed();
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = new DeliveryOptions();
    _deliveryOptions.addHeader("action", "listDoubleHandler");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body().getList()));
      }
    });
  }

  public void listCharHandler(Handler<AsyncResult<List<Character>>> resultHandler) {
    checkClosed();
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = new DeliveryOptions();
    _deliveryOptions.addHeader("action", "listCharHandler");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(convertToListChar(res.result().body())));
      }
    });
  }

  public void listBoolHandler(Handler<AsyncResult<List<Boolean>>> resultHandler) {
    checkClosed();
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = new DeliveryOptions();
    _deliveryOptions.addHeader("action", "listBoolHandler");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body().getList()));
      }
    });
  }

  public void listJsonObjectHandler(Handler<AsyncResult<List<JsonObject>>> resultHandler) {
    checkClosed();
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = new DeliveryOptions();
    _deliveryOptions.addHeader("action", "listJsonObjectHandler");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body().getList()));
      }
    });
  }

  public void listJsonArrayHandler(Handler<AsyncResult<List<JsonArray>>> resultHandler) {
    checkClosed();
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = new DeliveryOptions();
    _deliveryOptions.addHeader("action", "listJsonArrayHandler");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body().getList()));
      }
    });
  }

  public void ignoredMethod() {
  }


  private List<Character> convertToListChar(JsonArray arr) {
    List<Character> list = new ArrayList<>();
    for (Object obj: arr) {
      Integer jobj = (Integer)obj;
      list.add((char)jobj.intValue());
    }
    return list;
  }

  private void checkClosed() {
    if (closed) {
      throw new IllegalStateException("Proxy is closed");
    }
  }
}