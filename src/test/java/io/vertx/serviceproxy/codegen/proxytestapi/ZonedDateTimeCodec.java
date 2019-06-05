package io.vertx.serviceproxy.codegen.proxytestapi;

import io.vertx.core.spi.json.JsonCodec;

import java.time.ZonedDateTime;

public class ZonedDateTimeCodec implements JsonCodec<ZonedDateTime, String> {

  public static final ZonedDateTimeCodec INSTANCE = new ZonedDateTimeCodec();

  @Override
  public String encode(ZonedDateTime value) {
    return (value != null) ? value.toString() : null;
  }

  @Override
  public Class<ZonedDateTime> getTargetClass() {
    return ZonedDateTime.class;
  }

  @Override
  public ZonedDateTime decode(String value) {
    return (value != null) ? ZonedDateTime.parse(value) : null;
  }
}
