package io.vertx.serviceproxy.codegen.proxytestapi;

import io.vertx.core.spi.json.JsonMapper;

import java.time.ZonedDateTime;

public class ZonedDateTimeMapper implements JsonMapper<ZonedDateTime, String> {

  public static final ZonedDateTimeMapper INSTANCE = new ZonedDateTimeMapper();

  @Override
  public String serialize(ZonedDateTime value) throws IllegalArgumentException {
    return (value != null) ? value.toString() : null;
  }

  @Override
  public Class<ZonedDateTime> getTargetClass() {
    return ZonedDateTime.class;
  }

  @Override
  public ZonedDateTime deserialize(String value) throws IllegalArgumentException {
    return (value != null) ? ZonedDateTime.parse(value) : null;
  }
}
