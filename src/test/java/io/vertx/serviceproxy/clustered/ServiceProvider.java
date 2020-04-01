package io.vertx.serviceproxy.clustered;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceException;
import io.vertx.serviceproxy.testmodel.MyServiceException;
import io.vertx.serviceproxy.testmodel.SomeEnum;
import io.vertx.serviceproxy.testmodel.SomeVertxEnum;
import io.vertx.serviceproxy.testmodel.StringDataObject;
import io.vertx.serviceproxy.testmodel.TestDataObject;

import java.util.List;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class ServiceProvider implements Service {
  @Override
  public Service hello(String name, Handler<AsyncResult<String>> result) {
    result.handle(Future.succeededFuture("hello " + name));
    return this;
  }

  @Override
  public Service methodUsingEnum(SomeEnum e, Handler<AsyncResult<Boolean>> result) {
    if (e == SomeEnum.WIBBLE) {
      result.handle(Future.succeededFuture(true));
    } else {
      result.handle(Future.succeededFuture(false));
    }
    return this;
  }

  @Override
  public Service methodReturningEnum(Handler<AsyncResult<SomeEnum>> result) {
    result.handle(Future.succeededFuture(SomeEnum.WIBBLE));
    return this;
  }

  @Override
  public Service methodReturningVertxEnum(Handler<AsyncResult<SomeVertxEnum>> result) {
    result.handle(Future.succeededFuture(SomeVertxEnum.BAR));
    return this;
  }

  @Override
  public Service methodWithJsonObject(JsonObject json, Handler<AsyncResult<JsonObject>> result) {
    result.handle(Future.succeededFuture(json));
    return this;
  }

  @Override
  public Service methodWithJsonArray(JsonArray json, Handler<AsyncResult<JsonArray>> result) {
    result.handle(Future.succeededFuture(json));
    return this;
  }

  @Override
  public Service methodWithList(List<String> list, Handler<AsyncResult<List<String>>> result) {
    result.handle(Future.succeededFuture(list));
    return this;
  }

  @Override
  public Service methodWithDataObject(TestDataObject data, Handler<AsyncResult<TestDataObject>> result) {
    result.handle(Future.succeededFuture(data));
    return this;
  }

  @Override
  public Service methodWithListOfDataObject(List<TestDataObject> list, Handler<AsyncResult<List<TestDataObject>>> result) {
    result.handle(Future.succeededFuture(list));
    return this;
  }

  @Override
  public Service methodWithStringDataObject(StringDataObject data, Handler<AsyncResult<StringDataObject>> result) {
    result.handle(Future.succeededFuture(data));
    return this;
  }

  @Override
  public Service methodWithListOfStringDataObject(List<StringDataObject> list, Handler<AsyncResult<List<StringDataObject>>> result) {
    result.handle(Future.succeededFuture(list));
    return this;
  }

  @Override
  public Service methodWithListOfJsonObject(List<JsonObject> list, Handler<AsyncResult<List<JsonObject>>> result) {
    result.handle(Future.succeededFuture(list));
    return this;
  }

  @Override
  public Service methodWthFailingResult(String input, Handler<AsyncResult<JsonObject>> result) {
    if (input.equals("Fail")) {
      result.handle(ServiceException.fail(30, "failed!", new JsonObject().put("test", "val")));
    } else {
      result.handle(MyServiceException.fail(30, "failed!", "some extra"));
    }
    return this;
  }

  /*@Override
  public Service methodWithMapOfJsonObject(Map<String, JsonObject> map, Handler<AsyncResult<Map<String, JsonObject>>> result) {
    result.handle(Future.succeededFuture(map));
    return this;
  }*/
}
