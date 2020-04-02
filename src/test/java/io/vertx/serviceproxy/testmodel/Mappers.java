package io.vertx.serviceproxy.testmodel;

import java.time.ZonedDateTime;

public class Mappers {

  public static String serializeZonedDateTime(ZonedDateTime value) throws IllegalArgumentException {
    return (value != null) ? value.toString() : null;
  }

  public static ZonedDateTime deserializeZonedDateTime(String value) throws IllegalArgumentException {
    return (value != null) ? ZonedDateTime.parse(value) : null;
  }
}
