package io.vertx.serviceproxy.testmodel;

import io.vertx.codegen.annotations.Mapper;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.function.Function;

public class Mappers {

  @Mapper
  public static String serialize(ZonedDateTime value) throws IllegalArgumentException {
    return (value != null) ? value.toString() : null;
  }

  @Mapper
  public static ZonedDateTime deserialize(String value) throws IllegalArgumentException {
    return (value != null) ? ZonedDateTime.parse(value) : null;
  }

  @Mapper
  public static final Function<URI, String> SERIALIZER = value -> (value != null) ? value.toString() : null;

  @Mapper
  public static final Function<String, URI> DESERIALIZER = value -> {
    try {
      return (value != null) ? new URI(value) : null;
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }
  };
}
