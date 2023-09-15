package io.vertx.serviceproxy.generator;

import io.vertx.codegen.Generator;
import io.vertx.codegen.MethodInfo;
import io.vertx.codegen.ParamInfo;
import io.vertx.codegen.annotations.ModuleGen;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.serviceproxy.generator.model.ProxyMethodInfo;
import io.vertx.serviceproxy.generator.model.ProxyModel;

import java.lang.annotation.Annotation;
import java.util.*;

public class ServiceProxyProto extends Generator<ProxyModel> {

  public ServiceProxyProto() {
    kinds = Collections.singleton("proxy");
    name = "service_proxy";
  }

  @Override
  public Collection<Class<? extends Annotation>> annotations() {
    return Arrays.asList(ProxyGen.class, ModuleGen.class);
  }

  @Override
  public String filename(ProxyModel model) {
    return model.getIfaceFQCN() + "_messages.java";
  }

  @Override
  public String render(ProxyModel model, int index, int size, Map<String, Object> session) {

    String s = "package " + model.getType().getPackageName() + ";\n";

    s += "public class " + model.getType().getSimpleName() + "_messages {}\n";

    for (int j = 0;j < model.getMethods().size();j++) {
      MethodInfo method = model.getMethods().get(j);
      if (method instanceof ProxyMethodInfo) {
        ProxyMethodInfo pmi = (ProxyMethodInfo) method;
        if (pmi.isProxyIgnore() || pmi.isStaticMethod()) {
          continue;
        }
        s += "@io.vertx.codegen.annotations.DataObject\n" +
          "@io.vertx.codegen.protobuf.annotations.ProtobufGen\n" +
          "class " + model.getType().getSimpleName() + "_" + method.getName() + "{\n";
        List<ParamInfo> params = method.getParams();
        for (int i = 0;i < params.size();i++) {
          ParamInfo param = params.get(i);
          String name = param.getName();
          name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
          s += "private " + param.getType().getName() + " " + name + ";\n";
          s += "public void set" + name + "(" + param.getType().getName() + " value) { }\n";
          s += "public " + param.getType().getName() + " get" + name + "() { throw new UnsupportedOperationException(); }\n";
        }
        s += "}\n";
      }
    }
    return s;
  }
}
