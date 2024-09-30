package io.vertx.serviceproxy.tests.codegen.proxytestapi;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@ProxyGen
public interface InvalidParams4 {

  void someMethod(String str, Handler<AsyncResult<String>> wrongResultType);
}
