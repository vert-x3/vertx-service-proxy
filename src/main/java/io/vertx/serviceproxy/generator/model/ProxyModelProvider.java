package io.vertx.serviceproxy.generator.model;

import io.vertx.codegen.processor.Model;
import io.vertx.codegen.processor.ModelProvider;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.processor.type.TypeMirrorFactory;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;

public class ProxyModelProvider implements ModelProvider {
  @Override
  public Model getModel(ProcessingEnvironment env, TypeMirrorFactory typeFactory, TypeElement elt) {
    if (elt.getAnnotation(ProxyGen.class) != null) {
      ProxyModel model = new ProxyModel(env, typeFactory, elt);
      return model;
    } else {
      return null;
    }
  }
}
