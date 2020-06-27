package io.vertx.serviceproxy.generator;

import static com.github.javaparser.StaticJavaParser.parse;
import static com.github.javaparser.StaticJavaParser.parseBlock;
import static com.github.javaparser.StaticJavaParser.parseStatement;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.Statement;
import io.vertx.codegen.Generator;
import io.vertx.codegen.ParamInfo;
import io.vertx.codegen.annotations.ModuleGen;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.type.ApiTypeInfo;
import io.vertx.codegen.type.ClassKind;
import io.vertx.codegen.type.ClassTypeInfo;
import io.vertx.codegen.type.DataObjectInfo;
import io.vertx.codegen.type.ParameterizedTypeInfo;
import io.vertx.codegen.type.TypeInfo;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.serviceproxy.HelperUtils;
import io.vertx.serviceproxy.ServiceBinder;
import io.vertx.serviceproxy.generator.model.ProxyMethodInfo;
import io.vertx.serviceproxy.generator.model.ProxyModel;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
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

  @Override
  public String render(ProxyModel model, int index, int size, Map<String, Object> session) {
    String className = className(model);
    CompilationUnit template = parse(utils.proxyHandlerTemplate);
    ClassOrInterfaceDeclaration proxyHandlerClassTemplate = utils.getClassDeclaration(template);

    template.setPackageDeclaration(model.getIfacePackageName());
    proxyHandlerClassTemplate.setName(className);
    utils.setConstructorsNames(className, template);
    utils.setServiceTypes(model, proxyHandlerClassTemplate);
    generateCheckTimedOutMethodImplementation(model, proxyHandlerClassTemplate);
    generateHandleMethodImplementation(model, proxyHandlerClassTemplate);

    return template.toString();
  }

  private void generateCheckTimedOutMethodImplementation(ProxyModel model, ClassOrInterfaceDeclaration template) {
    template.getMethodsByName("checkTimedOut")
      .stream()
      .findAny()
      .flatMap(MethodDeclaration::getBody)
      .ifPresent(blockStmt -> blockStmt.getStatements()
        .stream()
        .filter(Statement::isIfStmt)
        .forEach(statement -> statement.getChildNodes()
          .get(1)
          .replace(parseStatement(generateCheckTimedOutMethodImplementation(model)))));
  }

  private String generateCheckTimedOutMethodImplementation(ProxyModel model) {
    return model
      .getMethods()
      .stream()
      .filter(m -> ((ProxyMethodInfo) m).isProxyClose())
      .findFirst()
      .map(info -> {
        if (info.getParams().isEmpty()) {
          return "{"
            + "close();\n"
            + "service." + info.getName() + "();\n"
            + "}";
        } else {
          return "{\n"
            + "close();\n"
            + "service." + info.getName() + "(done -> {});\n"
            + "}";
        }
      })
      .orElse("{\n"
        + "close();\n"
        + "}");
  }

  private void generateHandleMethodImplementation(ProxyModel model, ClassOrInterfaceDeclaration template) {
    template.getMethodsByName("handle")
      .stream()
      .findAny()
      .flatMap(MethodDeclaration::getBody)
      .ifPresent(it -> it.getStatement(0)
        .replace(parseBlock(utils.generatedCodeBlock(utils.proxyHandlerHandleMethodTemplate,
          generateHandleMethodCases(model), HelperUtils.class))));
  }

  private String generateHandleMethodCases(ProxyModel model) {
    return model.getMethods()
      .stream()
      .filter(info -> !info.isStaticMethod())
      .map(it -> (ProxyMethodInfo) it)
      .map(info -> utils.generatedCodeBlock(
        "case $S: {\n"
          + "  service.$L($L);\n"
          + "  $L\n"
          + "}\n",
        info.getName(), info.getName(), generateHandleMethodCaseParams(info), closeProxy(info)))
      .collect(Collectors.joining(""));
  }

  private String closeProxy(ProxyMethodInfo info) {
    return info.isProxyClose() ?
      "\nclose();\n" + "break;" :
      "\nbreak;";
  }

  public String generateHandleMethodCaseParams(ProxyMethodInfo info) {
    ParamInfo lastParam = utils.getLastParam(info);
    boolean hasResultHandler = utils.isResultHandler(lastParam);
    if (hasResultHandler) {
      return generateHandleMethodCaseParamsWithResultHandler(info, lastParam);
    } else {
      return generateHandleMethodCaseParamsWithoutResultHandler(info);
    }
  }

  private String generateHandleMethodCaseParamsWithResultHandler(ProxyMethodInfo info, ParamInfo lastParam) {
    return Stream
      .concat(info.getParams()
          .subList(0, info.getParams().size() - 1)
          .stream()
          .map(this::generateJsonParamExtract),
        Stream.of(generateHandler(lastParam)))
      .collect(Collectors.joining(", \n"));
  }

  private String generateHandleMethodCaseParamsWithoutResultHandler(ProxyMethodInfo info) {
    return info
      .getParams()
      .stream()
      .map(this::generateJsonParamExtract)
      .collect(Collectors.joining(", \n"));
  }

  public String generateJsonParamExtract(ParamInfo param) {
    String name = param.getName();
    TypeInfo type = param.getType();
    String typeName = type.getName();

    if (isCharacter(typeName)) return charParamExtract(name);
    if (isIntegerNumberType(typeName)) return integerParamExtract(name, typeName);
    if (isFloatingNumberType(typeName)) return floatingParamExtract(name, typeName);
    if (type.getKind() == ClassKind.ENUM) return enumParamExtract(param, name);
    if (type.getKind() == ClassKind.LIST || type.getKind() == ClassKind.SET) return listOrSetParamExtract(name, type);
    if (type.getKind() == ClassKind.MAP) return mapParamExtract(name, type);
    if (type.isDataObjectHolder()) {
      ClassTypeInfo doType = (ClassTypeInfo) type;
      String valueExtractionStmt = utils.generatedCodeBlock("json.$L($S)", resolveDataObjectJsonExtractorMethod(doType.getDataObject()), name);
      return GeneratorUtils.generateDeserializeDataObject(valueExtractionStmt, doType);
    }
    return utils.generatedCodeBlock("($L)json.getValue($S)", type.getName(), name);
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

  private boolean isCharacter(String typeName) {
    return typeName.equals("char") ||
      typeName.equals("java.lang.Character");
  }

  private boolean isFloatingNumberType(String typeName) {
    return typeName.equals("float") ||
      typeName.equals("java.lang.Float") ||
      typeName.equals("double") ||
      typeName.equals("java.lang.Double");
  }

  private boolean isIntegerNumberType(String typeName) {
    return isBoxedIntegerType(typeName) ||
      typeName.equals("byte") ||
      typeName.equals("short") ||
      typeName.equals("int") ||
      typeName.equals("long");
  }

  private boolean isBoxedNumericType(String typeName) {
    return isBoxedIntegerType(typeName) ||
      typeName.equals("java.lang.Float") ||
      typeName.equals("java.lang.Double");
  }

  private boolean isBoxedIntegerType(String typeName) {
    return typeName.equals("java.lang.Byte") ||
      typeName.equals("java.lang.Short") ||
      typeName.equals("java.lang.Integer") ||
      typeName.equals("java.lang.Long");
  }

  private boolean isApiType(TypeInfo typeArg) {
    return typeArg.getKind() == ClassKind.API && ((ApiTypeInfo) typeArg).isProxyGen();
  }

  public String generateHandler(ParamInfo param) {
    TypeInfo typeArg = ((ParameterizedTypeInfo) ((ParameterizedTypeInfo) param.getType()).getArg(0)).getArg(0);
    return generateHandler(typeArg);
  }

  public String generateHandler(TypeInfo typeArg) {
    if (typeArg.getKind() == ClassKind.LIST || typeArg.getKind() == ClassKind.SET) return listOrSetTypeHandler(typeArg);
    if (typeArg.getKind() == ClassKind.MAP) return mapTypeHandler((ParameterizedTypeInfo) typeArg);
    if (typeArg.isDataObjectHolder()) return dataObjectTypeHandler((ClassTypeInfo) typeArg);
    if (isApiType(typeArg)) return apiTypeHandler(typeArg);
    return utils.generatedCodeBlock("HelperUtils.createHandler(msg, includeDebugInfo)");
  }

  private String mapParamExtract(String name, TypeInfo type) {
    TypeInfo typeArg = ((ParameterizedTypeInfo) type).getArg(1);
    if (isBoxedNumericType(typeArg.getName())) {
      return utils.generatedCodeBlock(
        "json.getJsonObject($S).getMap().entrySet()"
          + ".stream()"
          + ".collect($T.toMap($T.Entry::getKey, entry -> ((java.lang.Number)entry.getValue()).$LValue()))",
        name, Collectors.class, Map.class, numericMapping.get(typeArg.getName()));
    }
    if (typeArg.isDataObjectHolder()) {
      ClassTypeInfo doType = (ClassTypeInfo) typeArg;
      return utils.generatedCodeBlock(
        "json.getJsonObject($S)"
          + ".stream()"
          + ".collect($T.toMap($T.Entry::getKey, e -> $L))",
        name, Collectors.class, Map.class, GeneratorUtils.generateDeserializeDataObject("e.getValue()", doType));
    }
    return utils.generatedCodeBlock("HelperUtils.convertMap(json.getJsonObject($S).getMap())", name);
  }

  private String listOrSetParamExtract(String name, TypeInfo type) {
    String coll = type.getKind() == ClassKind.LIST ? "List" : "Set";
    TypeInfo typeArg = ((ParameterizedTypeInfo) type).getArg(0);
    if (typeArg.isDataObjectHolder()) {
      ClassTypeInfo doType = (ClassTypeInfo) typeArg;
      return utils.generatedCodeBlock(
        "json.getJsonArray($S)"
          + ".stream()"
          + ".map(v -> $L)"
          + ".collect($T.to$L())",
        name, GeneratorUtils.generateDeserializeDataObject("v", doType), Collectors.class, coll);
    }
    if (isBoxedIntegerType(typeArg.getName())) {
      return utils.generatedCodeBlock(
        "json.getJsonArray($S)"
          + ".stream()"
          + ".map(o -> ((Number)o).$LValue())"
          + ".collect($T.to$L())",
        name, numericMapping.get(typeArg.getName()), Collectors.class, coll);
    }
    return utils.generatedCodeBlock("HelperUtils.convert$L(json.getJsonArray($S).getList())", coll, name);
  }

  private String enumParamExtract(ParamInfo param, String name) {
    return utils.generatedCodeBlock("json.getString($S) == null ? null : $L.valueOf(json.getString($S))",
      name, param.getType().getName(), name);
  }

  private String floatingParamExtract(String name, String typeName) {
    return utils.generatedCodeBlock("json.getValue($S) == null ? null : (json.getDouble($S).$LValue())",
      name, name, numericMapping.get(typeName));
  }

  private String integerParamExtract(String name, String typeName) {
    return utils.generatedCodeBlock("json.getValue($S) == null ? null : (json.getLong($S).$LValue())",
      name, name, numericMapping.get(typeName));
  }

  private String charParamExtract(String name) {
    return utils.generatedCodeBlock("json.getInteger($S) == null ? null : (char)(int)(json.getInteger($S))",
      name, name);
  }

  private String apiTypeHandler(TypeInfo typeArg) {
    return utils.generatedCodeBlock(
      "res -> {\n"
        + "  if (res.failed()) {\n"
        + "    HelperUtils.manageFailure(msg,res.cause(),includeDebugInfo);\n"
        + "  } else {\n"
        + "    String proxyAddress= $T.randomUUID().toString();\n"
        + "    new $T(vertx).setAddress(proxyAddress).setTopLevel(false).setTimeoutSeconds(timeoutSeconds).register($L.class, res.result());\n"
        + "    msg.reply(null, new $T().addHeader(\"proxyaddr\", proxyAddress));\n"
        + "  }\n"
        + "}\n",
      UUID.class, ServiceBinder.class, typeArg.getSimpleName(), DeliveryOptions.class);
  }

  private String dataObjectTypeHandler(ClassTypeInfo typeArg) {
    return utils.generatedCodeBlock(
      "res -> {\n"
        + "  if (res.failed()) {\n"
        + "    HelperUtils.manageFailure(msg, res.cause(), includeDebugInfo);\n"
        + "  } else {\n"
        + "    msg.reply($L);\n"
        + "  }\n"
        + "}\n",
      GeneratorUtils.generateSerializeDataObject("res.result()", typeArg));
  }

  private String mapTypeHandler(ParameterizedTypeInfo typeArg) {
    TypeInfo innerTypeArg = typeArg.getArg(1);
    if (innerTypeArg.getName().equals("java.lang.Character")) {
      return utils.generatedCodeBlock("HelperUtils.createMapCharHandler(msg, includeDebugInfo)");
    }
    if (innerTypeArg.isDataObjectHolder()) {
      return utils.generatedCodeBlock(
        "res -> {\n"
          + "  if (res.failed()) {\n"
          + "    if (res.cause() instanceof ServiceException) {\n"
          + "      msg.reply(res.cause());\n"
          + "    } else {\n"
          + "      msg.reply(new ServiceException(-1, res.cause().getMessage()));\n"
          + "    }\n"
          + "  } else {\n"
          + "    msg.reply(new JsonObject(res.result().entrySet().stream().collect($T.toMap($T.Entry::getKey,e->$L))));\n"
          + "  }\n"
          + "}\n",
        Collectors.class, Map.class, GeneratorUtils.generateSerializeDataObject("e.getValue()", (ClassTypeInfo) innerTypeArg));
    }
    return utils.generatedCodeBlock("HelperUtils.createMapHandler(msg, includeDebugInfo)");
  }

  private String listOrSetTypeHandler(TypeInfo typeArg) {
    String coll = typeArg.getKind() == ClassKind.LIST ? "List" : "Set";
    TypeInfo innerTypeArg = ((ParameterizedTypeInfo) typeArg).getArg(0);
    if (innerTypeArg.getName().equals("java.lang.Character")) {
      return utils.generatedCodeBlock("HelperUtils.create$LCharHandler(msg, includeDebugInfo)", coll);
    }
    if (innerTypeArg.isDataObjectHolder()) {
      return utils.generatedCodeBlock(
        "res -> {\n"
          + "  if (res.failed()) {\n"
          + "    HelperUtils.manageFailure(msg, res.cause(), includeDebugInfo);\n"
          + "  } else {\n"
          + "    msg.reply(new $T(res.result().stream().map(v -> $L).collect($T.toList())));}\n"
          + "}\n",
        JsonArray.class, GeneratorUtils.generateSerializeDataObject("v", (ClassTypeInfo) innerTypeArg), Collectors.class);
    }
    return utils.generatedCodeBlock("HelperUtils.create$LHandler(msg, includeDebugInfo)", coll);
  }
}
