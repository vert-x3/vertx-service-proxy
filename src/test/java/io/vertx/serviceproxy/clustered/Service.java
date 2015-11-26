package io.vertx.serviceproxy.clustered;


import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.serviceproxy.ProxyHelper;
import io.vertx.serviceproxy.testmodel.SomeEnum;

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

}
