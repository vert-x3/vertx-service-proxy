package io.vertx.serviceproxy.generator;

import io.vertx.codegen.Generator;
import io.vertx.codegen.MethodKind;
import io.vertx.codegen.ParamInfo;
import io.vertx.codegen.annotations.ModuleGen;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.type.*;
import io.vertx.codegen.writer.CodeWriter;
import io.vertx.serviceproxy.generator.model.ProxyMethodInfo;
import io.vertx.serviceproxy.generator.model.ProxyModel;

import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author <a href="http://slinkydeveloper.github.io">Francesco Guardiani @slinkydeveloper</a>
 */
public class ServiceProxyHandlerGen extends Generator<ProxyModel> {

  public final GeneratorUtils utils;

  public final static Map<String, String> numericMapping = new HashMap<>();

  static {
    numericMapping.put("byte", "byte");
    numericMapping.put("java.lang.Byte", "byte");
    numericMapping.put("short", "short");
    numericMapping.put("java.lang.Short", "short");
    numericMapping.put("int", "int");
    numericMapping.put("java.lang.Integer", "int");
    numericMapping.put("long", "long");
    numericMapping.put("java.lang.Long", "long");
    numericMapping.put("float", "float");
    numericMapping.put("java.lang.Float", "float");
    numericMapping.put("double", "double");
    numericMapping.put("java.lang.Double", "double");
  }

  public ServiceProxyHandlerGen(GeneratorUtils utils) {
    kinds = Collections.singleton("proxy");
    name = "service_proxy_handler";
    this.utils = utils;
  }

  @Override
  public Collection<Class<? extends Annotation>> annotations() {
    return Arrays.asList(ProxyGen.class, ModuleGen.class);
  }

  @Override
  public String filename(ProxyModel model) {
    return model.getIfacePackageName() + "." + className(model) + ".java";
  }

  public String className(ProxyModel model) {
    return model.getIfaceSimpleName() + "VertxProxyHandler";
  }

  public Stream<String> additionalImports() { return Stream.empty(); }

