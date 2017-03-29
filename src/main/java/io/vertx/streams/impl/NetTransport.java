package io.vertx.streams.impl;

import io.netty.util.CharsetUtil;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.eventbus.impl.CodecManager;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class NetTransport implements Transport, Handler<NetSocket> {

  private final CodecManager codecManager = new CodecManager();
  private ConcurrentMap<String, Handler<ReadStream<Object>>> handlerMap = new ConcurrentHashMap<>();

  private final NetClient client;
  private final int port;
  private final String host;

  public NetTransport(Vertx vertx, int port, String host) {
    this.client = vertx.createNetClient();
    this.port = port;
    this.host = host;
  }

  @Override
  public void handle(NetSocket netSocket) {
    Buffer received = Buffer.buffer();
    AtomicReference<SocketReadStream> ref = new AtomicReference<>();
    netSocket.closeHandler(v -> {
      SocketReadStream rs = ref.get();
      if (rs == null) {
        // Handle me
      } else {
        if (rs.endHandler != null) {
          rs.endHandler.handle(null);
        }
      }
    });
    netSocket.handler(buff -> {
      SocketReadStream rs = ref.get();
      if (rs == null) {
        received.appendBuffer(buff);
        if (received.length() >= 4) {
          int len = received.getInt(0);
          int to = 4 + len;
          if (received.length() >= to) {
            String address = received.slice(4, to).toString(StandardCharsets.UTF_8);
            Handler<ReadStream<Object>> handler = handlerMap.get(address);
            rs = new SocketReadStream(netSocket);
            ref.set(rs);
            handler.handle(rs);
            rs.handleChunk(received.slice(to, received.length()));
          }
        }
      } else {
        rs.handleChunk(buff);
      }
    });
  }

  public class SocketReadStream<T> implements ReadStream<T> {

    private Handler<T> handler;
    private Handler<Void> endHandler;
    private Buffer pending = Buffer.buffer();
    private final NetSocket netSocket;

    public SocketReadStream(NetSocket netSocket) {
      this.netSocket = netSocket;
    }

    public NetSocket netSocket() {
      return netSocket;
    }

    void handleChunk(Buffer chunk) {
      pending.appendBuffer(chunk);
      while (true) {
        if (pending.length() >= 4) {
          int len = pending.getInt(0);
          if (pending.length() >= len + 4) {
            Buffer message = pending.slice(4, 4 + len);
            handleMessage(message);
            pending = pending.slice(4 + len, pending.length());
          } else {
            break;
          }
        } else {
          break;
        }
      }
    }

    void handleMessage(Buffer buffer) {
      int len = buffer.getInt(0);
      int pos = 4 + len;
      String codecName = buffer.getString(4, pos, "UTF-8");
      MessageCodec codec;
      if (codecName.equals("string")) {
        codec = CodecManager.STRING_MESSAGE_CODEC;
      } else {
        codec = codecManager.getCodec(codecName);
      }
      Object obj = codec.transform(codec.decodeFromWire(pos, buffer));
      if (handler != null) {
        handler.handle((T) obj);
      }
    }

    @Override
    public ReadStream<T> exceptionHandler(Handler<Throwable> handler) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ReadStream<T> handler(Handler<T> handler) {
      this.handler = handler;
      return this;
    }

    @Override
    public ReadStream<T> pause() {
      netSocket.pause();
      return this;
    }

    @Override
    public ReadStream<T> resume() {
      netSocket.resume();
      return this;
    }

    @Override
    public ReadStream<T> endHandler(Handler<Void> handler) {
      endHandler = handler;
      return this;
    }
  }

  @Override
  public <T> String bind(Handler<AsyncResult<ReadStream<T>>> completionHandler) {
    String localAddress = UUID.randomUUID().toString();
    handlerMap.put(localAddress, stream -> {
      completionHandler.handle(Future.succeededFuture((ReadStream<T>) stream));
    });
    return localAddress;
  }

  @Override
  public <T> void connect(String address, Handler<AsyncResult<WriteStream<T>>> completionHandler) {
    client.connect(port, host, ar -> {
      if (ar.succeeded()) {
        NetSocket socket = ar.result();
        byte[] bytes = address.getBytes(StandardCharsets.UTF_8);
        socket.write(Buffer.buffer().appendInt(address.length()).appendBytes(bytes));
        completionHandler.handle(Future.succeededFuture(new NetStreamImpl<>(socket)));
      } else {
        completionHandler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }

  private class NetStreamImpl<T> implements WriteStream<T> {

    private final NetSocket socket;

    public NetStreamImpl(NetSocket socket) {
      this.socket = socket;
    }

    @Override
    public WriteStream<T> exceptionHandler(Handler<Throwable> handler) {
      throw new UnsupportedOperationException();
    }

    @Override
    public WriteStream<T> write(T t) {
      MessageCodec codec = codecManager.lookupCodec(t, null);
      Buffer buff = Buffer.buffer();
      buff.appendInt(0);
      byte[] strBytes = codec.name().getBytes(CharsetUtil.UTF_8);
      buff.appendInt(strBytes.length);
      buff.appendBytes(strBytes);
      codec.encodeToWire(buff, t);
      buff.setInt(0, buff.length() - 4);
      socket.write(buff);
      return this;
    }

    @Override
    public void end() {
      socket.close();
    }

    @Override
    public WriteStream<T> setWriteQueueMaxSize(int i) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean writeQueueFull() {
      throw new UnsupportedOperationException();
    }

    @Override
    public WriteStream<T> drainHandler(Handler<Void> handler) {
      throw new UnsupportedOperationException();
    }
  }
}
