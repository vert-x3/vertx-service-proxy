package io.vertx.serviceproxy.generator;

import static com.github.javaparser.StaticJavaParser.parse;
import static com.github.javaparser.StaticJavaParser.parseBlock;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier.Keyword;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import io.vertx.codegen.Generator;
import io.vertx.codegen.MethodInfo;
import io.vertx.codegen.ParamInfo;
import io.vertx.codegen.annotations.ModuleGen;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.type.ApiTypeInfo;
import io.vertx.codegen.type.ClassKind;
import io.vertx.codegen.type.ClassTypeInfo;
import io.vertx.codegen.type.ParameterizedTypeInfo;
import io.vertx.codegen.type.TypeInfo;
import io.vertx.serviceproxy.generator.model.ProxyMethodInfo;
import io.vertx.serviceproxy.generator.model.ProxyModel;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    ClassOrInterfaceDeclaration proxyClassDeclaration = utils.getClassDeclaration(template);

    template.setPackageDeclaration(model.getIfacePackageName());
    proxyClassDeclaration.setName(className);
    utils.additionalImports(model).forEach(template::addImport);
    utils.setConstructorsNames(className, template);
    utils.setServiceClassNames(model, template);
    addGeneratedMethodsToTemplate(model, proxyClassDeclaration);

    return template.toString();
  }

  private void addGeneratedMethodsToTemplate(ProxyModel model, ClassOrInterfaceDeclaration proxyHandlerClassTemplate) {
    MethodDeclaration methodTemplate = new MethodDeclaration().setModifier(Keyword.PUBLIC, true);
    List<MethodDeclaration> methods = generateMethods(model, methodTemplate);
    methods.forEach(proxyHandlerClassTemplate::addMember);
  }

  private List<MethodDeclaration> generateMethods(ProxyModel model, MethodDeclaration methodTemplate) {
    List<MethodDeclaration> generatedMethods = new ArrayList<>();

    for (MethodInfo m : model.getMethods()) {
      if (!m.isStaticMethod()) {
        MethodDeclaration method = generateMethod(methodTemplate, m);
        generatedMethods.add(method);
      }
    }

    return generatedMethods;
  }

  private MethodDeclaration generateMethod(MethodDeclaration methodTemplate, MethodInfo m) {
    MethodDeclaration method = methodTemplate.clone();
    StringBuilder methodBody = new StringBuilder();

    method.setName(m.getName());

    if (!m.getTypeParams().isEmpty()) {
      utils.setTypeParams(m, method);
    }

    utils.setMethodParams(m, method);
    utils.setReturnType(m, method);

    if (!((ProxyMethodInfo) m).isProxyIgnore()) {
      methodBody.append(generateMethodBody((ProxyMethodInfo) m));
    }

    if (m.isFluent()) {
      methodBody.append("return this;");
    }

    method.setBody(parsedMethodBody(methodBody.toString()));

    return method;
  }

  private BlockStmt parsedMethodBody(String methodBody) {
    return parseBlock("{" + methodBody + "}");
  }

  private String generateMethodBody(ProxyMethodInfo method) {
    ParamInfo lastParam = utils.getLastParam(method);
    boolean hasResultHandler = utils.isResultHandler(lastParam);
    StringBuilder methodBody = new StringBuilder();

    if (hasResultHandler) {
      List<ParamInfo> paramsExcludedHandler = utils.paramsWithoutResultHandler(method);
      methodBody.append(utils.generatedCodeBlock(
        "if (closed) {\n"
          + "  $L.handle(Future.failedFuture(new IllegalStateException(\"Proxy is closed\")));\n"
          + "  $L;\n"
          + "}\n",
        lastParam.getName(), method.isFluent() ? "return this" : "return"));
      methodBody.append(commonPart(method, paramsExcludedHandler));
      methodBody.append(generateSendCallWithResultHandler(lastParam));
    } else {
      List<ParamInfo> params = method.getParams().isEmpty() ? new ArrayList<>() : method.getParams();
      methodBody.append(utils.generatedCodeBlock("if (closed) throw new IllegalStateException($S);", "Proxy is closed"));
      methodBody.append(commonPart(method, params));
      methodBody.append(utils.generatedCodeBlock("_vertx.eventBus().send(_address, _json, _deliveryOptions);"));
    }

    if (method.isProxyClose()) {
      methodBody.append(utils.generatedCodeBlock("closed = true;"));
    }

    return methodBody.toString();
  }

  private String commonPart(ProxyMethodInfo method, List<ParamInfo> paramsExcludedHandler) {
    StringBuilder methodBody = new StringBuilder();
    String addToJsonStatements = paramsExcludedHandler
      .stream()
      .map(this::addToJsonStatement)
      .collect(Collectors.joining("\n"));

    methodBody.append(utils.generatedCodeBlock("JsonObject _json = new JsonObject();"));
    methodBody.append(addToJsonStatements);
    methodBody.append(utils.generatedCodeBlock("DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();"));
    methodBody.append(utils.generatedCodeBlock("_deliveryOptions.addHeader($S, $S);", "action", method.getName()));

    return methodBody.toString();
  }

  private String addToJsonStatement(ParamInfo param) {
    TypeInfo t = param.getType();
    String name = param.getName();
    if ("char".equals(t.getName())) {
      return addIntToJson(name);
    } else if ("java.lang.Character".equals(t.getName())) {
      return addBoxedIntToJson(name);
    } else if (t.getKind() == ClassKind.ENUM) {
      return addEnumToJson(name);
    } else if (t.getKind() == ClassKind.LIST) {
      return addListToJson((ParameterizedTypeInfo) t, name);
    } else if (t.getKind() == ClassKind.SET) {
      return addSetToJson((ParameterizedTypeInfo) t, name);
    } else if (t.getKind() == ClassKind.MAP) {
      return addMapToJson((ParameterizedTypeInfo) t, name);
    } else if (t.isDataObjectHolder()) {
      return addDataObjectHolderToJson((ClassTypeInfo) t, name);
    } else {
      return utils.generatedCodeBlock("_json.put($S, $L);", name, name);
    }
  }

  private String addIntToJson(String name) {
    return utils.generatedCodeBlock("_json.put($S, (int)$L);", name, name);
  }

  private String addBoxedIntToJson(String name) {
    return utils.generatedCodeBlock("_json.put($S, $L == null ? null : (int) $L);", name, name, name);
  }

  private String addEnumToJson(String name) {
    return utils.generatedCodeBlock("_json.put($S, $L == null ? null : $L.name());", name, name, name);
  }

  private String addListToJson(ParameterizedTypeInfo t, String name) {
    if (t.getArg(0).isDataObjectHolder()) {
      return addJsonArray(t, name, "v");
    } else {
      return utils.generatedCodeBlock("_json.put($S, new JsonArray($L));", name, name);
    }
  }

  private String addSetToJson(ParameterizedTypeInfo t, String name) {
    if (t.getArg(0).isDataObjectHolder()) {
      return addJsonArray(t, name, "v");
    } else {
      return utils.generatedCodeBlock("_json.put($S, new JsonArray(new ArrayList<>($L)));", name, name);
    }
  }

  private String addJsonArray(ParameterizedTypeInfo t, String name, String stmt) {
    ClassTypeInfo doType = (ClassTypeInfo) t.getArg(0);
    return utils.generatedCodeBlock(
      "_json"
        + ".put($S, new JsonArray($L == null ? java.util.Collections.emptyList() : $L.stream().map(v -> $L)"
        + ".collect(Collectors.toList())));",
      name, name, name, GeneratorUtils.generateSerializeDataObject(stmt, doType)
    );
  }

  private String addMapToJson(ParameterizedTypeInfo t, String name) {
    if (t.getArg(1).isDataObjectHolder()) {
      ClassTypeInfo doType = (ClassTypeInfo) t.getArg(1);
      return utils.generatedCodeBlock(
        "_json"
          + ".put($S, new JsonObject($L == null ? java.util.Collections.emptyMap() : $L.entrySet().stream()"
          + ".collect(Collectors.toMap(Map.Entry::getKey, e -> $L))));",
        name, name, name, GeneratorUtils.generateSerializeDataObject("e.getValue()", doType)
      );
    } else {
      return utils.generatedCodeBlock("_json.put($S, new JsonObject(ProxyUtils.convertMap($L)));", name, name);
    }
  }

  private String addDataObjectHolderToJson(ClassTypeInfo t, String name) {
    return utils.generatedCodeBlock("_json.put($S, $L);", name, GeneratorUtils.generateSerializeDataObject(name, t));
  }

  private String generateSendCallWithResultHandler(ParamInfo lastParam) {
    String name = lastParam.getName();
    TypeInfo t = ((ParameterizedTypeInfo) ((ParameterizedTypeInfo) lastParam.getType()).getArg(0)).getArg(0);
    return wrapResult(t, name, false);
  }

  private String wrapResult(TypeInfo t, String name, boolean promise) {
    if (promise) {
      return utils.generatedCodeBlock(
        "return _vertx.eventBus().<$L>request(_address, _json, _deliveryOptions" + ").map(msg -> { return msg$L;});\n",
        sendTypeParameter(t), responseResult(t, "msg"));
    } else {
      return utils.generatedCodeBlock(""
        + "_vertx.eventBus().<$L>request(_address, _json, _deliveryOptions" + ", res -> { if (res.failed()) {\n"
        + "$L.handle(Future.failedFuture(res.cause()));"
        + "} else {\n"
        + "$L.handle(Future.succeededFuture($L));}"
        + "\n});\n", sendTypeParameter(t), name, name, responseResult(t, "res.result()"));
    }
  }

  private String responseResult(TypeInfo t, String resultStr) {
    if (t.getKind() == ClassKind.LIST) {
      return listResponseResult((ParameterizedTypeInfo) t, resultStr);
    } else if (t.getKind() == ClassKind.SET) {
      return setResponseResult((ParameterizedTypeInfo) t, resultStr);
    } else if (t.getKind() == ClassKind.MAP) {
      return mapResponseResult((ParameterizedTypeInfo) t, resultStr);
    } else if (t.getKind() == ClassKind.API && t instanceof ApiTypeInfo && ((ApiTypeInfo) t).isProxyGen()) {
      return utils.generatedCodeBlock("new $LVertxEBProxy(_vertx, $L.headers().get($S))",
        t.getSimpleName(), resultStr, "proxyaddr");
    } else if (t.isDataObjectHolder()) {
      return GeneratorUtils.generateDeserializeDataObject(resultStr + ".body()", (ClassTypeInfo) t);
    } else if (t.getKind() == ClassKind.ENUM) {
      return utils.generatedCodeBlock("$L.body() == null ? null : $L.valueOf($L.body())",
        resultStr, t.getSimpleName(), resultStr);
    } else {
      return utils.generatedCodeBlock("$L.body()", resultStr);
    }
  }

  private String listResponseResult(ParameterizedTypeInfo t, String resultStr) {
    if ("java.lang.Character".equals(t.getArg(0).getName())) {
      return utils.generatedCodeBlock("ProxyUtils.convertToListChar($L.body())", resultStr);
    } else if (t.getArg(0).isDataObjectHolder()) {
      ClassTypeInfo doType = (ClassTypeInfo) t.getArg(0);
      return utils.generatedCodeBlock(
        "$L.body()"
          + ".stream()\n"
          + ".map(v -> $L)"
          + ".collect(Collectors.toList())",
        resultStr, GeneratorUtils.generateDeserializeDataObject("v", doType));
    } else {
      return utils.generatedCodeBlock("ProxyUtils.convertList($L.body().getList())", resultStr);
    }
  }

  private String setResponseResult(ParameterizedTypeInfo t, String resultStr) {
    if ("java.lang.Character".equals(t.getArg(0).getName())) {
      return utils.generatedCodeBlock("ProxyUtils.convertToSetChar($L.body())", resultStr);
    } else if (t.getArg(0).isDataObjectHolder()) {
      ClassTypeInfo doType = (ClassTypeInfo) t.getArg(0);
      return utils.generatedCodeBlock(
        "$L.body()"
          + ".stream()\n"
          + ".map(v -> $L)"
          + ".collect(Collectors.toSet())",
        resultStr, GeneratorUtils.generateDeserializeDataObject("v", doType));
    } else {
      return utils.generatedCodeBlock("ProxyUtils.convertSet($L.body().getList())", resultStr);
    }
  }

  private String mapResponseResult(ParameterizedTypeInfo t, String resultStr) {
    if ("java.lang.Character".equals(t.getArg(1).getName())) {
      return utils.generatedCodeBlock("ProxyUtils.convertToMapChar($L.body())", resultStr);
    } else if (t.getArg(1).isDataObjectHolder()) {
      ClassTypeInfo doType = (ClassTypeInfo) t.getArg(1);
      return utils.generatedCodeBlock(
        "$L.body()"
          + ".stream()\n"
          + ".collect(Collectors.toMap(Map.Entry::getKey, e -> $L))",
        resultStr, GeneratorUtils.generateDeserializeDataObject("e.getValue()", doType));
    } else {
      return utils.generatedCodeBlock("ProxyUtils.convertMap($L.body().getMap())", resultStr);
    }
  }

  private String sendTypeParameter(TypeInfo t) {
    if (t.getKind() == ClassKind.LIST || t.getKind() == ClassKind.SET) return "JsonArray";
    if (t.getKind() == ClassKind.MAP) return "JsonObject";
    if (t.isDataObjectHolder()) return t.getDataObject().getJsonType().getSimpleName();
    if (t.getKind() == ClassKind.ENUM) return "String";
    return t.getSimpleName();
  }
}
