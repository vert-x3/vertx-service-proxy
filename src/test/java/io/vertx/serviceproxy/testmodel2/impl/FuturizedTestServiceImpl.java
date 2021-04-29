package io.vertx.serviceproxy.testmodel2.impl;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.serviceproxy.testmodel2.FuturizedTestService;

public class FuturizedTestServiceImpl implements FuturizedTestService {

  private final Vertx vertx;

  public FuturizedTestServiceImpl(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  public Future<String> ok() {
    return Future.succeededFuture("foobar");
  }
}
