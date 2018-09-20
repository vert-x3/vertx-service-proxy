package io.vertx.serviceproxy.generator;

import io.vertx.codegen.ParamInfo;
import io.vertx.codegen.type.ClassKind;
import io.vertx.codegen.type.ParameterizedTypeInfo;

import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

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

}
