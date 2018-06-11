package io.vertx.serviceproxy.codegen.proxytestapi;

import io.vertx.codegen.annotations.ProxyGen;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
@ProxyGen
public interface InvalidParamsDataObject {
  void invalidDataObject(Concrete concrete);
}
