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
import io.vertx.core.Vertx;
import io.vertx.core.Handler;
import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.util.List;
import io.vertx.proxygen.testmodel.SomeEnum;
import io.vertx.proxygen.testmodel.TestService;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import java.util.List;
import io.vertx.proxygen.testmodel.TestOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/*
  Generated Proxy code - DO NOT EDIT
  @author Roger the Robot
*/
public class TestServiceVertxProxyHandler implements Handler<Message<JsonObject>> {

  private Vertx vertx;
  private TestService service;

  public TestServiceVertxProxyHandler(Vertx vertx, TestService service) {
    this.vertx = vertx;
    this.service = service;
  }

  public void handle(Message<JsonObject> msg) {
    JsonObject json = msg.body();
    String action = msg.headers().get("action");
    if (action == null) {
      throw new IllegalStateException("action not specified");
    }
    switch (action) {


      case "noParams": {
        service.noParams();
        break;
      }
      case "basicTypes": {
        service.basicTypes((java.lang.String)json.getValue("str"), (byte)json.getValue("b"), (short)json.getValue("s"), (int)json.getValue("i"), (long)json.getValue("l"), (float)json.getValue("f"), (double)json.getValue("d"), (char)(json.getInteger("c").intValue()), (boolean)json.getValue("bool"));
        break;
      }
      case "basicBoxedTypes": {
        service.basicBoxedTypes((java.lang.String)json.getValue("str"), (java.lang.Byte)json.getValue("b"), (java.lang.Short)json.getValue("s"), (java.lang.Integer)json.getValue("i"), (java.lang.Long)json.getValue("l"), (java.lang.Float)json.getValue("f"), (java.lang.Double)json.getValue("d"), (char)(json.getInteger("c").intValue()), (java.lang.Boolean)json.getValue("bool"));
        break;
      }
      case "jsonTypes": {
        service.jsonTypes((io.vertx.core.json.JsonObject)json.getValue("jsonObject"), (io.vertx.core.json.JsonArray)json.getValue("jsonArray"));
        break;
      }
      case "enumType": {
        service.enumType(io.vertx.proxygen.testmodel.SomeEnum.valueOf(json.getString("someEnum")));
        break;
      }
      case "optionType": {
        service.optionType(new io.vertx.proxygen.testmodel.TestOptions(json.getJsonObject("options")));
        break;
      }
      case "stringHandler": {
        service.stringHandler(createHandler(msg));
        break;
      }
      case "byteHandler": {
        service.byteHandler(createHandler(msg));
        break;
      }
      case "shortHandler": {
        service.shortHandler(createHandler(msg));
        break;
      }
      case "intHandler": {
        service.intHandler(createHandler(msg));
        break;
      }
      case "longHandler": {
        service.longHandler(createHandler(msg));
        break;
      }
      case "floatHandler": {
        service.floatHandler(createHandler(msg));
        break;
      }
      case "doubleHandler": {
        service.doubleHandler(createHandler(msg));
        break;
      }
      case "charHandler": {
        service.charHandler(createHandler(msg));
        break;
      }
      case "booleanHandler": {
        service.booleanHandler(createHandler(msg));
        break;
      }
      case "jsonObjectHandler": {
        service.jsonObjectHandler(createHandler(msg));
        break;
      }
      case "jsonArrayHandler": {
        service.jsonArrayHandler(createHandler(msg));
        break;
      }
      case "voidHandler": {
        service.voidHandler(createHandler(msg));
        break;
      }
      case "fluentMethod": {
        service.fluentMethod((java.lang.String)json.getValue("str"), createHandler(msg));
        break;
      }
      case "fluentNoParams": {
        service.fluentNoParams();
        break;
      }
      case "failingMethod": {
        service.failingMethod(createHandler(msg));
        break;
      }
      case "invokeWithMessage": {
        service.invokeWithMessage((io.vertx.core.json.JsonObject)json.getValue("object"), (java.lang.String)json.getValue("str"), (int)json.getValue("i"), (char)(json.getInteger("chr").intValue()), io.vertx.proxygen.testmodel.SomeEnum.valueOf(json.getString("senum")), createHandler(msg));
        break;
      }
      case "listStringHandler": {
        service.listStringHandler(createListHandler(msg));
        break;
      }
      case "listByteHandler": {
        service.listByteHandler(createListHandler(msg));
        break;
      }
      case "listShortHandler": {
        service.listShortHandler(createListHandler(msg));
        break;
      }
      case "listIntHandler": {
        service.listIntHandler(createListHandler(msg));
        break;
      }
      case "listLongHandler": {
        service.listLongHandler(createListHandler(msg));
        break;
      }
      case "listFloatHandler": {
        service.listFloatHandler(createListHandler(msg));
        break;
      }
      case "listDoubleHandler": {
        service.listDoubleHandler(createListHandler(msg));
        break;
      }
      case "listCharHandler": {
        service.listCharHandler(createListCharHandler(msg));
        break;
      }
      case "listBoolHandler": {
        service.listBoolHandler(createListHandler(msg));
        break;
      }
      case "listJsonObjectHandler": {
        service.listJsonObjectHandler(createListJsonObjectHandler(msg));
        break;
      }
      case "listJsonArrayHandler": {
        service.listJsonArrayHandler(createListJsonArrayHandler(msg));
        break;
      }
      case "ignoredMethod": {
        service.ignoredMethod();
        break;
      }
      default: {
        throw new IllegalStateException("Invalid action: " + action);
      }
    }
  }
  private <T> Handler<AsyncResult<T>> createHandler(Message msg) {
    return res -> {
      if (res.failed()) {
        msg.fail(-1, res.cause().getMessage());
      } else {
        msg.reply(res.result());
      }
    };
  }
  private <T> Handler<AsyncResult<List<T>>> createListHandler(Message msg) {
    return res -> {
      if (res.failed()) {
        msg.fail(-1, res.cause().getMessage());
      } else {
        msg.reply(new JsonArray(res.result()));
      }
    };
  }
  // This is clunky, but will disappear once we refactor JsonObject to be a map
  private Handler<AsyncResult<List<JsonObject>>> createListJsonObjectHandler(Message msg) {
    return res -> {
      if (res.failed()) {
        msg.fail(-1, res.cause().getMessage());
      } else {
        JsonArray arr = new JsonArray();
        for (JsonObject obj: res.result()) {
          arr.add(obj);
        }
        msg.reply(arr);
      }
    };
  }
  // This is clunky, but will disappear once we refactor JsonArray to be a list
  private Handler<AsyncResult<List<JsonArray>>> createListJsonArrayHandler(Message msg) {
    return res -> {
      if (res.failed()) {
        msg.fail(-1, res.cause().getMessage());
      } else {
        JsonArray arr = new JsonArray();
        for (JsonArray obj: res.result()) {
          arr.add(obj);
        }
        msg.reply(arr);
      }
    };
  }
  private Handler<AsyncResult<List<Character>>> createListCharHandler(Message msg) {
    return res -> {
      if (res.failed()) {
        msg.fail(-1, res.cause().getMessage());
      } else {
        JsonArray arr = new JsonArray();
        for (Character chr: res.result()) {
          arr.add((int)chr);
        }
        msg.reply(arr);
      }
    };
  }
}