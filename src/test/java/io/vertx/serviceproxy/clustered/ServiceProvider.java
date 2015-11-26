package io.vertx.serviceproxy.clustered;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.serviceproxy.testmodel.SomeEnum;

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
}
