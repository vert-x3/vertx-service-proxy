package io.vertx.serviceproxy.tests.grpcinterop;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;

@ProxyGen
@VertxGen
public interface GreeterProxy {

  @SuppressWarnings("checkstyle:MethodName")
  Future<HelloJavaReply> SayHello(String name);

  Future<HelloJavaReply> sayHi(String name);

}
