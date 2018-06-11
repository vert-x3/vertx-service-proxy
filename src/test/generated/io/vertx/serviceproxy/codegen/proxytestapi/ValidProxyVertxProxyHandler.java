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
import io.vertx.core.Vertx;
import io.vertx.core.Handler;
import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.util.Collection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import io.vertx.serviceproxy.ProxyHelper;
import io.vertx.serviceproxy.ProxyHandler;
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
public class ValidProxyVertxProxyHandler extends ProxyHandler {

  public static final long DEFAULT_CONNECTION_TIMEOUT = 5 * 60; // 5 minutes 

  private final Vertx vertx;
  private final ValidProxy service;
  private final long timerID;
  private long lastAccessed;
  private final long timeoutSeconds;

  public ValidProxyVertxProxyHandler(Vertx vertx, ValidProxy service) {
    this(vertx, service, DEFAULT_CONNECTION_TIMEOUT);
  }

  public ValidProxyVertxProxyHandler(Vertx vertx, ValidProxy service, long timeoutInSecond) {
    this(vertx, service, true, timeoutInSecond);
  }

  public ValidProxyVertxProxyHandler(Vertx vertx, ValidProxy service, boolean topLevel, long timeoutSeconds) {
    this.vertx = vertx;
    this.service = service;
    this.timeoutSeconds = timeoutSeconds;
    try {
      this.vertx.eventBus().registerDefaultCodec(ServiceException.class,
          new ServiceExceptionMessageCodec());
    } catch (IllegalStateException ex) {}
    if (timeoutSeconds != -1 && !topLevel) {
      long period = timeoutSeconds * 1000 / 2;
      if (period > 10000) {
        period = 10000;
      }
      this.timerID = vertx.setPeriodic(period, this::checkTimedOut);
    } else {
      this.timerID = -1;
    }
    accessed();
  }

  private void checkTimedOut(long id) {
    long now = System.nanoTime();
    if (now - lastAccessed > timeoutSeconds * 1000000000) {
      service.closeIt();
      close();
    }
  }

  @Override
  public void close() {
    if (timerID != -1) {
      vertx.cancelTimer(timerID);
    }
    super.close();
  }

  private void accessed() {
    this.lastAccessed = System.nanoTime();
  }

