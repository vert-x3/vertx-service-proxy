package io.vertx.serviceproxy.generator;

import io.vertx.codegen.Generator;
import io.vertx.codegen.ParamInfo;
import io.vertx.codegen.type.*;
import io.vertx.serviceproxy.generator.model.ProxyMethodInfo;
import io.vertx.serviceproxy.generator.model.ProxyModel;

import java.io.StringWriter;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
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
  public String relativeFilename(ProxyModel model) {
    return model.getIfacePackageName() + "." + className(model) + ".java";
  }

  public String className(ProxyModel model) {
    return model.getIfaceSimpleName() + "VertxProxyHandler";
  }

  public Stream<String> additionalImports(ProxyModel model) {
    return model.getImportedTypes()
      .stream()
      .filter(c -> !c.getPackageName().equals("java.lang"))
      .map(ClassTypeInfo::toString);
  }

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
    additionalImports(model).forEach(i -> utils.writeImport(writer, i));
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
      .code("public " + className + "(Vertx vertx, " + model.getIfaceSimpleName() + " service, boolean topLevel, long timeoutSeconds) {\n");
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
      .stmt("msg.reply(new ServiceException(500, t.getMessage()))")
      .stmt("throw t")
      .unindent()
      .code("}\n")
      .unindent()
      .code("}\n")
      .unindent()
      .code("}");
    return buffer.toString();
  }

  public void generateActionSwitchEntry(ProxyMethodInfo m, CodeWriter writer) {
    ParamInfo lastParam = !m.getParams().isEmpty() ? m.getParam(m.getParams().size() - 1) : null;
    boolean hasResultHandler = utils.isResultHandler(lastParam);
    writer
      .code("case \"" + m.getName() + "\": {\n")
      .indent()
      .code("service." + m.getName() + "(")
      .indent();
    if (hasResultHandler) {
      writer.writeArray(
        ",\n" + writer.indentation(),
        Stream.concat(
          m.getParams().subList(0, m.getParams().size() - 1).stream().map(this::generateJsonParamExtract),
          Stream.of(generateHandler(lastParam))
        ).collect(Collectors.toList()),
        String::toString
      );
    } else {
      writer.writeArray(
        ",\n" + writer.indentation(),
        m.getParams().stream().map(this::generateJsonParamExtract).collect(Collectors.toList()),
        String::toString
      );
    }
    writer.unindent();
    writer.write(");\n");
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
      if (typeArg.getKind() == ClassKind.DATA_OBJECT)
        return "json.getJsonArray(\"" + name + "\").stream().map(o -> new " + typeArg.getName() + "((JsonObject)o)).collect(Collectors.to" + coll + "())";
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
      return "HelperUtils.convertMap(json.getJsonObject(\"" + name + "\").getMap())";
    }
    if (type.getKind() == ClassKind.DATA_OBJECT)
      return "json.getJsonObject(\"" + name + "\") == null ? null : new " + type.getName() + "(json.getJsonObject(\"" + name + "\"))";
    return "(" + type.getName() + ")json.getValue(\"" + name + "\")";
  }

  public String generateHandler(ParamInfo param) {
    TypeInfo typeArg = ((ParameterizedTypeInfo)((ParameterizedTypeInfo)param.getType()).getArg(0)).getArg(0);
    if (typeArg.getKind() == ClassKind.LIST || typeArg.getKind() == ClassKind.SET) {
      String coll = typeArg.getKind() == ClassKind.LIST ? "List" : "Set";
      TypeInfo innerTypeArg = ((ParameterizedTypeInfo)typeArg).getArg(0);
      if (innerTypeArg.getName().equals("java.lang.Character"))
        return "HelperUtils.create" + coll + "CharHandler(msg)";
      if (innerTypeArg.getKind() == ClassKind.DATA_OBJECT)
        return "res -> {\n" +
          "            if (res.failed()) {\n" +
          "              if (res.cause() instanceof ServiceException) {\n" +
          "                msg.reply(res.cause());\n" +
          "              } else {\n" +
          "                msg.reply(new ServiceException(-1, res.cause().getMessage()));\n" +
          "              }\n" +
          "            } else {\n" +
          "              msg.reply(new JsonArray(res.result().stream().map(r -> r == null ? null : r.toJson()).collect(Collectors.toList())));\n" +
          "            }\n" +
          "         }";
      return "HelperUtils.create" + coll + "Handler(msg)";
    }
    if (typeArg.getKind() == ClassKind.DATA_OBJECT)
      return "res -> {\n" +
        "            if (res.failed()) {\n" +
        "              if (res.cause() instanceof ServiceException) {\n" +
        "                msg.reply(res.cause());\n" +
        "              } else {\n" +
        "                msg.reply(new ServiceException(-1, res.cause().getMessage()));\n" +
        "              }\n" +
        "            } else {\n" +
        "              msg.reply(res.result() == null ? null : res.result().toJson());\n" +
        "            }\n" +
        "         }";
    if (typeArg.getKind() == ClassKind.API && ((ApiTypeInfo)typeArg).isProxyGen())
      return "res -> {\n" +
        "            if (res.failed()) {\n" +
        "                if (res.cause() instanceof ServiceException) {\n" +
        "                  msg.reply(res.cause());\n" +
        "                } else {\n" +
        "                  msg.reply(new ServiceException(-1, res.cause().getMessage()));\n" +
        "                }\n" +
        "            } else {\n" +
        "              String proxyAddress = UUID.randomUUID().toString();\n" +
        "              ProxyHelper.registerService(" + typeArg.getSimpleName() + ".class, vertx, res.result(), proxyAddress, false, timeoutSeconds);\n" +
        "              msg.reply(null, new DeliveryOptions().addHeader(\"proxyaddr\", proxyAddress));\n" +
        "            }\n" +
        "          }";
    return "HelperUtils.createHandler(msg)";
  }

}
