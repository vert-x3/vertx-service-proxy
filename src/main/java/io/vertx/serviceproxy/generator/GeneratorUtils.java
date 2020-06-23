package io.vertx.serviceproxy.generator;

import static com.github.javaparser.StaticJavaParser.parseType;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.type.Type;
import com.squareup.javapoet.CodeBlock;
import io.vertx.codegen.ParamInfo;
import io.vertx.codegen.type.ClassKind;
import io.vertx.codegen.type.ClassTypeInfo;
import io.vertx.codegen.type.MapperInfo;
import io.vertx.codegen.type.ParameterizedTypeInfo;
import io.vertx.serviceproxy.generator.model.ProxyMethodInfo;
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
  final String proxyHandlerTemplate;
  final String proxyHandlerHandleMethodTemplate;

  public GeneratorUtils() {
    classHeader = loadResource("class_header.txt") + "\n";
    proxyGenImports = loadResource("proxy_gen_import.txt") + "\n";
    handlerGenImports = loadResource("handler_gen_import.txt") + "\n";
    handlerConstructorBody = loadResource("handler_constructor_body.txt") + "\n";
    handlerCloseAccessed = loadResource("handler_close_accessed.txt") + "\n";
    roger = loadResource("roger.txt") + "\n";
    proxyHandlerTemplate = loadResource("proxy_handler_class_template.java");
    proxyHandlerHandleMethodTemplate = loadResource("part/proxy_handler_handle_method_body_template.java");
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

  ClassOrInterfaceDeclaration getClassDeclaration(CompilationUnit template) {
    return template
      .findFirst(ClassOrInterfaceDeclaration.class)
      .orElseThrow(() -> new IllegalStateException("Cannot find the class in a template."));
  }

  void setConstructorsNames(String className, CompilationUnit template) {
    template
      .findAll(ConstructorDeclaration.class)
      .forEach(c -> c.setName(className));
  }

  void setServiceTypes(ProxyModel model, ClassOrInterfaceDeclaration template) {
    template
      .findAll(Type.class, type -> "$ServiceName$".equals(type.getElementType().asString()))
      .forEach(type -> type.replace(parseType(model.getIfaceSimpleName())));
  }

  ParamInfo getLastParam(ProxyMethodInfo info) {
    return !info.getParams().isEmpty() ? info.getParam(info.getParams().size() - 1) : null;
  }
}
