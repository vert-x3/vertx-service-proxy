package io.vertx.serviceproxy.generator;

import com.squareup.javapoet.CodeBlock;
import io.vertx.codegen.ParamInfo;
import io.vertx.codegen.type.ClassKind;
import io.vertx.codegen.type.ClassTypeInfo;
import io.vertx.codegen.type.MapperInfo;
import io.vertx.codegen.type.ParameterizedTypeInfo;
import io.vertx.serviceproxy.generator.model.ProxyModel;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.stream.Stream;

/**
 * @author <a href="http://slinkydeveloper.github.io">Francesco Guardiani @slinkydeveloper</a>
 */
public class GeneratorUtils {

  final String proxyHandlerTemplate;
  final String proxyHandlerHandleMethodTemplate;
  final String proxyHandlerHandleMethodCaseTemplate;
  final String proxyHandlerHandleMethodDataObjectTypeHandle;
  final String proxyHandlerHandleMethodMapTypeHandle;
  final String proxyHandlerHandleMethodListOrSetTypeHandle;
  final String proxyHandlerHandleMethodApiTypeHandle;
  final String proxyTemplate;

  public GeneratorUtils() {
    proxyTemplate = loadResource("proxy_class_template.java");
    proxyHandlerTemplate = loadResource("proxy_handler_class_template.java");
    proxyHandlerHandleMethodTemplate = loadResource("part/proxy_handler_handle_method_body_template.java");
    proxyHandlerHandleMethodCaseTemplate = loadResource("part/proxy_handler_handle_method_case_statement_template.java");
    proxyHandlerHandleMethodDataObjectTypeHandle = loadResource("part/proxy_handler_handle_method_data_object_type_handle.java", "vertx-service-proxy");
    proxyHandlerHandleMethodMapTypeHandle = loadResource("part/proxy_handler_handle_method_map_type_handle.java", "vertx-service-proxy");
    proxyHandlerHandleMethodListOrSetTypeHandle = loadResource("part/proxy_handler_handle_method_list_or_set_type_handle.java", "vertx-service-proxy");
    proxyHandlerHandleMethodApiTypeHandle = loadResource("part/proxy_handler_handle_method_api_type_handle.java", "vertx-service-proxy");
  }

  public Stream<String> additionalImports(ProxyModel model) {
    return Stream
      .concat(
        model.getImportedTypes().stream(),
        model.getReferencedDataObjectTypes()
          .stream()
          .filter(t -> t.isDataObjectHolder() && t.getDataObject().getJsonType() instanceof ClassTypeInfo)
          .map(t -> (ClassTypeInfo) t.getDataObject().getJsonType())
      )
      .filter(c -> !c.getPackageName().equals("java.lang") && !c.getPackageName().equals("io.vertx.core.json"))
      .map(ClassTypeInfo::toString)
      .distinct();
  }

  public String loadResource(String resource) {
    return loadResource(resource, "vertx-service-proxy");
  }

  public String loadResource(String resource, String moduleName) {
    InputStream input = GeneratorUtils.class.getResourceAsStream("/META-INF/vertx/" + moduleName + "/" + resource);
    try (Scanner scanner = new Scanner(input, StandardCharsets.UTF_8.name())) {
      return scanner.useDelimiter("\\A").next();
    }
  }

  public boolean isResultHandler(ParamInfo param) {
    return param != null &&
      param.getType().getKind() == ClassKind.HANDLER &&
      ((ParameterizedTypeInfo)param.getType()).getArg(0).getKind() == ClassKind.ASYNC_RESULT;
  }

  public static String generateDeserializeDataObject(String stmt, ClassTypeInfo doTypeInfo) {
    MapperInfo deserializer = doTypeInfo.getDataObject().getDeserializer();
    String s;
    switch (deserializer.getKind()) {
      case SELF:
        s = String.format("new %s((%s)%s)", doTypeInfo.getName(), doTypeInfo.getDataObject().getJsonType().getSimpleName(), stmt);
        break;
      case STATIC_METHOD:
        StringBuilder sb = new StringBuilder(deserializer.getQualifiedName());
        deserializer.getSelectors().forEach(selector -> {
          sb.append('.').append(selector);
        });
        sb.append("((").append(deserializer.getJsonType().getSimpleName()).append(')').append(stmt).append(')');
        s =  sb.toString();
        break;
      default:
        throw new AssertionError();
    }
    return String.format("%s != null ? %s : null", stmt, s);
  }

  public static String generateSerializeDataObject(String stmt, ClassTypeInfo doTypeInfo) {
    MapperInfo serializer = doTypeInfo.getDataObject().getSerializer();
    StringBuilder sb = new StringBuilder();
    serializer.getSelectors().forEach(selector -> {
      sb.append('.').append(selector);
    });
    String s;
    switch (serializer.getKind()) {
      case SELF:
        s = stmt + sb + "()";
        break;
      case STATIC_METHOD:
        s = serializer.getQualifiedName() + sb + "(" + stmt + ")";
        break;
      default:
        throw new AssertionError();
    }
    return String.format("%s != null ? %s : null", stmt, s);
  }

  public String generatedCodeBlock(String format, Object... args) {
    return CodeBlock
      .builder()
      .add(format, args)
      .build()
      .toString();
  }

}
