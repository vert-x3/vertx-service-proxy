package io.vertx.streams.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class StreamAdapterImpl<T> implements ReadStream<T> {

  private final Future<WriteStream<T>> fut = Future.future();
  private Handler<T> itemHandler;
  private Handler<Void> endHandler;

  public StreamAdapterImpl(Handler<AsyncResult<WriteStream<T>>> completionHandler) {
    fut.setHandler(completionHandler);
  }

  @Override
  public ReadStream<T> exceptionHandler(Handler<Throwable> handler) {
    return this;
  }

  @Override
  public ReadStream<T> handler(Handler<T> handler) {
    this.itemHandler = handler;
    if (handler != null) {
      fut.tryComplete(new WriteStream<T>() {
        @Override
        public WriteStream<T> exceptionHandler(Handler<Throwable> handler) {
          return this;
        }
        @Override
        public WriteStream<T> write(T t) {
          Handler<T> handler = itemHandler;
          if (handler != null) {
            handler.handle(t);
          }
          return this;
        }
        @Override
        public void end() {
          Handler<Void> handler = endHandler;
          if (handler != null) {
            handler.handle(null);
          }
        }
        @Override
        public WriteStream<T> setWriteQueueMaxSize(int i) {
          return this;
        }
        @Override
        public boolean writeQueueFull() {
          return false;
        }
        @Override
        public WriteStream<T> drainHandler(Handler<Void> handler) {
          return this;
        }
      });
    }
    return this;
  }

  @Override
  public ReadStream<T> pause() {
    return this;
  }

  @Override
  public ReadStream<T> resume() {
    return this;
  }

  @Override
  public ReadStream<T> endHandler(Handler<Void> handler) {
    endHandler = handler;
    return this;
  }
}
