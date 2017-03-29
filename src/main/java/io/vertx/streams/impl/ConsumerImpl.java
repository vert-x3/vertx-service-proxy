package io.vertx.streams.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import io.vertx.streams.Consumer;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class ConsumerImpl<T> implements Consumer<T> {

  final EventBus bus;
  final String address;
  final Transport transport;

  public ConsumerImpl(EventBus bus, String address) {
    this.bus = bus;
    this.transport = new EventBusTransport(bus);
    this.address = address;
  }

  public ConsumerImpl(EventBus bus, String address, Transport transport) {
    this.bus = bus;
    this.transport = transport;
    this.address = address;
  }

  @Override
  public void openReadStream(Object body, DeliveryOptions options, Handler<AsyncResult<ReadStream<T>>> doneHandler) {
    ConsumerStream<T> stream = new ConsumerStream<>(this, doneHandler, null);
    stream.subscribe(body, options);
  }

  @Override
  public void openReadStream(Handler<AsyncResult<ReadStream<T>>> doneHandler) {
    ConsumerStream<T> stream = new ConsumerStream<>(this, doneHandler, null);
    stream.subscribe();
  }

  @Override
  public void openWriteStream(Object body, DeliveryOptions options, Handler<AsyncResult<WriteStream<T>>> doneHandler) {
    ConsumerStream<T> stream = new ConsumerStream<>(this, null, doneHandler);
    stream.subscribe(body, options);
  }

  @Override
  public void openWriteStream(Handler<AsyncResult<WriteStream<T>>> doneHandler) {
    ConsumerStream<T> stream = new ConsumerStream<>(this, null, doneHandler);
    stream.subscribe();
  }
}
