package io.vertx.serviceproxy;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.*;

/**
 * @author <a href="http://slinkydeveloper.github.io">Francesco Guardiani @slinkydeveloper</a>
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class HelperUtils {

  public static <T> Handler<AsyncResult<T>> createHandler(Message msg, boolean includeDebugInfo) {
    return res -> {
      if (res.failed()) {
        manageFailure(msg, res.cause(), includeDebugInfo);
      } else {
        if (res.result() != null  && res.result().getClass().isEnum()) {
          msg.reply(((Enum) res.result()).name());
        } else {
          msg.reply(res.result());
        }
      }
    };
  }

  public static <T> Handler<AsyncResult<List<T>>> createListHandler(Message msg, boolean includeDebugInfo) {
    return res -> {
      if (res.failed()) {
        manageFailure(msg, res.cause(), includeDebugInfo);
      } else {
        msg.reply(new JsonArray(res.result()));
      }
    };
  }

  public static <T> Handler<AsyncResult<Set<T>>> createSetHandler(Message msg, boolean includeDebugInfo) {
    return res -> {
      if (res.failed()) {
        manageFailure(msg, res.cause(), includeDebugInfo);
      } else {
        msg.reply(new JsonArray(new ArrayList<>(res.result())));
      }
    };
  }

  public static <T> Handler<AsyncResult<Map<String, T>>> createMapHandler(Message msg, boolean includeDebugInfo) {
    return res -> {
      if (res.failed()) {
        manageFailure(msg, res.cause(), includeDebugInfo);
      } else {
        msg.reply(new JsonObject(new HashMap<>(res.result())));
      }
    };
  }

  public static Handler<AsyncResult<List<Character>>> createListCharHandler(Message msg, boolean includeDebugInfo) {
    return res -> {
      if (res.failed()) {
        manageFailure(msg, res.cause(), includeDebugInfo);
      } else {
        JsonArray arr = new JsonArray();
        for (Character chr: res.result()) {
          arr.add((int) chr);
        }
        msg.reply(arr);
      }
    };
  }

  public static Handler<AsyncResult<Set<Character>>> createSetCharHandler(Message msg, boolean includeDebugInfo) {
    return res -> {
      if (res.failed()) {
        manageFailure(msg, res.cause(), includeDebugInfo);
      } else {
        JsonArray arr = new JsonArray();
        for (Character chr: res.result()) {
          arr.add((int) chr);
        }
        msg.reply(arr);
      }
    };
  }

  public static Handler<AsyncResult<Map<String, Character>>> createMapCharHandler(Message msg, boolean includeDebugInfo) {
    return res -> {
      if (res.failed()) {
        manageFailure(msg, res.cause(), includeDebugInfo);
      } else {
        JsonObject obj = new JsonObject();
        for (Map.Entry<String, Character> chr: res.result().entrySet()) {
          obj.put(chr.getKey(), (int) chr.getValue());
        }
        msg.reply(obj);
      }
    };
  }

  public static void manageFailure(Message msg, Throwable cause, boolean includeDebugInfo) {
    if (cause instanceof ServiceException) {
      msg.reply(cause);
    } else {
      if (includeDebugInfo)
        msg.reply(new ServiceException(-1, cause.getMessage(), generateDebugInfo(cause)));
      else
        msg.reply(new ServiceException(-1, cause.getMessage()));
    }
  }

  public static <T> Map<String, T> convertMap(Map map) {
    return (Map<String, T>)map;
  }

  public static <T> List<T> convertList(List list) {
    return (List<T>)list;
  }

  public static <T> Set<T> convertSet(List list) {
    return new HashSet<T>((List<T>)list);
  }

  public static JsonObject generateDebugInfo(Throwable cause) {
    if (cause == null) return null;
    JsonObject obj = new JsonObject();
    obj.put("causeName", cause.getClass().getCanonicalName());
    obj.put("causeMessage", cause.getMessage());
    obj.put("causeStackTrace", convertStackTrace(cause));
    return obj;
  }

  public static JsonArray convertStackTrace(Throwable cause) {
    if (cause == null || cause.getStackTrace() == null) return new JsonArray();
    return Arrays
      .stream(cause.getStackTrace())
      .map(StackTraceElement::toString)
      .collect(JsonArray::new, JsonArray::add, JsonArray::addAll);
  }

}
