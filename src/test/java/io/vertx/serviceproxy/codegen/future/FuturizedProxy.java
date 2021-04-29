package io.vertx.serviceproxy.codegen.future;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.Future;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@ProxyGen
public interface FuturizedProxy {

  Future<String> future0();

/*
  void handler1(Handler<AsyncResult<Byte>> byteHandler);
  void handler2(Handler<AsyncResult<Short>> shortHandler);
  void handler3(Handler<AsyncResult<Integer>> intHandler);
  void handler4(Handler<AsyncResult<Long>> longHandler);
  void handler5(Handler<AsyncResult<Float>> floatHandler);
  void handler6(Handler<AsyncResult<Double>> doubleHandler);
  void handler7(Handler<AsyncResult<Character>> charHandler);
  void handler8(Handler<AsyncResult<Boolean>> boolHandler);
  void handler9(Handler<AsyncResult<JsonObject>> jsonObjectHandler);
  void handler10(Handler<AsyncResult<JsonArray>> jsonArrayHandler);
  void handler11(Handler<AsyncResult<ProxyDataObject>> dataObjectHandler);

  void handler12(Handler<AsyncResult<List<String>>> stringListHandler);
  void handler13(Handler<AsyncResult<List<Byte>>> byteListHandler);
  void handler14(Handler<AsyncResult<List<Short>>> shortListHandler);
  void handler15(Handler<AsyncResult<List<Integer>>> intListHandler);
  void handler16(Handler<AsyncResult<List<Long>>> longListHandler);
  void handler17(Handler<AsyncResult<List<Float>>> floatListHandler);
  void handler18(Handler<AsyncResult<List<Double>>> doubleListHandler);
  void handler19(Handler<AsyncResult<List<Character>>> charListHandler);
  void handler20(Handler<AsyncResult<List<Boolean>>> boolListHandler);
  void handler21(Handler<AsyncResult<List<JsonObject>>> jsonObjectListHandler);
  void handler22(Handler<AsyncResult<List<JsonArray>>> jsonArrayListHandler);

  void handler24(Handler<AsyncResult<Set<String>>> stringSetHandler);
  void handler25(Handler<AsyncResult<Set<Byte>>> byteSetHandler);
  void handler26(Handler<AsyncResult<Set<Short>>> shortSetHandler);
  void handler27(Handler<AsyncResult<Set<Integer>>> intSetHandler);
  void handler28(Handler<AsyncResult<Set<Long>>> longSetHandler);
  void handler29(Handler<AsyncResult<Set<Float>>> floatSetHandler);
  void handler30(Handler<AsyncResult<Set<Double>>> doubleSetHandler);
  void handler31(Handler<AsyncResult<Set<Character>>> charSetHandler);
  void handler32(Handler<AsyncResult<Set<Boolean>>> boolSetHandler);
  void handler33(Handler<AsyncResult<Set<JsonObject>>> jsonObjectSetHandler);
  void handler34(Handler<AsyncResult<Set<JsonArray>>> jsonArraySetHandler);

  void handler35(Handler<AsyncResult<ZonedDateTime>> zonedDateTime);
  void handler36(Handler<AsyncResult<List<ZonedDateTime>>> zonedDateTimeListHandler);
  void handler37(Handler<AsyncResult<Set<ZonedDateTime>>> zonedDateTimeSetHandler);

  @ProxyIgnore
  void ignored();

  @ProxyClose
  void closeIt();

  void connection(String foo, Handler<AsyncResult<ProxyConnection>> resultHandler);
*/

}
