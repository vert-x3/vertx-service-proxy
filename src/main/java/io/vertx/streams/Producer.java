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
package io.vertx.streams;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import io.vertx.streams.impl.ProducerImpl;
import io.vertx.streams.impl.Transport;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@VertxGen
public interface Producer<T> {

  static <T> Producer<T> producer(EventBus bus) {
    return new ProducerImpl<>(bus);
  }

  static <T> Producer<T> producer(EventBus bus, Transport transport) {
    return new ProducerImpl<>(bus, transport);
  }

  @Fluent
  Producer<T> readStreamHandler(Handler<WriteStream<T>> handler);

  @Fluent
  Producer<T> writeStreamHandler(Handler<ReadStream<T>> handler);

  void register(String address);

}
