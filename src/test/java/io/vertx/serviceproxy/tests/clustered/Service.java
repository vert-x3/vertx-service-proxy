package io.vertx.serviceproxy.tests.clustered;


import java.util.List;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.tests.testmodel.SomeEnum;
import io.vertx.serviceproxy.tests.testmodel.SomeEnumWithCustomConstructor;
import io.vertx.serviceproxy.tests.testmodel.SomeVertxEnum;
import io.vertx.serviceproxy.tests.testmodel.StringDataObject;
import io.vertx.serviceproxy.tests.testmodel.TestDataObject;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@ProxyGen
@VertxGen
public interface Service {

  static Service createProxy(Vertx vertx, String address) {
    return new ServiceVertxEBProxy(vertx, address);
  }

  Future<String> hello(String name);

  Future<Boolean> methodUsingEnum(SomeEnum e);

  Future<SomeEnum> methodReturningEnum();

  Future<SomeVertxEnum> methodReturningVertxEnum();

  Future<Boolean> methodUsingCustomEnum(SomeEnumWithCustomConstructor e);

  Future<SomeEnumWithCustomConstructor> methodReturningCustomEnum();

  Future<JsonObject> methodWithJsonObject(JsonObject json);

  Future<JsonArray> methodWithJsonArray(JsonArray json);

  Future<List<String>> methodWithList(List<String> list);

  Future<TestDataObject> methodWithDataObject(TestDataObject data);

  Future<List<TestDataObject>> methodWithListOfDataObject(List<TestDataObject> list);

  Future<StringDataObject> methodWithStringDataObject(StringDataObject data);

  Future<List<StringDataObject>> methodWithListOfStringDataObject(List<StringDataObject> list);


  Future<List<JsonObject>> methodWithListOfJsonObject(List<JsonObject> list);

  Future<JsonObject> methodWthFailingResult(String input);

}
