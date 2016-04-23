package io.vertx.serviceproxy.testmodel;

import io.netty.util.CharsetUtil;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

/**
 * @author <a href="mailto:oreilldf@gmail.com">Dan O'Reilly</a>
 */
public class MyServiceExceptionMessageCodec  implements
    MessageCodec<MyServiceException, MyServiceException> {
  @Override
  public void encodeToWire(Buffer buffer, MyServiceException body) {
    buffer.appendInt(body.failureCode());
    if (body.getMessage() == null) {
      buffer.appendByte((byte)0);
    } else {
      buffer.appendByte((byte)1);
      byte[] encoded = body.getMessage().getBytes(CharsetUtil.UTF_8);
      buffer.appendInt(encoded.length);
      buffer.appendBytes(encoded);
    }
    if (body.getExtra() == null) {
      buffer.appendByte((byte)0);
    } else {
      buffer.appendByte((byte)1);
      byte[] encoded = body.getExtra().getBytes(CharsetUtil.UTF_8);
      buffer.appendInt(encoded.length);
      buffer.appendBytes(encoded);
    }
  }

  @Override
  public MyServiceException decodeFromWire(int pos, Buffer buffer) {
    int failureCode = buffer.getInt(pos);
    pos += 4;
    boolean isNull = buffer.getByte(pos) == (byte)0;
    pos++;
    String message;
    if (!isNull) {
      int strLength = buffer.getInt(pos);
      pos += 4;
      byte[] bytes = buffer.getBytes(pos, pos + strLength);
      pos += strLength;
      message = new String(bytes, CharsetUtil.UTF_8);
    } else {
      message = null;
    }
    isNull = buffer.getByte(pos) == (byte)0;
    String extra;
    if (!isNull) {
      pos++;
      int strLength = buffer.getInt(pos);
      pos += 4;
      byte[] bytes = buffer.getBytes(pos, pos + strLength);
      extra = new String(bytes, CharsetUtil.UTF_8);
    } else {
      extra = null;
    }
    return new MyServiceException(failureCode, message, extra);
  }

  @Override
  public MyServiceException transform(MyServiceException e) {
    return e;
  }

  @Override
  public String name() {
    return "myServiceException";
  }

  @Override
  public byte systemCodecID() {
    return -1;
  }
}