  @Override
  public String render(ProxyModel model, int index, int size, Map<String, Object> session) {
    StringWriter buffer = new StringWriter();
    CodeWriter writer = new CodeWriter(buffer);
    String className = className(model);
    utils.classHeader(writer);
    writer.stmt("package " + model.getIfacePackageName());
    writer.newLine();
    utils.writeImport(writer, model.getIfaceFQCN());
    utils.handlerGenImports(writer);
    Stream.concat(utils.additionalImports(model), additionalImports()).distinct().forEach(i -> utils.writeImport(writer, i));
    utils.roger(writer);
    writer
      .code("@SuppressWarnings({\"unchecked\", \"rawtypes\"})\n")
      .code("public class " + className + " extends ProxyHandler {\n")
      .newLine()
      .indent()
      .code("public static final long DEFAULT_CONNECTION_TIMEOUT = 5 * 60; // 5 minutes \n")
      .stmt("private final Vertx vertx")
      .stmt("private final " + model.getIfaceSimpleName() + " service")
      .stmt("private final long timerID")
      .stmt("private long lastAccessed")
      .stmt("private final long timeoutSeconds")
      .stmt("private final boolean includeDebugInfo")
      .newLine()
      .code("public " + className + "(Vertx vertx, " + model.getIfaceSimpleName() + " service){\n")
      .indent()
      .stmt("this(vertx, service, DEFAULT_CONNECTION_TIMEOUT)")
      .unindent()
      .code("}\n")
      .newLine()
      .code("public "+ className + "(Vertx vertx, " + model.getIfaceSimpleName() + " service, long timeoutInSecond){\n")
      .indent()
      .stmt("this(vertx, service, true, timeoutInSecond)")
      .unindent()
      .code("}\n")
      .newLine()
      .code("public "+ className + "(Vertx vertx, " + model.getIfaceSimpleName() + " service, boolean topLevel, long timeoutInSecond){\n")
      .indent()
      .stmt("this(vertx, service, true, timeoutInSecond, false)")
      .unindent()
      .code("}\n")
      .newLine()
      .code("public " + className + "(Vertx vertx, " + model.getIfaceSimpleName() + " service, boolean topLevel, long timeoutSeconds, boolean includeDebugInfo) {\n");
    utils.handlerConstructorBody(writer);
    writer.code("private void checkTimedOut(long id) {\n")
      .indent()
      .stmt("long now = System.nanoTime()")
      .code("if (now - lastAccessed > timeoutSeconds * 1000000000) {\n")
      .indent();
    model.getMethods().stream()
      .filter(m -> ((ProxyMethodInfo)m).isProxyClose())
      .forEach(m -> {
        if (m.getParams().isEmpty()) writer.stmt("service." + m.getName() + "()");
        else writer.stmt("service." + m.getName() + "(done -> {})");
      });
    writer
      .stmt("close()")
      .unindent()
      .code("}\n")
      .unindent()
      .code("}\n")
      .newLine();
    utils.handleCloseAccessed(writer);
    writer.code("public void handle(Message<JsonObject> msg) {\n")
      .indent()
      .code("try{\n")
      .indent()
      .stmt("JsonObject json = msg.body()")
      .stmt("String action = msg.headers().get(\"action\")")
      .stmt("if (action == null) throw new IllegalStateException(\"action not specified\")")
      .stmt("accessed()")
      .code("switch (action) {\n")
      .indent();
    model.getMethods().stream().filter(m -> !m.isStaticMethod()).forEach(m -> generateActionSwitchEntry((ProxyMethodInfo) m, writer));
    writer
      .code("default: throw new IllegalStateException(\"Invalid action: \" + action);\n")
      .unindent()
      .code("}\n")
      .unindent()
      .code("} catch (Throwable t) {\n")
      .indent()
      .stmt("if (includeDebugInfo) msg.reply(new ServiceException(500, t.getMessage(), HelperUtils.generateDebugInfo(t)))")
      .stmt("else msg.reply(new ServiceException(500, t.getMessage()))")
      .stmt("throw t")
      .unindent()
      .code("}\n")
      .unindent()
      .code("}\n");
    generateAdditionalMethods(model, writer);
    writer
      .unindent()
      .code("}");
    return buffer.toString();
  }

  public void generateActionSwitchEntry(ProxyMethodInfo m, CodeWriter writer) {
    ParamInfo lastParam = !m.getParams().isEmpty() ? m.getParam(m.getParams().size() - 1) : null;
    writer
      .code("case \"" + m.getName() + "\": {\n")
      .indent()
      .code("service." + m.getName() + "(")
      .indent();
    if (m.getKind() == MethodKind.CALLBACK) {
      TypeInfo typeArg = ((ParameterizedTypeInfo) ((ParameterizedTypeInfo) lastParam.getType()).getArg(0)).getArg(0);
      writer.writeSeq(
        Stream.concat(
          m.getParams().subList(0, m.getParams().size() - 1).stream().map(this::generateJsonParamExtract),
          Stream.of(generateHandler(typeArg))
        ),
        ",\n" + writer.indentation()
      );
      writer.unindent();
      writer.write(");\n");
    } else if (m.getKind() == MethodKind.FUTURE) {
      TypeInfo typeArg = ((ParameterizedTypeInfo) m.getReturnType()).getArg(0);
      writer.writeSeq(
        m.getParams().stream().map(this::generateJsonParamExtract),
        ",\n" + writer.indentation()
      );
      writer.write(").onComplete(");
      writer.write(generateHandler(typeArg));
      writer.write(");\n");
      writer.unindent();
    } else {
      writer.writeSeq(
        m.getParams().stream().map(this::generateJsonParamExtract),
        ",\n" + writer.indentation()
      );
      writer.unindent();
      writer.write(");\n");
    }
    if (m.isProxyClose()) writer.stmt("close()");
    writer.stmt("break");
    writer.unindent();
    writer.code("}\n");
  }

