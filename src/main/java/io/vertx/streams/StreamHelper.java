package io.vertx.streams;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.streams.Pump;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import io.vertx.streams.impl.StreamAdapterImpl;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@VertxGen
public interface StreamHelper {

  static <T> ReadStream<T> adapter(Handler<AsyncResult<WriteStream<T>>> handler) {
    return new StreamAdapterImpl<T>(handler);
  }

  static void pipe(ReadStream rs, WriteStream ws) {
    Pump pump = Pump.pump(rs, ws);
    rs.endHandler(v -> {
      pump.stop();
      ws.end();
    });
    pump.start();
  }

}
