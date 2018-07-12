package io.vertx.serviceproxy;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author <a href="http://slinkydeveloper.github.io">Francesco Guardiani @slinkydeveloper</a>
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class ProxyUtils {

  public static List<Character> convertToListChar(JsonArray arr) {
    List<Character> list = new ArrayList<>();
    for (Object obj: arr) {
      Integer jobj = (Integer)obj;
      list.add((char)(int)jobj);
    }
    return list;
  }

  public static Set<Character> convertToSetChar(JsonArray arr) {
    Set<Character> set = new HashSet<>();
    for (Object obj: arr) {
      Integer jobj = (Integer)obj;
      set.add((char)(int)jobj);
    }
    return set;
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
