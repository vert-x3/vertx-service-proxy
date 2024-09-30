package io.vertx.serviceproxy.tests.testmodel;

import java.time.ZonedDateTime;

public class Mappers {

  public static String serializeZonedDateTime(ZonedDateTime value) throws IllegalArgumentException {
    return (value != null) ? value.toString() : null;
  }

  public static ZonedDateTime deserializeZonedDateTime(String value) throws IllegalArgumentException {
    return (value != null) ? ZonedDateTime.parse(value) : null;
  }

  public static String serializeSomeEnumWithCustomConstructor(SomeEnumWithCustomConstructor value) throws IllegalArgumentException {
    return (value != null) ? value.getLongName() : null;
  }

  public static SomeEnumWithCustomConstructor deserializeSomeEnumWithCustomConstructor(String value) throws IllegalArgumentException {
    return (value != null) ? SomeEnumWithCustomConstructor.of(value) : null;
  }
}