  public void handle(Message<JsonObject> msg) {
    try {
      JsonObject json = msg.body();
      String action = msg.headers().get("action");
      if (action == null) {
        throw new IllegalStateException("action not specified");
      }
      accessed();
      switch (action) {
        case "basicTypes": {
          service.basicTypes((java.lang.String)json.getValue("str"), json.getValue("b") == null ? null : (json.getLong("b").byteValue()), json.getValue("s") == null ? null : (json.getLong("s").shortValue()), json.getValue("i") == null ? null : (json.getLong("i").intValue()), json.getValue("l") == null ? null : (json.getLong("l").longValue()), json.getValue("f") == null ? null : (json.getDouble("f").floatValue()), json.getValue("d") == null ? null : (json.getDouble("d").doubleValue()), json.getInteger("c") == null ? null : (char)(int)(json.getInteger("c")), (boolean)json.getValue("bool"));
          break;
        }
        case "basicBoxedTypes": {
          service.basicBoxedTypes((java.lang.String)json.getValue("str"), json.getValue("b") == null ? null : (json.getLong("b").byteValue()), json.getValue("s") == null ? null : (json.getLong("s").shortValue()), json.getValue("i") == null ? null : (json.getLong("i").intValue()), json.getValue("l") == null ? null : (json.getLong("l").longValue()), json.getValue("f") == null ? null : (json.getDouble("f").floatValue()), json.getValue("d") == null ? null : (json.getDouble("d").doubleValue()), json.getInteger("c") == null ? null : (char)(int)(json.getInteger("c")), (java.lang.Boolean)json.getValue("bool"));
          break;
        }
        case "jsonTypes": {
          service.jsonTypes((io.vertx.core.json.JsonObject)json.getValue("jsonObject"), (io.vertx.core.json.JsonArray)json.getValue("jsonArray"));
          break;
        }
        case "methodWithListParams": {
          service.methodWithListParams(convertList(json.getJsonArray("listString").getList()), json.getJsonArray("listByte").stream().map(o -> ((Number)o).byteValue()).collect(Collectors.toList()), json.getJsonArray("listShort").stream().map(o -> ((Number)o).shortValue()).collect(Collectors.toList()), json.getJsonArray("listInt").stream().map(o -> ((Number)o).intValue()).collect(Collectors.toList()), json.getJsonArray("listLong").stream().map(o -> ((Number)o).longValue()).collect(Collectors.toList()), convertList(json.getJsonArray("listJsonObject").getList()), convertList(json.getJsonArray("listJsonArray").getList()));
          break;
        }
        case "methodWithSetParams": {
          service.methodWithSetParams(convertSet(json.getJsonArray("setString").getList()), json.getJsonArray("setByte").stream().map(o -> ((Number)o).byteValue()).collect(Collectors.toSet()), json.getJsonArray("setShort").stream().map(o -> ((Number)o).shortValue()).collect(Collectors.toSet()), json.getJsonArray("setInt").stream().map(o -> ((Number)o).intValue()).collect(Collectors.toSet()), json.getJsonArray("setLong").stream().map(o -> ((Number)o).longValue()).collect(Collectors.toSet()), convertSet(json.getJsonArray("setJsonObject").getList()), convertSet(json.getJsonArray("setJsonArray").getList()));
          break;
        }
        case "methodWithMapParams": {
          service.methodWithMapParams(convertMap(json.getJsonObject("mapString").getMap()), json.getJsonObject("mapByte").getMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> ((java.lang.Number)entry.getValue()).byteValue())), json.getJsonObject("mapShort").getMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> ((java.lang.Number)entry.getValue()).shortValue())), json.getJsonObject("mapInt").getMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> ((java.lang.Number)entry.getValue()).intValue())), json.getJsonObject("mapLong").getMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> ((java.lang.Number)entry.getValue()).longValue())), convertMap(json.getJsonObject("mapJsonObject").getMap()), convertMap(json.getJsonObject("mapJsonArray").getMap()));
          break;
        }
        case "enumType": {
          service.enumType(json.getString("someEnum") == null ? null : io.vertx.serviceproxy.codegen.proxytestapi.SomeEnum.valueOf(json.getString("someEnum")));
          break;
        }
        case "dataObjectType": {
          service.dataObjectType(json.getJsonObject("dataObject") == null ? null : new io.vertx.serviceproxy.codegen.proxytestapi.ProxyDataObject(json.getJsonObject("dataObject")));
          break;
        }
        case "dataObjectWithParentType": {
          service.dataObjectWithParentType(json.getJsonObject("dataObject") == null ? null : new io.vertx.serviceproxy.codegen.proxytestapi.ProxyDataObjectWithParent(json.getJsonObject("dataObject")));
          break;
        }
        case "dataObjectWithParentAndOverride": {
          service.dataObjectWithParentAndOverride(json.getJsonObject("dataObject") == null ? null : new io.vertx.serviceproxy.codegen.proxytestapi.ProxyDataObjectWithParentOverride(json.getJsonObject("dataObject")));
          break;
        }
        case "handler0": {
          service.handler0(createHandler(msg));
          break;
        }
        case "handler1": {
          service.handler1(createHandler(msg));
          break;
        }
        case "handler2": {
          service.handler2(createHandler(msg));
          break;
        }
        case "handler3": {
          service.handler3(createHandler(msg));
          break;
        }
        case "handler4": {
          service.handler4(createHandler(msg));
          break;
        }
        case "handler5": {
          service.handler5(createHandler(msg));
          break;
        }
        case "handler6": {
          service.handler6(createHandler(msg));
          break;
        }
        case "handler7": {
          service.handler7(createHandler(msg));
          break;
        }
        case "handler8": {
          service.handler8(createHandler(msg));
          break;
        }
        case "handler9": {
          service.handler9(createHandler(msg));
          break;
        }
        case "handler10": {
          service.handler10(createHandler(msg));
          break;
        }
        case "handler11": {
          service.handler11(res -> {
            if (res.failed()) {
              if (res.cause() instanceof ServiceException) {
                msg.reply(res.cause());
              } else {
                msg.reply(new ServiceException(-1, res.cause().getMessage()));
              }
            } else {
              msg.reply(res.result() == null ? null : res.result().toJson());
            }
         });
          break;
        }
        case "handler12": {
          service.handler12(createListHandler(msg));
          break;
        }
        case "handler13": {
          service.handler13(createListHandler(msg));
          break;
        }
        case "handler14": {
          service.handler14(createListHandler(msg));
          break;
        }
        case "handler15": {
          service.handler15(createListHandler(msg));
          break;
        }
        case "handler16": {
          service.handler16(createListHandler(msg));
          break;
        }
        case "handler17": {
          service.handler17(createListHandler(msg));
          break;
        }
        case "handler18": {
          service.handler18(createListHandler(msg));
          break;
        }
        case "handler19": {
          service.handler19(createListCharHandler(msg));
          break;
        }
        case "handler20": {
          service.handler20(createListHandler(msg));
          break;
        }
        case "handler21": {
          service.handler21(createListHandler(msg));
          break;
        }
        case "handler22": {
          service.handler22(createListHandler(msg));
          break;
        }
        case "handler24": {
          service.handler24(createSetHandler(msg));
          break;
        }
        case "handler25": {
          service.handler25(createSetHandler(msg));
          break;
        }
        case "handler26": {
          service.handler26(createSetHandler(msg));
          break;
        }
        case "handler27": {
          service.handler27(createSetHandler(msg));
          break;
        }
        case "handler28": {
          service.handler28(createSetHandler(msg));
          break;
        }
        case "handler29": {
          service.handler29(createSetHandler(msg));
          break;
        }
        case "handler30": {
          service.handler30(createSetHandler(msg));
          break;
        }
        case "handler31": {
          service.handler31(createSetCharHandler(msg));
          break;
        }
        case "handler32": {
          service.handler32(createSetHandler(msg));
          break;
        }
        case "handler33": {
          service.handler33(createSetHandler(msg));
          break;
        }
        case "handler34": {
          service.handler34(createSetHandler(msg));
          break;
        }
        case "ignored": {
          service.ignored();
          break;
        }
        case "closeIt": {
          service.closeIt();
          close();
          break;
        }
        case "connection": {
          service.connection((java.lang.String)json.getValue("foo"), res -> {
            if (res.failed()) {
                if (res.cause() instanceof ServiceException) {
                  msg.reply(res.cause());
                } else {
                  msg.reply(new ServiceException(-1, res.cause().getMessage()));
                }
            } else {
              String proxyAddress = UUID.randomUUID().toString();
              ProxyHelper.registerService(ProxyConnection.class, vertx, res.result(), proxyAddress, false, timeoutSeconds);
              msg.reply(null, new DeliveryOptions().addHeader("proxyaddr", proxyAddress));
            }
          });
          break;
        }
        default: {
          throw new IllegalStateException("Invalid action: " + action);
        }
      }
    } catch (Throwable t) {
      msg.reply(new ServiceException(500, t.getMessage()));
      throw t;
    }
  }

  private <T> Handler<AsyncResult<T>> createHandler(Message msg) {
    return res -> {
      if (res.failed()) {
        if (res.cause() instanceof ServiceException) {
          msg.reply(res.cause());
        } else {
          msg.reply(new ServiceException(-1, res.cause().getMessage()));
        }
      } else {
        if (res.result() != null  && res.result().getClass().isEnum()) {
          msg.reply(((Enum) res.result()).name());
        } else {
          msg.reply(res.result());
        }
      }
    };
  }

  private <T> Handler<AsyncResult<List<T>>> createListHandler(Message msg) {
    return res -> {
      if (res.failed()) {
        if (res.cause() instanceof ServiceException) {
          msg.reply(res.cause());
        } else {
          msg.reply(new ServiceException(-1, res.cause().getMessage()));
        }
      } else {
        msg.reply(new JsonArray(res.result()));
      }
    };
  }

  private <T> Handler<AsyncResult<Set<T>>> createSetHandler(Message msg) {
    return res -> {
      if (res.failed()) {
        if (res.cause() instanceof ServiceException) {
          msg.reply(res.cause());
        } else {
          msg.reply(new ServiceException(-1, res.cause().getMessage()));
        }
      } else {
        msg.reply(new JsonArray(new ArrayList<>(res.result())));
      }
    };
  }

  private Handler<AsyncResult<List<Character>>> createListCharHandler(Message msg) {
    return res -> {
      if (res.failed()) {
        if (res.cause() instanceof ServiceException) {
          msg.reply(res.cause());
        } else {
          msg.reply(new ServiceException(-1, res.cause().getMessage()));
        }
      } else {
        JsonArray arr = new JsonArray();
        for (Character chr: res.result()) {
          arr.add((int) chr);
        }
        msg.reply(arr);
      }
    };
  }

  private Handler<AsyncResult<Set<Character>>> createSetCharHandler(Message msg) {
    return res -> {
      if (res.failed()) {
        if (res.cause() instanceof ServiceException) {
          msg.reply(res.cause());
        } else {
          msg.reply(new ServiceException(-1, res.cause().getMessage()));
        }
      } else {
        JsonArray arr = new JsonArray();
        for (Character chr: res.result()) {
          arr.add((int) chr);
        }
        msg.reply(arr);
      }
    };
  }

  private <T> Map<String, T> convertMap(Map map) {
    return (Map<String, T>)map;
  }

  private <T> List<T> convertList(List list) {
    return (List<T>)list;
  }

  private <T> Set<T> convertSet(List list) {
    return new HashSet<T>((List<T>)list);
  }
}