package io.vertx.serviceproxy.generator;

import io.vertx.codegen.*;
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
          .code("try{")
          .indent()
            .stmt("this._vertx.eventBus().registerDefaultCodec(ServiceException.class, new ServiceExceptionMessageCodec())")
          .unindent()
          .code("} catch (IllegalStateException ex) {}")
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
        writer.code("public ");
        if (!m.getTypeParams().isEmpty()) {
          writer.write("<");
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
    boolean hasResultHandler = utils.isResultHandler(lastParam);
    if (hasResultHandler) {
      writer.code("if (closed) {\n");
      writer.indent();
      writer.stmt(lastParam.getName() + ".handle(Future.failedFuture(new IllegalStateException(\"Proxy is closed\")))");
      if (method.isFluent())
        writer.stmt("return this");
      else
        writer.stmt("return");
      writer.unindent();
      writer.code("}\n");
    } else {
      writer.code("if (closed) throw new IllegalStateException(\"Proxy is closed\");\n");
    }
    if (method.isProxyClose())
      writer.stmt("closed = true");
    writer.stmt("JsonObject _json = new JsonObject()");
    List<ParamInfo> paramsExcludedHandler =
      (method.getParams().isEmpty()) ? new ArrayList<>() :
        (hasResultHandler) ? method.getParams().subList(0, method.getParams().size() - 1) : method.getParams();
    paramsExcludedHandler.forEach(p -> generateAddToJsonStmt(p, writer));
    writer.newLine();
    writer.stmt("DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions()");
    writer.stmt("_deliveryOptions.addHeader(\"action\", \"" + method.getName() + "\")");
    if (hasResultHandler) {
      generateSendCallWithResultHandler(lastParam, writer);
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
    else if (t.getKind() == ClassKind.ENUM)
      writer.stmt("_json.put(\"" + name + "\", " + name + " == null ? null : " + name + ".name())");
    else if (t.getKind() == ClassKind.LIST) {
      if (((ParameterizedTypeInfo)t).getArg(0).getKind() == ClassKind.DATA_OBJECT)
        writer.stmt("_json.put(\"" + name + "\", new JsonArray(" + name + " == null ? java.util.Collections.emptyList() : " + name + ".stream().map(v -> v != null ? " + ((DataObjectTypeInfo)((ParameterizedTypeInfo)t).getArg(0)).getJsonEncoderFQCN() + ".INSTANCE.encode(v) : null).collect(Collectors.toList())))");
      else
        writer.stmt("_json.put(\"" + name + "\", new JsonArray(" + name + "))");
    } else if (t.getKind() == ClassKind.SET) {
      if (((ParameterizedTypeInfo)t).getArg(0).getKind() == ClassKind.DATA_OBJECT)
        writer.stmt("_json.put(\"" + name + "\", new JsonArray(" + name + " == null ? java.util.Collections.emptyList() : " + name + ".stream().map(v -> v != null ? " + ((DataObjectTypeInfo)((ParameterizedTypeInfo)t).getArg(0)).getJsonEncoderFQCN() + ".INSTANCE.encode(v) : null).collect(Collectors.toList())))");
      else
        writer.stmt("_json.put(\"" + name + "\", new JsonArray(new ArrayList<>(" + name + ")))");
    } else if (t.getKind() == ClassKind.MAP)
      if (((ParameterizedTypeInfo)t).getArg(1).getKind() == ClassKind.DATA_OBJECT) {
        DataObjectTypeInfo doTypeInfo = (DataObjectTypeInfo) ((ParameterizedTypeInfo)t).getArg(1);
        writer.stmt("_json.put(\"" + name + "\", new JsonObject(" + name + " == null ? java.util.Collections.emptyMap() : " + name + ".entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue() != null ? " + doTypeInfo.getJsonEncoderFQCN() + ".INSTANCE.encode(e.getValue()) : null))))");
      } else
        writer.stmt("_json.put(\"" + name + "\", new JsonObject(ProxyUtils.convertMap(" + name + ")))");
    else if (t.getKind() == ClassKind.DATA_OBJECT)
      writer.stmt("_json.put(\"" + name + "\", " + name + " != null ? " + ((DataObjectTypeInfo)t).getJsonEncoderFQCN()+ ".INSTANCE.encode(" + name + ") : null)");
    else
      writer.stmt("_json.put(\"" + name + "\", " + name + ")");
  }

  private void generateSendCallWithResultHandler(ParamInfo lastParam, CodeWriter writer) {
    String name = lastParam.getName();
    TypeInfo t = ((ParameterizedTypeInfo)((ParameterizedTypeInfo)lastParam.getType()).getArg(0)).getArg(0);
    writer
      .code("_vertx.eventBus().<" + sendTypeParameter(t) + ">send(_address, _json, _deliveryOptions, res -> {\n")
      .indent()
        .code("if (res.failed()) {\n")
        .indent()
          .stmt(name + ".handle(Future.failedFuture(res.cause()))")
        .unindent()
        .code("} else {\n")
        .indent();

    if (t.getKind() == ClassKind.LIST) {
      if ("java.lang.Character".equals(((ParameterizedTypeInfo) t).getArg(0).getName()))
        writer.stmt(name + ".handle(Future.succeededFuture(ProxyUtils.convertToListChar(res.result().body())))");
      else if (((ParameterizedTypeInfo) t).getArg(0).getKind() == ClassKind.DATA_OBJECT) {
        writer.code(name + ".handle(Future.succeededFuture(res.result().body().stream()\n")
          .indent()
          .codeln(".map(o -> (" + ((DataObjectTypeInfo)((ParameterizedTypeInfo) t).getArg(0)).getTargetJsonType().getSimpleName() + ")o)")
          .codeln(".map(v -> v != null ? " + ((DataObjectTypeInfo)((ParameterizedTypeInfo) t).getArg(0)).getJsonDecoderFQCN() + ".INSTANCE.decode(v) : null)")
          .codeln(".collect(Collectors.toList())));")
          .unindent();
      } else {
        writer.stmt(name + ".handle(Future.succeededFuture(ProxyUtils.convertList(res.result().body().getList())))");
      }
    } else if (t.getKind() == ClassKind.SET) {
      if ("java.lang.Character".equals(((ParameterizedTypeInfo)t).getArg(0).getName()))
        writer.stmt(name + ".handle(Future.succeededFuture(ProxyUtils.convertToSetChar(res.result().body())))");
      else if (((ParameterizedTypeInfo)t).getArg(0).getKind() == ClassKind.DATA_OBJECT) {
        writer.code(name + ".handle(Future.succeededFuture(res.result().body().stream()\n")
          .indent()
          .codeln(".map(o -> (" + ((DataObjectTypeInfo)((ParameterizedTypeInfo) t).getArg(0)).getTargetJsonType().getSimpleName() + ")o)")
          .codeln(".map(v -> v != null ? " + ((DataObjectTypeInfo)((ParameterizedTypeInfo) t).getArg(0)).getJsonDecoderFQCN() + ".INSTANCE.decode(v) : null)")
          .codeln(".collect(Collectors.toSet())));")
          .unindent();
      } else {
        writer.stmt(name + ".handle(Future.succeededFuture(ProxyUtils.convertSet(res.result().body().getList())))");
      }
    } else if (t.getKind() == ClassKind.MAP) {
      if ("java.lang.Character".equals(((ParameterizedTypeInfo)t).getArg(1).getName()))
        writer.stmt(name + ".handle(Future.succeededFuture(ProxyUtils.convertToMapChar(res.result().body())))");
      else if (((ParameterizedTypeInfo)t).getArg(1).getKind() == ClassKind.DATA_OBJECT) {
        DataObjectTypeInfo doTypeInfo = (DataObjectTypeInfo)((ParameterizedTypeInfo) t).getArg(1);
        writer.code(name + ".handle(Future.succeededFuture(res.result().body().stream()\n")
          .indent()
          .codeln(".collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue() != null ? " + doTypeInfo.getJsonDecoderFQCN() + ".INSTANCE.decode((" + doTypeInfo.getTargetJsonType().getSimpleName() + ")e.getValue()) : null))));")
          .unindent();
      } else {
        writer.stmt(name + ".handle(Future.succeededFuture(ProxyUtils.convertMap(res.result().body().getMap())))");
      }
    } else if (t.getKind() == ClassKind.API && t instanceof ApiTypeInfo && ((ApiTypeInfo)t).isProxyGen()) {
      writer.stmt("String addr = res.result().headers().get(\"proxyaddr\")");
      writer.stmt(name + ".handle(Future.succeededFuture(new " + t.getSimpleName() + "VertxEBProxy(_vertx, addr)))");
    } else if (t.getKind() == ClassKind.DATA_OBJECT)
      writer.stmt(name + ".handle(Future.succeededFuture(res.result().body() != null ? " + ((DataObjectTypeInfo)t).getJsonDecoderFQCN() + ".INSTANCE.decode(res.result().body()) : null))");
    else if (t.getKind() == ClassKind.ENUM)
      writer.stmt(name + ".handle(Future.succeededFuture(res.result().body() == null ? null : " + t.getSimpleName() + ".valueOf(res.result().body())))");
    else
      writer.stmt(name + ".handle(Future.succeededFuture(res.result().body()))");

    writer
        .unindent()
        .code("}\n")
      .unindent()
      .code("});\n");

  }

  private String sendTypeParameter(TypeInfo t) {
    if (t.getKind() == ClassKind.LIST || t.getKind() == ClassKind.SET) return "JsonArray";
    if (t.getKind() == ClassKind.MAP) return "JsonObject";
    if (t.getKind() == ClassKind.DATA_OBJECT) return ((DataObjectTypeInfo)t).getTargetJsonType().getSimpleName();
    if (t.getKind() == ClassKind.ENUM) return "String";
    return t.getSimpleName();
  }
}
