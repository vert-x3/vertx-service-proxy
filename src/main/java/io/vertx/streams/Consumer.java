package io.vertx.streams;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import io.vertx.streams.impl.ConsumerImpl;
import io.vertx.streams.impl.Transport;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@VertxGen
public interface Consumer<T> {

  static <T> Consumer<T> consumer(EventBus bus, String address) {
    return new ConsumerImpl<>(bus, address);
  }

  static <T> Consumer<T> consumer(EventBus bus, String address, Transport transport) {
    return new ConsumerImpl<T>(bus, address, transport);
  }

  void openReadStream(Object body, DeliveryOptions options, Handler<AsyncResult<ReadStream<T>>> doneHandler);

  void openReadStream(Handler<AsyncResult<ReadStream<T>>> doneHandler);

  void openWriteStream(Object body, DeliveryOptions options, Handler<AsyncResult<WriteStream<T>>> doneHandler);

  void openWriteStream(Handler<AsyncResult<WriteStream<T>>> doneHandler);
}
