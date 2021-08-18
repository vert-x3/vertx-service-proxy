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

package io.vertx.serviceproxy.testmodel.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.testmodel.TestConnection;
import io.vertx.serviceproxy.testmodel.sub.TestSubConnection;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class TestSubConnectionImpl implements TestSubConnection {

  private Vertx vertx;
  private String str;

  public TestSubConnectionImpl(Vertx vertx, String str) {
    this.vertx = vertx;
    this.str = str;
  }
  @Override
  public void close() {
    vertx.eventBus().send("closeCalled", "bluh");
  }
}
