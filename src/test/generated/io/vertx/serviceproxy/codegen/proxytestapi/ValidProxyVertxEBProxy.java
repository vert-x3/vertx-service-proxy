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

package io.vertx.serviceproxy.codegen.proxytestapi;

import io.vertx.serviceproxy.codegen.proxytestapi.ValidProxy;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.Vertx;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.function.Function;
import io.vertx.serviceproxy.ProxyHelper;
import io.vertx.serviceproxy.ServiceException;
import io.vertx.serviceproxy.ServiceExceptionMessageCodec;
import io.vertx.serviceproxy.codegen.proxytestapi.ProxyDataObjectWithParent;
import io.vertx.serviceproxy.codegen.proxytestapi.ProxyDataObjectWithParentOverride;
import java.util.Set;
import io.vertx.core.json.JsonArray;
import java.util.List;
import io.vertx.serviceproxy.codegen.proxytestapi.ProxyConnection;
import java.util.Map;
import io.vertx.serviceproxy.codegen.proxytestapi.ProxyDataObject;
import io.vertx.core.json.JsonObject;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.serviceproxy.codegen.proxytestapi.SomeEnum;

/*
  Generated Proxy code - DO NOT EDIT
  @author Roger the Robot
*/
@SuppressWarnings({"unchecked", "rawtypes"})
public class ValidProxyVertxEBProxy implements ValidProxy {

  private Vertx _vertx;
  private String _address;
  private DeliveryOptions _options;
  private boolean closed;

  public ValidProxyVertxEBProxy(Vertx vertx, String address) {
    this(vertx, address, null);
  }

  public ValidProxyVertxEBProxy(Vertx vertx, String address, DeliveryOptions options) {
    this._vertx = vertx;
    this._address = address;
    this._options = options;
    try {
      this._vertx.eventBus().registerDefaultCodec(ServiceException.class,
          new ServiceExceptionMessageCodec());
    } catch (IllegalStateException ex) {}
  }

