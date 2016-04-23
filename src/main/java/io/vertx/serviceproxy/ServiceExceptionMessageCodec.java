package io.vertx.serviceproxy;

import io.netty.util.CharsetUtil;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

/**
 * A MessageCodec for ServiceException
 *
 * @author <a href="mailto:oreilldf@gmail.com">Dan O'Reilly</a>
 */
public class ServiceExceptionMessageCodec implements MessageCodec<ServiceException, ServiceException> {

  @Override
  public void encodeToWire(Buffer buffer, ServiceException body) {
    buffer.appendInt(body.failureCode());
    if (body.getMessage() == null) {
      buffer.appendByte((byte)0);
    } else {
      buffer.appendByte((byte)1);
      byte[] encoded = body.getMessage().getBytes(CharsetUtil.UTF_8);
      buffer.appendInt(encoded.length);
      buffer.appendBytes(encoded);
    }
  }

  @Override
  public ServiceException decodeFromWire(int pos, Buffer buffer) {
    int failureCode = buffer.getInt(pos);
    pos += 4;
    boolean isNull = buffer.getByte(pos) == (byte)0;
    String message;
    if (!isNull) {
      pos++;
      int strLength = buffer.getInt(pos);
      pos += 4;
      byte[] bytes = buffer.getBytes(pos, pos + strLength);
      message = new String(bytes, CharsetUtil.UTF_8);
    } else {
      message = null;
    }
    return new ServiceException(failureCode, message);
  }

  @Override
  public ServiceException transform(ServiceException exception) {
    return exception;
  }

  @Override
  public String name() {
    return "ServiceException";
  }

  @Override
  public byte systemCodecID() {
    return -1;
  }
}

