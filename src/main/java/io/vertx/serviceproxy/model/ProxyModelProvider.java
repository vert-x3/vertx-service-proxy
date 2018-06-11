package io.vertx.serviceproxy.model;

import io.vertx.codegen.Model;
import io.vertx.codegen.ModelProvider;
import io.vertx.codegen.annotations.ProxyGen;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;

public class ProxyModelProvider implements ModelProvider {
  @Override
  public Model getModel(ProcessingEnvironment env, TypeElement elt) {
    if (elt.getAnnotation(ProxyGen.class) != null) {
      ProxyModel model = new ProxyModel(env, elt);
      return model;
    } else {
      return null;
    }
  }
}
