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
package io.vertx.streams.impl;

import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import io.vertx.streams.Producer;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class ProducerImpl<T> implements Producer<T>, Handler<Message<Object>> {

  private final EventBus bus;
  private Handler<WriteStream<T>> readStreamHandler;
  private Handler<ReadStream<T>> writeStreamHandler;
  private StreamProducerManager<T> mgr;

  public ProducerImpl(EventBus bus, Transport transport) {
    this.bus = bus;
    this.mgr = new StreamProducerManager<>(transport);
  }

  public ProducerImpl(EventBus bus) {
    this(bus, new EventBusTransport(bus));
  }

  @Override
  public Producer<T> readStreamHandler(Handler<WriteStream<T>> handler) {
    this.readStreamHandler = handler;
    return this;
  }

  @Override
  public Producer<T> writeStreamHandler(Handler<ReadStream<T>> handler) {
    this.writeStreamHandler = handler;
    return this;
  }

  @Override
  public void handle(Message<Object> msg) {
    String addr = msg.headers().get("addr");
    String action = msg.headers().get("stream");
    if (action != null) {
      switch (action) {
        case "open":
          if (addr != null) {
            mgr.openReadStream(addr, ar -> {
              if (ar.succeeded()) {
                readStreamHandler.handle(ar.result());
                msg.reply(null);
              } else {
                // Something else ?
                msg.fail(0, ar.cause().getMessage());
              }
            });
          } else {
            addr = mgr.<T>openWriteStream(ar -> {
              if (ar.succeeded()) {
                writeStreamHandler.handle(ar.result());
              } else {
                throw new UnsupportedOperationException();
              }
            });
            msg.reply(addr);
          }
          break;
        case "close":
          break;
      }
    }
  }

  @Override
  public void register(String address) {
    bus.consumer(address, this);
  }
}
