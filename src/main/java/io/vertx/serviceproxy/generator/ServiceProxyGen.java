package io.vertx.serviceproxy.generator;

import static com.github.javaparser.StaticJavaParser.parse;
import static com.github.javaparser.StaticJavaParser.parseType;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.type.Type;
import io.vertx.codegen.Generator;
import io.vertx.codegen.MethodInfo;
import io.vertx.codegen.ParamInfo;
import io.vertx.codegen.TypeParamInfo;
import io.vertx.codegen.annotations.ModuleGen;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.type.ApiTypeInfo;
import io.vertx.codegen.type.ClassKind;
import io.vertx.codegen.type.ClassTypeInfo;
import io.vertx.codegen.type.ParameterizedTypeInfo;
import io.vertx.codegen.type.TypeInfo;
import io.vertx.codegen.writer.CodeWriter;
import io.vertx.serviceproxy.generator.model.ProxyMethodInfo;
import io.vertx.serviceproxy.generator.model.ProxyModel;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
    String className = model.getIfaceSimpleName() + "VertxEBProxy";
    CompilationUnit template = parse(utils.proxyTemplate);
    StringWriter buffer = new StringWriter();
    CodeWriter writer = new CodeWriter(buffer);
    ClassOrInterfaceDeclaration proxyHandlerClassTemplate = template
      .findFirst(ClassOrInterfaceDeclaration.class)
      .orElseThrow(() -> new IllegalStateException("Cannot find the class in a template."));

    template.setPackageDeclaration(model.getIfacePackageName());
    proxyHandlerClassTemplate.setName(className);
    template.findAll(ConstructorDeclaration.class).forEach(c -> c.setName(className));
    template.findAll(Type.class, type -> "$ServiceName$".equals(type.toString())).forEach(type -> type.replace(parseType(model.getIfaceSimpleName())));

    utils.additionalImports(model).forEach(template::addImport);
    addGeneratedMethodsToTemplate(model, buffer, writer, proxyHandlerClassTemplate);

    return template.toString();
  }

  private void addGeneratedMethodsToTemplate(ProxyModel model, StringWriter buffer,
    CodeWriter writer, ClassOrInterfaceDeclaration proxyHandlerClassTemplate) {
    generateMethods(model, writer);
    // temporary hack to add methods to a template
    List<CallableDeclaration> temp = parse("class X {\n" + buffer.toString() + "\n}").findAll(CallableDeclaration.class);
    temp.forEach(proxyHandlerClassTemplate::addMember);
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
    } else if (t.getKind() == ClassKind.MAP)
      if (((ParameterizedTypeInfo)t).getArg(1).isDataObjectHolder()) {
        ClassTypeInfo doType = (ClassTypeInfo) ((ParameterizedTypeInfo) t).getArg(1);
        writer.stmt(String.format(
          "_json.put(\"%s\", new JsonObject(%s == null ? java.util.Collections.emptyMap() : %s.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> %s))))",
          name,
          name,
          name,
          GeneratorUtils.generateSerializeDataObject("e.getValue()", doType)
        ));
      } else
        writer.stmt("_json.put(\"" + name + "\", new JsonObject(ProxyUtils.convertMap(" + name + ")))");
    else if (t.isDataObjectHolder()) {
      writer.stmt(String.format(
        "_json.put(\"%s\", %s)",
        name,
        GeneratorUtils.generateSerializeDataObject(name, (ClassTypeInfo) t)
      ));
    } else
      writer.stmt("_json.put(\"" + name + "\", " + name + ")");
  }

  private void generateSendCallWithResultHandler(ParamInfo lastParam, CodeWriter writer) {
    String name = lastParam.getName();
    TypeInfo t = ((ParameterizedTypeInfo)((ParameterizedTypeInfo)lastParam.getType()).getArg(0)).getArg(0);

    wrapResult(t, name, false, writer);
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
      writer.println(", res -> {");
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
