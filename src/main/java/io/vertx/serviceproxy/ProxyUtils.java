/*
 * Copyright 2021 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */
package io.vertx.serviceproxy;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author <a href="http://slinkydeveloper.github.io">Francesco Guardiani @slinkydeveloper</a>
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class ProxyUtils {

  public static List<Character> convertToListChar(JsonArray arr) {
    return arr.stream().map(ProxyUtils::javaObjToChar).collect(Collectors.toList());
  }

  public static Set<Character> convertToSetChar(JsonArray arr) {
    return arr.stream().map(ProxyUtils::javaObjToChar).collect(Collectors.toSet());
  }

  public static Map<String, Character> convertToMapChar(JsonObject obj) {
    return obj.stream().collect(Collectors.toMap(Map.Entry::getKey, e -> javaObjToChar(e.getValue())));
  }

  public static Character javaObjToChar(Object obj) {
    Integer jobj = (Integer)obj;
    return (char)(int)jobj;
  }

  public static <T> Map<String, T> convertMap(Map map) {
    if (map.isEmpty()) {
      return (Map<String, T>) map;
    }

    Object elem = map.values().stream().findFirst().get();
    if (!(elem instanceof Map) && !(elem instanceof List)) {
      return (Map<String, T>) map;
    } else {
      Function<Object, T> converter;
      if (elem instanceof List) {
        converter = object -> (T) new JsonArray((List) object);
      } else {
        converter = object -> (T) new JsonObject((Map) object);
      }
      return ((Map<String, T>) map).entrySet()
        .stream()
        .collect(Collectors.toMap(Map.Entry::getKey, converter::apply));
    }
  }

  public static <T> List<T> convertList(List list) {
    if (list.isEmpty()) {
      return (List<T>) list;
    }

    Object elem = list.get(0);
    if (!(elem instanceof Map) && !(elem instanceof List)) {
      return (List<T>) list;
    } else {
      Function<Object, T> converter;
      if (elem instanceof List) {
        converter = object -> (T) new JsonArray((List) object);
      } else {
        converter = object -> (T) new JsonObject((Map) object);
      }
      return (List<T>) list.stream().map(converter).collect(Collectors.toList());
    }
  }

  public static <T> Set<T> convertSet(List list) {
    return new HashSet<T>(convertList(list));
  }

}
