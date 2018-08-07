package io.vertx.serviceproxy.codegen.proxytestapi;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
@VertxGen
@ProxyGen
public interface InvalidParamsDataObject {
  void invalidDataObject(Concrete concrete);
}
