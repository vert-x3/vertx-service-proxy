package io.vertx.serviceproxy.tests.codegen.future;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.Future;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@ProxyGen
public interface FuturizedProxy {

  Future<String> future0();

}
