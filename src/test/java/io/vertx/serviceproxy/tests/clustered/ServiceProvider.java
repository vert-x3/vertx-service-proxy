package io.vertx.serviceproxy.tests.clustered;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceException;
import io.vertx.serviceproxy.tests.testmodel.MyServiceException;
import io.vertx.serviceproxy.tests.testmodel.SomeEnum;
import io.vertx.serviceproxy.tests.testmodel.SomeEnumWithCustomConstructor;
import io.vertx.serviceproxy.tests.testmodel.SomeVertxEnum;
import io.vertx.serviceproxy.tests.testmodel.StringDataObject;
import io.vertx.serviceproxy.tests.testmodel.TestDataObject;

import java.util.List;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class ServiceProvider implements Service {

  @Override
  public Future<String> hello(String name) {
    return Future.succeededFuture("hello " + name);
  }

  @Override
  public Future<Boolean> methodUsingEnum(SomeEnum e) {
    if (e == SomeEnum.WIBBLE) {
      return Future.succeededFuture(true);
    } else {
      return Future.succeededFuture(false);
    }
  }

  @Override
  public Future<SomeEnum> methodReturningEnum() {
    return Future.succeededFuture(SomeEnum.WIBBLE);
  }

  @Override
  public Future<SomeVertxEnum> methodReturningVertxEnum() {
    return Future.succeededFuture(SomeVertxEnum.BAR);
  }

  @Override
  public Future<JsonObject> methodWithJsonObject(JsonObject json) {
    return Future.succeededFuture(json);
  }

  @Override
  public Future<JsonArray> methodWithJsonArray(JsonArray json) {
    return Future.succeededFuture(json);
  }

  @Override
  public Future<List<String>> methodWithList(List<String> list) {
    return Future.succeededFuture(list);
  }

  @Override
  public Future<TestDataObject> methodWithDataObject(TestDataObject data) {
    return Future.succeededFuture(data);
  }

  @Override
  public Future<List<TestDataObject>> methodWithListOfDataObject(List<TestDataObject> list) {
    return Future.succeededFuture(list);
  }

  @Override
  public Future<StringDataObject> methodWithStringDataObject(StringDataObject data) {
    return Future.succeededFuture(data);
  }

  @Override
  public Future<List<StringDataObject>> methodWithListOfStringDataObject(List<StringDataObject> list) {
    return Future.succeededFuture(list);
  }

  @Override
  public Future<List<JsonObject>> methodWithListOfJsonObject(List<JsonObject> list) {
    return Future.succeededFuture(list);
  }

  @Override
  public Future<JsonObject> methodWthFailingResult(String input) {
    if (input.equals("Fail")) {
      return ServiceException.fail(30, "failed!", new JsonObject().put("test", "val"));
    } else {
      return MyServiceException.fail(30, "failed!", "some extra");
    }
  }

  @Override
  public Future<Boolean> methodUsingCustomEnum(SomeEnumWithCustomConstructor e) {
    if (e == SomeEnumWithCustomConstructor.ITEST) {
      return Future.succeededFuture(true);
    } else {
      return Future.succeededFuture(false);
    }
  }

  @Override
  public Future<SomeEnumWithCustomConstructor> methodReturningCustomEnum() {
    return Future.succeededFuture(SomeEnumWithCustomConstructor.DEV);
  }

  /*@Override
  public Service methodWithMapOfJsonObject(Map<String, JsonObject> map, Handler<AsyncResult<Map<String, JsonObject>>> result) {
    result.handle(Future.succeededFuture(map));
    return this;
  }*/
}
