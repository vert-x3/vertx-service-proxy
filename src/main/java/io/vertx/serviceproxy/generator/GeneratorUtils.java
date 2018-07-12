package io.vertx.serviceproxy.generator;

import io.vertx.codegen.ParamInfo;
import io.vertx.codegen.type.ClassKind;
import io.vertx.codegen.type.ParameterizedTypeInfo;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author <a href="http://slinkydeveloper.github.io">Francesco Guardiani @slinkydeveloper</a>
 */
public class GeneratorUtils {

  final String classHeader;
  final String proxyGenImports;
  final String roger;

  public GeneratorUtils() {
    classHeader = loadResource("class_header");
    proxyGenImports = loadResource("proxy_gen_import");
    roger = loadResource("roger");
  }

  public void classHeader(PrintWriter w) {
    w.print(classHeader);
  }

  public void proxyGenImports(PrintWriter w) {
    w.print(proxyGenImports);
  }

  public void roger(PrintWriter w) { w.print(roger); }

  public void writeImport(PrintWriter w, String i) {
    w.print("import " + i + ";\n");
  }

  private String loadResource(String resource) {
    try {
      Path p = Paths.get(GeneratorUtils.class.getResource(resource + ".txt").toURI());
      return String.join("\n", Files.readAllLines(p, StandardCharsets.UTF_8));
    } catch (Exception e) { // Should never happen
      e.printStackTrace();
      return null;
    }
  }

  public boolean isResultHandler(ParamInfo param) {
    return param != null &&
      param.getType().getKind() == ClassKind.HANDLER &&
      ((ParameterizedTypeInfo)param.getType()).getArg(0).getKind() == ClassKind.ASYNC_RESULT;
  }

}
