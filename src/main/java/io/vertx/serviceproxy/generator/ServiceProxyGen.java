package io.vertx.serviceproxy.generator;

import io.vertx.codegen.Generator;
import io.vertx.codegen.MethodInfo;
import io.vertx.codegen.MethodKind;
import io.vertx.codegen.ParamInfo;
import io.vertx.codegen.TypeParamInfo;
import io.vertx.codegen.annotations.ModuleGen;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.type.*;
import io.vertx.codegen.writer.CodeWriter;
import io.vertx.serviceproxy.generator.model.ProxyMethodInfo;
import io.vertx.serviceproxy.generator.model.ProxyModel;

import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.*;

/**
 * @author <a href="http://slinkydeveloper.github.io">Francesco Guardiani @slinkydeveloper</a>
 */
public class ServiceProxyGen extends Generator<ProxyModel> {

  final GeneratorUtils utils;

  public ServiceProxyGen(GeneratorUtils utils) {
    kinds = Collections.singleton("proxy");
    name = "service_proxy";
    this.utils = utils;
  }

  @Override
  public Collection<Class<? extends Annotation>> annotations() {
    return Arrays.asList(ProxyGen.class, ModuleGen.class);
  }

  @Override
  public String filename(ProxyModel model) {
    return model.getIfaceFQCN() + "VertxEBProxy.java";
  }

  @Override
  public String render(ProxyModel model, int index, int size, Map<String, Object> session) {
    StringWriter buffer = new StringWriter();
    CodeWriter writer = new CodeWriter(buffer);

    String className = model.getIfaceSimpleName() + "VertxEBProxy";

    utils.classHeader(writer);
    writer.code("package " + model.getIfacePackageName() + ";\n");
    writer.code("\n");
    utils.proxyGenImports(writer);
    utils.additionalImports(model).forEach(i -> utils.writeImport(writer, i));
    utils.roger(writer);
    writer
      .code("@SuppressWarnings({\"unchecked\", \"rawtypes\"})\n")
      .code("public class " + className + " implements " + model.getIfaceSimpleName() + " {\n")
      .indent()
        .stmt("private Vertx _vertx")
        .stmt("private String _address")
        .stmt("private DeliveryOptions _options")
        .stmt("private boolean closed")
        .newLine()
        .code("public " + className + "(Vertx vertx, String address) {\n")
        .indent()
          .stmt("this(vertx, address, null)")
        .unindent()
        .code("}\n")
        .newLine()
        .code("public " + className +  "(Vertx vertx, String address, DeliveryOptions options) {\n")
        .indent()
          .stmt("this._vertx = vertx")
          .stmt("this._address = address")
          .stmt("this._options = options")
          .code("try {")
          .indent()
            .stmt("this._vertx.eventBus().registerDefaultCodec(ServiceException.class, new ServiceExceptionMessageCodec())")
          .unindent()
          .code("} catch (IllegalStateException ex) {\n}")
        .unindent()
        .code("}\n")
        .newLine();
    generateMethods(model, writer);
    writer
      .unindent()
      .code("}\n");
    return buffer.toString();
  }

  private void generateMethods(ProxyModel model, CodeWriter writer) {
    for (MethodInfo m : model.getMethods()) {
      if (!m.isStaticMethod()) {
        writer.code("@Override\n");
        writer.code("public");
        if (!m.getTypeParams().isEmpty()) {
          writer.write(" <");
          writer.writeSeq(m.getTypeParams().stream().map(TypeParamInfo::getName), ", ");
          writer.write(">");
        }
        writer.write(" " + m.getReturnType().getSimpleName() + " " + m.getName() + "(");
        writer.writeSeq(m.getParams().stream().map(p -> p.getType().getSimpleName() + " " + p.getName()), ", ");
        writer.write("){\n");
        writer.indent();
        if (!((ProxyMethodInfo) m).isProxyIgnore()) generateMethodBody((ProxyMethodInfo) m, writer);
        if (m.isFluent()) writer.stmt("return this");
        writer.unindent();
        writer.code("}\n");
      }
    }
  }