  @Override
  public void basicTypes(String str, byte b, short s, int i, long l, float f, double d, char c, boolean bool) {
    if (closed) {
    throw new IllegalStateException("Proxy is closed");
  }
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
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "basicTypes");
    _vertx.eventBus().send(_address, _json, _deliveryOptions);
  }

  @Override
  public void basicBoxedTypes(String str, Byte b, Short s, Integer i, Long l, Float f, Double d, Character c, Boolean bool) {
    if (closed) {
    throw new IllegalStateException("Proxy is closed");
  }
    JsonObject _json = new JsonObject();
    _json.put("str", str);
    _json.put("b", b);
    _json.put("s", s);
    _json.put("i", i);
    _json.put("l", l);
    _json.put("f", f);
    _json.put("d", d);
    _json.put("c", c == null ? null : (int)c);
    _json.put("bool", bool);
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "basicBoxedTypes");
    _vertx.eventBus().send(_address, _json, _deliveryOptions);
  }

  @Override
  public void jsonTypes(JsonObject jsonObject, JsonArray jsonArray) {
    if (closed) {
    throw new IllegalStateException("Proxy is closed");
  }
    JsonObject _json = new JsonObject();
    _json.put("jsonObject", jsonObject);
    _json.put("jsonArray", jsonArray);
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "jsonTypes");
    _vertx.eventBus().send(_address, _json, _deliveryOptions);
  }

  @Override
  public void methodWithListParams(List<String> listString, List<Byte> listByte, List<Short> listShort, List<Integer> listInt, List<Long> listLong, List<JsonObject> listJsonObject, List<JsonArray> listJsonArray) {
    if (closed) {
    throw new IllegalStateException("Proxy is closed");
  }
    JsonObject _json = new JsonObject();
    _json.put("listString", new JsonArray(listString));
    _json.put("listByte", new JsonArray(listByte));
    _json.put("listShort", new JsonArray(listShort));
    _json.put("listInt", new JsonArray(listInt));
    _json.put("listLong", new JsonArray(listLong));
    _json.put("listJsonObject", new JsonArray(listJsonObject));
    _json.put("listJsonArray", new JsonArray(listJsonArray));
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "methodWithListParams");
    _vertx.eventBus().send(_address, _json, _deliveryOptions);
  }

  @Override
  public void methodWithSetParams(Set<String> setString, Set<Byte> setByte, Set<Short> setShort, Set<Integer> setInt, Set<Long> setLong, Set<JsonObject> setJsonObject, Set<JsonArray> setJsonArray) {
    if (closed) {
    throw new IllegalStateException("Proxy is closed");
  }
    JsonObject _json = new JsonObject();
    _json.put("setString", new JsonArray(new ArrayList<>(setString)));
    _json.put("setByte", new JsonArray(new ArrayList<>(setByte)));
    _json.put("setShort", new JsonArray(new ArrayList<>(setShort)));
    _json.put("setInt", new JsonArray(new ArrayList<>(setInt)));
    _json.put("setLong", new JsonArray(new ArrayList<>(setLong)));
    _json.put("setJsonObject", new JsonArray(new ArrayList<>(setJsonObject)));
    _json.put("setJsonArray", new JsonArray(new ArrayList<>(setJsonArray)));
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "methodWithSetParams");
    _vertx.eventBus().send(_address, _json, _deliveryOptions);
  }

  @Override
  public void methodWithMapParams(Map<String,String> mapString, Map<String,Byte> mapByte, Map<String,Short> mapShort, Map<String,Integer> mapInt, Map<String,Long> mapLong, Map<String,JsonObject> mapJsonObject, Map<String,JsonArray> mapJsonArray) {
    if (closed) {
    throw new IllegalStateException("Proxy is closed");
  }
    JsonObject _json = new JsonObject();
    _json.put("mapString", new JsonObject(convertMap(mapString)));
    _json.put("mapByte", new JsonObject(convertMap(mapByte)));
    _json.put("mapShort", new JsonObject(convertMap(mapShort)));
    _json.put("mapInt", new JsonObject(convertMap(mapInt)));
    _json.put("mapLong", new JsonObject(convertMap(mapLong)));
    _json.put("mapJsonObject", new JsonObject(convertMap(mapJsonObject)));
    _json.put("mapJsonArray", new JsonObject(convertMap(mapJsonArray)));
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "methodWithMapParams");
    _vertx.eventBus().send(_address, _json, _deliveryOptions);
  }

  @Override
  public void enumType(SomeEnum someEnum) {
    if (closed) {
    throw new IllegalStateException("Proxy is closed");
  }
    JsonObject _json = new JsonObject();
    _json.put("someEnum", someEnum == null ? null : someEnum.toString());
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "enumType");
    _vertx.eventBus().send(_address, _json, _deliveryOptions);
  }

  @Override
  public void dataObjectType(ProxyDataObject dataObject) {
    if (closed) {
    throw new IllegalStateException("Proxy is closed");
  }
    JsonObject _json = new JsonObject();
    _json.put("dataObject", dataObject == null ? null : dataObject.toJson());
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "dataObjectType");
    _vertx.eventBus().send(_address, _json, _deliveryOptions);
  }

  @Override
  public void dataObjectWithParentType(ProxyDataObjectWithParent dataObject) {
    if (closed) {
    throw new IllegalStateException("Proxy is closed");
  }
    JsonObject _json = new JsonObject();
    _json.put("dataObject", dataObject == null ? null : dataObject.toJson());
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "dataObjectWithParentType");
    _vertx.eventBus().send(_address, _json, _deliveryOptions);
  }

  @Override
  public void dataObjectWithParentAndOverride(ProxyDataObjectWithParentOverride dataObject) {
    if (closed) {
    throw new IllegalStateException("Proxy is closed");
  }
    JsonObject _json = new JsonObject();
    _json.put("dataObject", dataObject == null ? null : dataObject.toJson());
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "dataObjectWithParentAndOverride");
    _vertx.eventBus().send(_address, _json, _deliveryOptions);
  }

  @Override
  public void handler0(Handler<AsyncResult<String>> stringHandler) {
    if (closed) {
    stringHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "handler0");
    _vertx.eventBus().<String>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        stringHandler.handle(Future.failedFuture(res.cause()));
      } else {
        stringHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  @Override
  public void handler1(Handler<AsyncResult<Byte>> byteHandler) {
    if (closed) {
    byteHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "handler1");
    _vertx.eventBus().<Byte>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        byteHandler.handle(Future.failedFuture(res.cause()));
      } else {
        byteHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  @Override
  public void handler2(Handler<AsyncResult<Short>> shortHandler) {
    if (closed) {
    shortHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "handler2");
    _vertx.eventBus().<Short>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        shortHandler.handle(Future.failedFuture(res.cause()));
      } else {
        shortHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  @Override
  public void handler3(Handler<AsyncResult<Integer>> intHandler) {
    if (closed) {
    intHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "handler3");
    _vertx.eventBus().<Integer>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        intHandler.handle(Future.failedFuture(res.cause()));
      } else {
        intHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  @Override
  public void handler4(Handler<AsyncResult<Long>> longHandler) {
    if (closed) {
    longHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "handler4");
    _vertx.eventBus().<Long>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        longHandler.handle(Future.failedFuture(res.cause()));
      } else {
        longHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  @Override
  public void handler5(Handler<AsyncResult<Float>> floatHandler) {
    if (closed) {
    floatHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "handler5");
    _vertx.eventBus().<Float>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        floatHandler.handle(Future.failedFuture(res.cause()));
      } else {
        floatHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  @Override
  public void handler6(Handler<AsyncResult<Double>> doubleHandler) {
    if (closed) {
    doubleHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "handler6");
    _vertx.eventBus().<Double>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        doubleHandler.handle(Future.failedFuture(res.cause()));
      } else {
        doubleHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  @Override
  public void handler7(Handler<AsyncResult<Character>> charHandler) {
    if (closed) {
    charHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "handler7");
    _vertx.eventBus().<Character>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        charHandler.handle(Future.failedFuture(res.cause()));
      } else {
        charHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  @Override
  public void handler8(Handler<AsyncResult<Boolean>> boolHandler) {
    if (closed) {
    boolHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "handler8");
    _vertx.eventBus().<Boolean>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        boolHandler.handle(Future.failedFuture(res.cause()));
      } else {
        boolHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  @Override
  public void handler9(Handler<AsyncResult<JsonObject>> jsonObjectHandler) {
    if (closed) {
    jsonObjectHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "handler9");
    _vertx.eventBus().<JsonObject>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        jsonObjectHandler.handle(Future.failedFuture(res.cause()));
      } else {
        jsonObjectHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  @Override
  public void handler10(Handler<AsyncResult<JsonArray>> jsonArrayHandler) {
    if (closed) {
    jsonArrayHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "handler10");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        jsonArrayHandler.handle(Future.failedFuture(res.cause()));
      } else {
        jsonArrayHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  @Override
  public void handler11(Handler<AsyncResult<ProxyDataObject>> dataObjectHandler) {
    if (closed) {
    dataObjectHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "handler11");
    _vertx.eventBus().<JsonObject>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        dataObjectHandler.handle(Future.failedFuture(res.cause()));
      } else {
        dataObjectHandler.handle(Future.succeededFuture(res.result().body() == null ? null : new ProxyDataObject(res.result().body())));
                      }
    });
  }

  @Override
  public void handler12(Handler<AsyncResult<List<String>>> stringListHandler) {
    if (closed) {
    stringListHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "handler12");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        stringListHandler.handle(Future.failedFuture(res.cause()));
      } else {
        stringListHandler.handle(Future.succeededFuture(convertList(res.result().body().getList())));
      }
    });
  }

  @Override
  public void handler13(Handler<AsyncResult<List<Byte>>> byteListHandler) {
    if (closed) {
    byteListHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "handler13");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        byteListHandler.handle(Future.failedFuture(res.cause()));
      } else {
        byteListHandler.handle(Future.succeededFuture(convertList(res.result().body().getList())));
      }
    });
  }

  @Override
  public void handler14(Handler<AsyncResult<List<Short>>> shortListHandler) {
    if (closed) {
    shortListHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "handler14");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        shortListHandler.handle(Future.failedFuture(res.cause()));
      } else {
        shortListHandler.handle(Future.succeededFuture(convertList(res.result().body().getList())));
      }
    });
  }

  @Override
  public void handler15(Handler<AsyncResult<List<Integer>>> intListHandler) {
    if (closed) {
    intListHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "handler15");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        intListHandler.handle(Future.failedFuture(res.cause()));
      } else {
        intListHandler.handle(Future.succeededFuture(convertList(res.result().body().getList())));
      }
    });
  }

  @Override
  public void handler16(Handler<AsyncResult<List<Long>>> longListHandler) {
    if (closed) {
    longListHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "handler16");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        longListHandler.handle(Future.failedFuture(res.cause()));
      } else {
        longListHandler.handle(Future.succeededFuture(convertList(res.result().body().getList())));
      }
    });
  }

  @Override
  public void handler17(Handler<AsyncResult<List<Float>>> floatListHandler) {
    if (closed) {
    floatListHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "handler17");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        floatListHandler.handle(Future.failedFuture(res.cause()));
      } else {
        floatListHandler.handle(Future.succeededFuture(convertList(res.result().body().getList())));
      }
    });
  }

  @Override
  public void handler18(Handler<AsyncResult<List<Double>>> doubleListHandler) {
    if (closed) {
    doubleListHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "handler18");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        doubleListHandler.handle(Future.failedFuture(res.cause()));
      } else {
        doubleListHandler.handle(Future.succeededFuture(convertList(res.result().body().getList())));
      }
    });
  }

  @Override
  public void handler19(Handler<AsyncResult<List<Character>>> charListHandler) {
    if (closed) {
    charListHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "handler19");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        charListHandler.handle(Future.failedFuture(res.cause()));
      } else {
        charListHandler.handle(Future.succeededFuture(convertToListChar(res.result().body())));
      }
    });
  }

  @Override
  public void handler20(Handler<AsyncResult<List<Boolean>>> boolListHandler) {
    if (closed) {
    boolListHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "handler20");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        boolListHandler.handle(Future.failedFuture(res.cause()));
      } else {
        boolListHandler.handle(Future.succeededFuture(convertList(res.result().body().getList())));
      }
    });
  }

  @Override
  public void handler21(Handler<AsyncResult<List<JsonObject>>> jsonObjectListHandler) {
    if (closed) {
    jsonObjectListHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "handler21");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        jsonObjectListHandler.handle(Future.failedFuture(res.cause()));
      } else {
        jsonObjectListHandler.handle(Future.succeededFuture(convertList(res.result().body().getList())));
      }
    });
  }

  @Override
  public void handler22(Handler<AsyncResult<List<JsonArray>>> jsonArrayListHandler) {
    if (closed) {
    jsonArrayListHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "handler22");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        jsonArrayListHandler.handle(Future.failedFuture(res.cause()));
      } else {
        jsonArrayListHandler.handle(Future.succeededFuture(convertList(res.result().body().getList())));
      }
    });
  }

  @Override
  public void handler24(Handler<AsyncResult<Set<String>>> stringSetHandler) {
    if (closed) {
    stringSetHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "handler24");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        stringSetHandler.handle(Future.failedFuture(res.cause()));
      } else {
        stringSetHandler.handle(Future.succeededFuture(convertSet(res.result().body().getList())));
      }
    });
  }

  @Override
  public void handler25(Handler<AsyncResult<Set<Byte>>> byteSetHandler) {
    if (closed) {
    byteSetHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "handler25");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        byteSetHandler.handle(Future.failedFuture(res.cause()));
      } else {
        byteSetHandler.handle(Future.succeededFuture(convertSet(res.result().body().getList())));
      }
    });
  }

  @Override
  public void handler26(Handler<AsyncResult<Set<Short>>> shortSetHandler) {
    if (closed) {
    shortSetHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "handler26");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        shortSetHandler.handle(Future.failedFuture(res.cause()));
      } else {
        shortSetHandler.handle(Future.succeededFuture(convertSet(res.result().body().getList())));
      }
    });
  }

  @Override
  public void handler27(Handler<AsyncResult<Set<Integer>>> intSetHandler) {
    if (closed) {
    intSetHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "handler27");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        intSetHandler.handle(Future.failedFuture(res.cause()));
      } else {
        intSetHandler.handle(Future.succeededFuture(convertSet(res.result().body().getList())));
      }
    });
  }

  @Override
  public void handler28(Handler<AsyncResult<Set<Long>>> longSetHandler) {
    if (closed) {
    longSetHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "handler28");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        longSetHandler.handle(Future.failedFuture(res.cause()));
      } else {
        longSetHandler.handle(Future.succeededFuture(convertSet(res.result().body().getList())));
      }
    });
  }

  @Override
  public void handler29(Handler<AsyncResult<Set<Float>>> floatSetHandler) {
    if (closed) {
    floatSetHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "handler29");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        floatSetHandler.handle(Future.failedFuture(res.cause()));
      } else {
        floatSetHandler.handle(Future.succeededFuture(convertSet(res.result().body().getList())));
      }
    });
  }

  @Override
  public void handler30(Handler<AsyncResult<Set<Double>>> doubleSetHandler) {
    if (closed) {
    doubleSetHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "handler30");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        doubleSetHandler.handle(Future.failedFuture(res.cause()));
      } else {
        doubleSetHandler.handle(Future.succeededFuture(convertSet(res.result().body().getList())));
      }
    });
  }

  @Override
  public void handler31(Handler<AsyncResult<Set<Character>>> charSetHandler) {
    if (closed) {
    charSetHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "handler31");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        charSetHandler.handle(Future.failedFuture(res.cause()));
      } else {
        charSetHandler.handle(Future.succeededFuture(convertToSetChar(res.result().body())));
      }
    });
  }

  @Override
  public void handler32(Handler<AsyncResult<Set<Boolean>>> boolSetHandler) {
    if (closed) {
    boolSetHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "handler32");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        boolSetHandler.handle(Future.failedFuture(res.cause()));
      } else {
        boolSetHandler.handle(Future.succeededFuture(convertSet(res.result().body().getList())));
      }
    });
  }

  @Override
  public void handler33(Handler<AsyncResult<Set<JsonObject>>> jsonObjectSetHandler) {
    if (closed) {
    jsonObjectSetHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "handler33");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        jsonObjectSetHandler.handle(Future.failedFuture(res.cause()));
      } else {
        jsonObjectSetHandler.handle(Future.succeededFuture(convertSet(res.result().body().getList())));
      }
    });
  }

  @Override
  public void handler34(Handler<AsyncResult<Set<JsonArray>>> jsonArraySetHandler) {
    if (closed) {
    jsonArraySetHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "handler34");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        jsonArraySetHandler.handle(Future.failedFuture(res.cause()));
      } else {
        jsonArraySetHandler.handle(Future.succeededFuture(convertSet(res.result().body().getList())));
      }
    });
  }

  @Override
  public void ignored() {
  }

  @Override
  public void closeIt() {
    if (closed) {
    throw new IllegalStateException("Proxy is closed");
  }
    closed = true;
  JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "closeIt");
    _vertx.eventBus().send(_address, _json, _deliveryOptions);
  }

  @Override
  public void connection(String foo, Handler<AsyncResult<ProxyConnection>> resultHandler) {
    if (closed) {
    resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    _json.put("foo", foo);
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "connection");
    _vertx.eventBus().<ProxyConnection>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        String addr = res.result().headers().get("proxyaddr");
        resultHandler.handle(Future.succeededFuture(ProxyHelper.createProxy(ProxyConnection.class, _vertx, addr)));
      }
    });
  }


  private List<Character> convertToListChar(JsonArray arr) {
    List<Character> list = new ArrayList<>();
    for (Object obj: arr) {
      Integer jobj = (Integer)obj;
      list.add((char)(int)jobj);
    }
    return list;
  }

  private Set<Character> convertToSetChar(JsonArray arr) {
    Set<Character> set = new HashSet<>();
    for (Object obj: arr) {
      Integer jobj = (Integer)obj;
      set.add((char)(int)jobj);
    }
    return set;
  }

  private <T> Map<String, T> convertMap(Map map) {
    if (map.isEmpty()) { 
      return (Map<String, T>) map; 
    } 
     
    Object elem = map.values().stream().findFirst().get(); 
    if (!(elem instanceof Map) && !(elem instanceof List)) { 
      return (Map<String, T>) map; 
    } else { 
      Function<Object, T> converter; 
      if (elem instanceof List) { 
        converter = object -> (T) new JsonArray((List) object); 
      } else { 
        converter = object -> (T) new JsonObject((Map) object); 
      } 
      return ((Map<String, T>) map).entrySet() 
       .stream() 
       .collect(Collectors.toMap(Map.Entry::getKey, converter::apply)); 
    } 
  }
  private <T> List<T> convertList(List list) {
    if (list.isEmpty()) { 
          return (List<T>) list; 
        } 
     
    Object elem = list.get(0); 
    if (!(elem instanceof Map) && !(elem instanceof List)) { 
      return (List<T>) list; 
    } else { 
      Function<Object, T> converter; 
      if (elem instanceof List) { 
        converter = object -> (T) new JsonArray((List) object); 
      } else { 
        converter = object -> (T) new JsonObject((Map) object); 
      } 
      return (List<T>) list.stream().map(converter).collect(Collectors.toList()); 
    } 
  }
  private <T> Set<T> convertSet(List list) {
    return new HashSet<T>(convertList(list));
  }
}