package io.vertx.serviceproxy.codegen.proxytestapi;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.json.JsonObject;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
@ProxyGen
public interface InvalidReturn3 {

  JsonObject someMethod();
}
