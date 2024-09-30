package io.vertx.serviceproxy.tests.codegen.proxytestapi;

import io.vertx.codegen.annotations.ProxyClose;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.Future;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@ProxyGen
public interface ValidProxyCloseWithFuture {

  @ProxyClose
  Future<Void> closeIt();

}
