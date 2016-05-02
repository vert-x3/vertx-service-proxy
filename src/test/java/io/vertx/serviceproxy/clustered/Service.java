package io.vertx.serviceproxy.clustered;


import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ProxyHelper;
import io.vertx.serviceproxy.testmodel.SomeEnum;
import io.vertx.serviceproxy.testmodel.SomeVertxEnum;
import io.vertx.serviceproxy.testmodel.TestDataObject;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@ProxyGen
@VertxGen
public interface Service {

  static Service createProxy(Vertx vertx, String address) {
    return ProxyHelper.createProxy(Service.class, vertx, address);
  }

  @Fluent
  Service hello(String name, Handler<AsyncResult<String>> result);

  @Fluent
  Service methodUsingEnum(SomeEnum e, Handler<AsyncResult<Boolean>> result);

  @Fluent
  Service methodReturningEnum(Handler<AsyncResult<SomeEnum>> result);

  @Fluent
  Service methodReturningVertxEnum(Handler<AsyncResult<SomeVertxEnum>> result);


  @Fluent
  Service methodWithJsonObject(JsonObject json, Handler<AsyncResult<JsonObject>> result);

  @Fluent
  Service methodWithJsonArray(JsonArray json, Handler<AsyncResult<JsonArray>> result);

  @Fluent
  Service methodWithList(List<String> list, Handler<AsyncResult<List<String>>> result);

  @Fluent
  Service methodWithDataObject(TestDataObject data,
                                     Handler<AsyncResult<TestDataObject>> result);

  @Fluent
  Service methodWithListOfDataObject(List<TestDataObject> list,
                                     Handler<AsyncResult<List<TestDataObject>>> result);

  @Fluent
  Service methodWithListOfJsonObject(List<JsonObject> list,
                                     Handler<AsyncResult<List<JsonObject>>> result);

  @Fluent
  Service methodWthFailingResult(String input, Handler<AsyncResult<JsonObject>> result);

  /*@Fluent
  Service methodWithMapOfJsonObject(Map<String, JsonObject> map,
                                    Handler<AsyncResult<Map<String, JsonObject>>> result);
*/
}