  public String generateJsonParamExtract(ParamInfo param) {
    String name = param.getName();
    TypeInfo type = param.getType();
    String typeName = type.getName();
    if (typeName.equals("char") || typeName.equals("java.lang.Character"))
      return "json.getInteger(\"" + name + "\") == null ? null : (char)(int)(json.getInteger(\"" + name + "\"))";
    if (typeName.equals("byte") || typeName.equals("java.lang.Byte") ||
      typeName.equals("short") || typeName.equals("java.lang.Short") ||
      typeName.equals("int") || typeName.equals("java.lang.Integer") ||
      typeName.equals("long") || typeName.equals("java.lang.Long"))
      return "json.getValue(\"" + name + "\") == null ? null : (json.getLong(\"" + name + "\")." + numericMapping.get(typeName) + "Value())";
    if (typeName.equals("float") || typeName.equals("java.lang.Float") ||
      typeName.equals("double") || typeName.equals("java.lang.Double"))
      return "json.getValue(\"" + name + "\") == null ? null : (json.getDouble(\"" + name + "\")." + numericMapping.get(typeName) + "Value())";
    if (type.getKind() == ClassKind.ENUM)
      return "json.getString(\"" + name + "\") == null ? null : " + param.getType().getName() + ".valueOf(json.getString(\"" + name + "\"))";
    if (type.getKind() == ClassKind.LIST || type.getKind() == ClassKind.SET) {
      String coll = type.getKind() == ClassKind.LIST ? "List" : "Set";
      TypeInfo typeArg = ((ParameterizedTypeInfo)type).getArg(0);
      if (typeArg.isDataObjectHolder()) {
        ClassTypeInfo doType = (ClassTypeInfo) typeArg;
        return String.format(
          "json.getJsonArray(\"%s\").stream().map(v -> %s).collect(Collectors.to%s())",
          name,
          GeneratorUtils.generateDeserializeDataObject("v", doType),
          coll
        );
      }
      if (typeArg.getName().equals("java.lang.Byte") || typeArg.getName().equals("java.lang.Short") ||
        typeArg.getName().equals("java.lang.Integer") || typeArg.getName().equals("java.lang.Long"))
        return "json.getJsonArray(\"" + name + "\").stream().map(o -> ((Number)o)." + numericMapping.get(typeArg.getName()) + "Value()).collect(Collectors.to" + coll + "())";
      return "HelperUtils.convert" + coll + "(json.getJsonArray(\"" + name + "\").getList())";
    }
    if (type.getKind() == ClassKind.MAP) {
      TypeInfo typeArg = ((ParameterizedTypeInfo)type).getArg(1);
      if (typeArg.getName().equals("java.lang.Byte") || typeArg.getName().equals("java.lang.Short") ||
        typeArg.getName().equals("java.lang.Integer") || typeArg.getName().equals("java.lang.Long") ||
        typeArg.getName().equals("java.lang.Float") || typeArg.getName().equals("java.lang.Double"))
        return "json.getJsonObject(\"" + name + "\").getMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> ((java.lang.Number)entry.getValue())." + numericMapping.get(typeArg.getName()) + "Value()))";
      if (typeArg.isDataObjectHolder()) {
        ClassTypeInfo doType = (ClassTypeInfo) typeArg;
        return String.format(
          "json.getJsonObject(\"%s\").stream().collect(Collectors.toMap(Map.Entry::getKey, e -> %s))",
          name,
          GeneratorUtils.generateDeserializeDataObject("e.getValue()", doType)
        );
      }
      return "HelperUtils.convertMap(json.getJsonObject(\"" + name + "\").getMap())";
    }
    if (type.isDataObjectHolder()) {
      ClassTypeInfo doType = (ClassTypeInfo) type;
      String valueExtractionStmt = "json." + resolveDataObjectJsonExtractorMethod(doType.getDataObject()) + "(\"" + name + "\")";
      return GeneratorUtils.generateDeserializeDataObject(valueExtractionStmt, doType);
    }
    return "(" + type.getName() + ")json.getValue(\"" + name + "\")";
  }

