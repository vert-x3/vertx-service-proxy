package io.vertx.serviceproxy.codegen.proxytestapi;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.vertx.codegen.annotations.ProxyClose;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.ProxyIgnore;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.testmodel.SomeEnumWithCustomConstructor;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@ProxyGen
public interface ValidProxy {

  void basicTypes(String str, byte b, short s, int i, long l, float f, double d, char c, boolean bool);

  void basicBoxedTypes(String str, Byte b, Short s, Integer i, Long l, Float f, Double d, Character c,
                       Boolean bool);

  void jsonTypes(JsonObject jsonObject, JsonArray jsonArray);

  void methodWithListParams(List<String> listString, List<Byte> listByte, List<Short> listShort, List<Integer> listInt, List<Long> listLong, List<JsonObject> listJsonObject, List<JsonArray> listJsonArray);

  void methodWithSetParams(Set<String> setString, Set<Byte> setByte, Set<Short> setShort, Set<Integer> setInt, Set<Long> setLong, Set<JsonObject> setJsonObject, Set<JsonArray> setJsonArray);

  void methodWithMapParams(Map<String, String> mapString, Map<String, Byte> mapByte, Map<String, Short> mapShort, Map<String, Integer> mapInt, Map<String, Long> mapLong, Map<String, JsonObject> mapJsonObject, Map<String, JsonArray> mapJsonArray);

  void enumType(SomeEnum someEnum);

  void enumWithCustomConstructorType(SomeEnumWithCustomConstructor someCustomEnum);

  void dataObjectType(ProxyDataObject dataObject);
  void methodMapper(ZonedDateTime dateTime);

  void dataObjectWithParentType(ProxyDataObjectWithParent dataObject);
  void dataObjectWithParentAndOverride(ProxyDataObjectWithParentOverride dataObject);

  Future<String> handler0();
  Future<Byte> handler1();
  Future<Short> handler2();
  Future<Integer> handler3();
  Future<Long> handler4();
  Future<Float> handler5();
  Future<Double> handler6();
  Future<Character> handler7();
  Future<Boolean> handler8();
  Future<JsonObject> handler9();
  Future<JsonArray> handler10();
  Future<ProxyDataObject> handler11();

  Future<List<String>> handler12();
  Future<List<Byte>> handler13();
  Future<List<Short>> handler14();
  Future<List<Integer>> handler15();
  Future<List<Long>> handler16();
  Future<List<Float>> handler17();
  Future<List<Double>> handler18();
  Future<List<Character>> handler19();
  Future<List<Boolean>> handler20();
  Future<List<JsonObject>> handler21();
  Future<List<JsonArray>> handler22();

  Future<Set<String>> handler24();
  Future<Set<Byte>> handler25();
  Future<Set<Short>> handler26();
  Future<Set<Integer>> handler27();
  Future<Set<Long>> handler28();
  Future<Set<Float>> handler29();
  Future<Set<Double>> handler30();
  Future<Set<Character>> handler31();
  Future<Set<Boolean>> handler32();
  Future<Set<JsonObject>> handler33();
  Future<Set<JsonArray>> handler34();

  Future<ZonedDateTime> handler35();
  Future<List<ZonedDateTime>> handler36();
  Future<Set<ZonedDateTime>> handler37();

  @ProxyIgnore
  void ignored();

  @ProxyClose
  void closeIt();

  Future<ProxyConnection> connection(String foo);

}