  private void generateMethodBody(ProxyMethodInfo method, CodeWriter writer) {
    ParamInfo lastParam = !method.getParams().isEmpty() ? method.getParam(method.getParams().size() - 1) : null;
    if (method.getKind() == MethodKind.FUTURE) {
      writer.code("if (closed) return io.vertx.core.Future.failedFuture(\"Proxy is closed\");\n");
    } else {
      writer.code("if (closed) throw new IllegalStateException(\"Proxy is closed\");\n");
    }
    if (method.isProxyClose())
      writer.stmt("closed = true");
    writer.stmt("JsonObject _json = new JsonObject()");
    List<ParamInfo> paramsExcludedHandler =
      (method.getParams().isEmpty()) ? new ArrayList<>() : method.getParams();
    paramsExcludedHandler.forEach(p -> generateAddToJsonStmt(p, writer));
    writer.newLine();
    writer.stmt("DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions()");
    writer.stmt("_deliveryOptions.addHeader(\"action\", \"" + method.getName() + "\")");
    writer.stmt("_deliveryOptions.getHeaders().set(\"action\", \"" + method.getName() + "\")");
    if (method.getKind() == MethodKind.FUTURE) {
      TypeInfo t = ((ParameterizedTypeInfo)method.getReturnType()).getArg(0);
      generateSendCallWithResultHandler(t, null, writer, true);
    } else {
      writer.stmt("_vertx.eventBus().send(_address, _json, _deliveryOptions)");
    }
  }

  private void generateAddToJsonStmt(ParamInfo param, CodeWriter writer) {
    TypeInfo t = param.getType();
    String name = param.getName();
    if ("char".equals(t.getName()))
      writer.stmt("_json.put(\"" + name + "\", (int)" + name + ")");
    else if ("java.lang.Character".equals(t.getName()))
      writer.stmt("_json.put(\"" + name + "\", " + name + " == null ? null : (int)" + name + ")");
    else if (t.getKind() == ClassKind.ENUM) {
      if (t.isDataObjectHolder()) {
        writer.stmt(String.format(
            "_json.put(\"%s\", %s)",
            name,
            GeneratorUtils.generateSerializeDataObject(name, (ClassTypeInfo) t)
          ));
      } else {
          writer.stmt("_json.put(\"" + name + "\", " + name + " == null ? null : " + name + ".name())");
        }
    }
    else if (t.getKind() == ClassKind.LIST) {
      if (((ParameterizedTypeInfo)t).getArg(0).isDataObjectHolder()) {
        ClassTypeInfo doType = (ClassTypeInfo) ((ParameterizedTypeInfo) t).getArg(0);
        writer.stmt(String.format(
          "_json.put(\"%s\", new JsonArray(%s == null ? java.util.Collections.emptyList() : %s.stream().map(v -> %s).collect(Collectors.toList())))",
          name,
          name,
          name,
          GeneratorUtils.generateSerializeDataObject("v", doType)
        ));
      } else
          writer.stmt("_json.put(\"" + name + "\", new JsonArray(" + name + "))");
    } else if (t.getKind() == ClassKind.SET) {
      if (((ParameterizedTypeInfo)t).getArg(0).isDataObjectHolder()) {
        ClassTypeInfo doType = (ClassTypeInfo) ((ParameterizedTypeInfo) t).getArg(0);
        writer.stmt(String.format(
          "_json.put(\"%s\", new JsonArray(%s == null ? java.util.Collections.emptyList() : %s.stream().map(v -> %s).collect(Collectors.toList())))",
          name,
          name,
          name,
          GeneratorUtils.generateSerializeDataObject("v", doType)
        ));
      } else
          writer.stmt("_json.put(\"" + name + "\", new JsonArray(new ArrayList<>(" + name + ")))");
    } else if (t.getKind() == ClassKind.MAP) {
        if (((ParameterizedTypeInfo)t).getArg(1).isDataObjectHolder()) {
          ClassTypeInfo doType = (ClassTypeInfo) ((ParameterizedTypeInfo) t).getArg(1);
          writer.stmt(String.format(
            "_json.put(\"%s\", new JsonObject(%s == null ? java.util.Collections.emptyMap() : %s.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> %s))))",
            name,
            name,
            name,
            GeneratorUtils.generateSerializeDataObject("e.getValue()", doType)
          ));
        } else {
            writer.stmt("_json.put(\"" + name + "\", new JsonObject(ProxyUtils.convertMap(" + name + ")))");
          }
      }
      else if (t.isDataObjectHolder()) {
        writer.stmt(String.format(
          "_json.put(\"%s\", %s)",
          name,
          GeneratorUtils.generateSerializeDataObject(name, (ClassTypeInfo) t)
        ));
      } else
          writer.stmt("_json.put(\"" + name + "\", " + name + ")");
  }

  private void generateSendCallWithResultHandler(TypeInfo futurType, String name, CodeWriter writer, boolean promise) {
    wrapResult(futurType, name, promise, writer);
  }

