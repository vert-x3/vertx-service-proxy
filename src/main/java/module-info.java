module io.vertx.serviceproxy {

  requires static io.vertx.auth.jwt;
  requires static io.vertx.docgen;

  requires io.vertx.codegen.api;
  requires io.vertx.codegen.processor;
  requires io.vertx.core;
  requires java.compiler;

  exports io.vertx.serviceproxy;
  exports io.vertx.serviceproxy.generator;
  exports io.vertx.serviceproxy.generator.model;
  exports io.vertx.serviceproxy.impl to io.vertx.serviceproxy.tests;
  exports io.vertx.serviceproxy.impl.utils to io.vertx.serviceproxy.tests;

}