  private String resolveDataObjectJsonExtractorMethod(DataObjectInfo info) {
    switch (info.getJsonType().getKind()) {
      case JSON_ARRAY:
        return "getJsonArray";
      case JSON_OBJECT:
        return "getJsonObject";
      default:
        return "getValue";
    }
  }
  public String generateHandler(TypeInfo typeArg) {
    if (typeArg.getKind() == ClassKind.LIST || typeArg.getKind() == ClassKind.SET) {
      String coll = typeArg.getKind() == ClassKind.LIST ? "List" : "Set";
      TypeInfo innerTypeArg = ((ParameterizedTypeInfo)typeArg).getArg(0);
      if (innerTypeArg.getName().equals("java.lang.Character"))
        return "HelperUtils.create" + coll + "CharHandler(msg, includeDebugInfo)";
      if (innerTypeArg.isDataObjectHolder())
        return "res -> {\n" +
          "            if (res.failed()) {\n" +
          "              HelperUtils.manageFailure(msg, res.cause(), includeDebugInfo);\n" +
          "            } else {\n" +
          "              msg.reply(new JsonArray(res.result().stream().map(v -> " + GeneratorUtils.generateSerializeDataObject("v", (ClassTypeInfo) innerTypeArg) + ").collect(Collectors.toList())));\n" +
          "            }\n" +
          "         }";
      return "HelperUtils.create" + coll + "Handler(msg, includeDebugInfo)";
    }
    if (typeArg.getKind() == ClassKind.MAP) {
      TypeInfo innerTypeArg = ((ParameterizedTypeInfo)typeArg).getArg(1);
      if (innerTypeArg.getName().equals("java.lang.Character"))
        return "HelperUtils.createMapCharHandler(msg, includeDebugInfo)";
      if (innerTypeArg.isDataObjectHolder())
        return "res -> {\n" +
          "            if (res.failed()) {\n" +
          "              if (res.cause() instanceof ServiceException) {\n" +
          "                msg.reply(res.cause());\n" +
          "              } else {\n" +
          "                msg.reply(new ServiceException(-1, res.cause().getMessage()));\n" +
          "              }\n" +
          "            } else {\n" +
          "              msg.reply(new JsonObject(res.result().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> " + GeneratorUtils.generateSerializeDataObject("e.getValue()", (ClassTypeInfo) innerTypeArg) + "))));\n" +
          "            }\n" +
          "         }";
      return "HelperUtils.createMapHandler(msg, includeDebugInfo)";
    }
    if (typeArg.isDataObjectHolder())
      return "res -> {\n" +
        "            if (res.failed()) {\n" +
        "              HelperUtils.manageFailure(msg, res.cause(), includeDebugInfo);\n" +
        "            } else {\n" +
        "              msg.reply(" + GeneratorUtils.generateSerializeDataObject("res.result()", (ClassTypeInfo) typeArg) + ");\n" +
        "            }\n" +
        "         }";
    if (typeArg.getKind() == ClassKind.API && ((ApiTypeInfo)typeArg).isProxyGen())
      return "res -> {\n" +
        "            if (res.failed()) {\n" +
        "              HelperUtils.manageFailure(msg, res.cause(), includeDebugInfo);\n" +
        "            } else {\n" +
        "              String proxyAddress = UUID.randomUUID().toString();\n" +
        "              new ServiceBinder(vertx).setAddress(proxyAddress).setTopLevel(false).setTimeoutSeconds(timeoutSeconds).register(" + typeArg.getSimpleName() + ".class, res.result());\n" +
        "              msg.reply(null, new DeliveryOptions().addHeader(\"proxyaddr\", proxyAddress));\n" +
        "            }\n" +
        "          }";
    return "HelperUtils.createHandler(msg, includeDebugInfo)";
  }

  public void generateAdditionalMethods(ProxyModel model, CodeWriter writer) {}

}