  private void wrapResult(TypeInfo t, String name, boolean promise, CodeWriter writer) {
    if (promise) {
      writer.print("return ");
    }
    writer
      .print("_vertx.eventBus().<" + sendTypeParameter(t) + ">request(_address, _json, _deliveryOptions");
    if (promise) {
      writer.println(").map(msg -> {");
      writer.indent();
      writer.print("return ");
    } else {
      writer.println(").onComplete(res -> {");
      writer.indent()
        .code("if (res.failed()) {\n")
        .indent();
      writer.stmt(name + ".handle(Future.failedFuture(res.cause()))");
      writer.unindent()
        .code("} else {\n")
        .indent();

      writer.print(name + ".handle(Future.succeededFuture(");
    }
    String resultStr = promise ? "msg" : "res.result()";

    if (t.getKind() == ClassKind.LIST) {
      if ("java.lang.Character".equals(((ParameterizedTypeInfo) t).getArg(0).getName()))
        writer.print("ProxyUtils.convertToListChar(" + resultStr + ".body())");
      else if (((ParameterizedTypeInfo) t).getArg(0).isDataObjectHolder()) {
        ClassTypeInfo doType = (ClassTypeInfo) ((ParameterizedTypeInfo) t).getArg(0);
        writer.print(resultStr + ".body().stream()\n");
        writer.indent()
          .codeln(".map(v -> " + GeneratorUtils.generateDeserializeDataObject("v", doType) + ")")
          .code(".collect(Collectors.toList())")
          .unindent();
      } else {
        writer.print("ProxyUtils.convertList(" + resultStr + ".body().getList())");
      }
    } else if (t.getKind() == ClassKind.SET) {
      if ("java.lang.Character".equals(((ParameterizedTypeInfo)t).getArg(0).getName()))
        writer.print("ProxyUtils.convertToSetChar(" + resultStr + ".body())");
      else if (((ParameterizedTypeInfo)t).getArg(0).isDataObjectHolder()) {
        ClassTypeInfo doType = (ClassTypeInfo) ((ParameterizedTypeInfo) t).getArg(0);
        writer.print(resultStr + ".body().stream()\n");
        writer.indent()
          .codeln(".map(v -> " + GeneratorUtils.generateDeserializeDataObject("v", doType) + ")")
          .code(".collect(Collectors.toSet())")
          .unindent();
      } else {
        writer.print("ProxyUtils.convertSet(" + resultStr + ".body().getList())");
      }
    } else if (t.getKind() == ClassKind.MAP) {
      if ("java.lang.Character".equals(((ParameterizedTypeInfo)t).getArg(1).getName()))
        writer.print("ProxyUtils.convertToMapChar(" + resultStr + ".body())");
      else if (((ParameterizedTypeInfo)t).getArg(1).isDataObjectHolder()) {
        ClassTypeInfo doType = (ClassTypeInfo) ((ParameterizedTypeInfo) t).getArg(1);
        writer.print(resultStr + ".body().stream()\n");
        writer.indent()
          .code(".collect(Collectors.toMap(Map.Entry::getKey, e -> " + GeneratorUtils.generateDeserializeDataObject("e.getValue()", doType) + "))")
          .unindent();
      } else {
        writer.print("ProxyUtils.convertMap(" + resultStr + ".body().getMap())");
      }
    } else if (t.getKind() == ClassKind.API && t instanceof ApiTypeInfo && ((ApiTypeInfo)t).isProxyGen()) {
      writer.print("new " + t.getSimpleName() + "VertxEBProxy(_vertx, " + resultStr + ".headers().get(\"proxyaddr\"))");
    } else if (t.isDataObjectHolder())
      writer.print(GeneratorUtils.generateDeserializeDataObject(resultStr + ".body()", (ClassTypeInfo) t));
    else if (t.getKind() == ClassKind.ENUM)
      writer.print(resultStr + ".body() == null ? null : " + t.getSimpleName() + ".valueOf(" + resultStr + ".body())");
    else
      writer.print(resultStr + ".body()");

    if (promise) {
      writer.println(";");
    } else {
      writer.println("));");
    }

    if (!promise) {
      writer
        .unindent()
        .code("}\n");
    }

      writer.unindent()
      .code("});\n");

  }

  private String sendTypeParameter(TypeInfo t) {
    if (t.getKind() == ClassKind.LIST || t.getKind() == ClassKind.SET) return "JsonArray";
    if (t.getKind() == ClassKind.MAP) return "JsonObject";
    if (t.isDataObjectHolder()) return t.getDataObject().getJsonType().getSimpleName();
    if (t.getKind() == ClassKind.ENUM) return "String";
    return t.getSimpleName();
  }
}
