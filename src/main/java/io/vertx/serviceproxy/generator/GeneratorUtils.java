package io.vertx.serviceproxy.generator;

import io.vertx.codegen.processor.ParamInfo;
import io.vertx.codegen.processor.type.ClassKind;
import io.vertx.codegen.processor.type.ClassTypeInfo;
import io.vertx.codegen.processor.type.MapperInfo;
import io.vertx.codegen.processor.type.ParameterizedTypeInfo;
import io.vertx.serviceproxy.generator.model.ProxyModel;

import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.stream.Stream;

/**
 * @author <a href="http://slinkydeveloper.github.io">Francesco Guardiani @slinkydeveloper</a>
 */
public class GeneratorUtils {

  final String classHeader;
  final String proxyGenImports;
  final String handlerGenImports;
  final String roger;
  final String handlerConstructorBody;
  final String handlerCloseAccessed;

  public GeneratorUtils() {
    classHeader = loadResource("class_header") + "\n";
    proxyGenImports = loadResource("proxy_gen_import") + "\n";
    handlerGenImports = loadResource("handler_gen_import") + "\n";
    handlerConstructorBody = loadResource("handler_constructor_body") + "\n";
    handlerCloseAccessed = loadResource("handler_close_accessed") + "\n";
    roger = loadResource("roger") + "\n";
  }

  public Stream<String> additionalImports(ProxyModel model) {
    return Stream.concat(
        Stream
          .concat(
            model.getImportedTypes().stream(),
            model.getReferencedDataObjectTypes()
              .stream()
              .filter(t -> t.isDataObjectHolder() && t.getDataObject().getJsonType() instanceof ClassTypeInfo)
              .map(t -> (ClassTypeInfo) t.getDataObject().getJsonType())
          )
          .filter(c -> !c.getPackageName().equals("java.lang") && !c.getPackageName().equals("io.vertx.core.json"))
          .map(ClassTypeInfo::toString),
        model.getReferencedTypes()
          .stream()
          .filter(t -> t.isProxyGen() && !t.getPackageName().equals(model.getIfacePackageName()))
          .map(t -> t.getName() + "VertxEBProxy"))
      .distinct();
  }

  public void classHeader(PrintWriter w) {
    w.print(classHeader);
  }

  public void proxyGenImports(PrintWriter w) {
    w.print(proxyGenImports);
  }

  public void handlerGenImports(PrintWriter w) { w.print(handlerGenImports); }

  public void roger(PrintWriter w) { w.print(roger); }

  public void handlerConstructorBody(PrintWriter w) { w.print(handlerConstructorBody); }

  public void handleCloseAccessed(PrintWriter w) { w.print(handlerCloseAccessed); }

  public void writeImport(PrintWriter w, String i) {
    w.print("import " + i + ";\n");
  }

  public String loadResource(String resource) {
    return loadResource(resource, "vertx-service-proxy");
  }

  public String loadResource(String resource, String moduleName) {
    InputStream input = GeneratorUtils.class.getResourceAsStream("/META-INF/vertx/" + moduleName + "/" + resource + ".txt");
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
}
