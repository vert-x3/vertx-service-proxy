package io.vertx.serviceproxy.testmodel;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.function.Function;

public class Mappers {

  public static String serializeZonedDateTime(ZonedDateTime value) throws IllegalArgumentException {
    return (value != null) ? value.toString() : null;
  }

  public static ZonedDateTime deserializeZonedDateTime(String value) throws IllegalArgumentException {
    return (value != null) ? ZonedDateTime.parse(value) : null;
  }

  public static final Function<URI, String> URI_SERIALIZER = value -> (value != null) ? value.toString() : null;

  public static final Function<String, URI> URI_DESERIALIZER = value -> {
    try {
      return (value != null) ? new URI(value) : null;
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }
  };
}
