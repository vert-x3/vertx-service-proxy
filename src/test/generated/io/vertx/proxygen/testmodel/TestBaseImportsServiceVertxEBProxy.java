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

import io.vertx.proxygen.testmodel.TestBaseImportsService;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.Vertx;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.util.List;
import java.util.ArrayList;

/*
  Generated Proxy code - DO NOT EDIT
  @author Roger the Robot
*/
public class TestBaseImportsServiceVertxEBProxy implements TestBaseImportsService {

  private Vertx _vertx;
  private String _address;

  public TestBaseImportsServiceVertxEBProxy(Vertx vertx, String address) {
    this._vertx = vertx;
    this._address = address;
  }

  public void m() {
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = new DeliveryOptions();
    _deliveryOptions.addHeader("action", "m");
    _vertx.eventBus().send(_address, _json, _deliveryOptions);
  }


  // This is clunky, but will disappear once we refactor JsonObject to be a map
  private List<JsonObject> convertToListJsonObject(JsonArray arr) {
    List<JsonObject> list = new ArrayList<>();
    for (Object obj: arr) {
      JsonObject jobj = (JsonObject)obj;
      list.add(jobj);
    }
    return list;
  }
  // This is clunky, but will disappear once we refactor Json stuff
  private List<JsonArray> convertToListJsonArray(JsonArray arr) {
    List<JsonArray> list = new ArrayList<>();
    for (Object obj: arr) {
      JsonArray jobj = (JsonArray)obj;
      list.add(jobj);
    }
    return list;
  }
  private List<Character> convertToListChar(JsonArray arr) {
    List<Character> list = new ArrayList<>();
    for (Object obj: arr) {
      Integer jobj = (Integer)obj;
      list.add((char)jobj.intValue());
    }
    return list;
  }
}