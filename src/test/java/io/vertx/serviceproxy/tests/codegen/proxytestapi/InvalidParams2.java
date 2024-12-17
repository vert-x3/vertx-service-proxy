package io.vertx.serviceproxy.tests.codegen.proxytestapi;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.Handler;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@ProxyGen
public interface InvalidParams2 {

  void someMethod(Handler<String> handler);
}