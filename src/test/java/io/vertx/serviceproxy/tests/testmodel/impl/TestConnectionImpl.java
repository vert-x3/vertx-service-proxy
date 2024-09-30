/*
 * Copyright 2014 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.serviceproxy.tests.testmodel.impl;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.tests.testmodel.TestConnection;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class TestConnectionImpl implements TestConnection {

  private Vertx vertx;
  private String str;

  public TestConnectionImpl(Vertx vertx, String str) {
    this.vertx = vertx;
    this.str = str;
  }

  @Override
  public Future<String> startTransaction() {
    return Future.succeededFuture(str);
  }

  @Override
  public Future<String> insert(String name, JsonObject data) {
    return Future.succeededFuture(str);
  }

  @Override
  public Future<String> commit() {
    return Future.succeededFuture(str);
  }

  @Override
  public Future<String> rollback() {
    return Future.succeededFuture(str);
  }

  @Override
  public void close() {
    vertx.eventBus().send("closeCalled", "blah");
  }
}
