package io.vertx.serviceproxy.generator;

import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;
import java.util.function.Function;

/**
 * @author <a href="http://slinkydeveloper.github.io">Francesco Guardiani @slinkydeveloper</a>
 */
public class CodeWriter extends PrintWriter {

  private String indent = "";

  public CodeWriter(@NotNull OutputStream out) {
    super(out);
  }

  public CodeWriter(@NotNull Writer out) {
    super(out);
  }

  public String indentation() {
    return indent;
  }

  public CodeWriter indent() {
    indent += "  ";
    return this;
  }

  public CodeWriter unindent() {
    if (indent.length() >= 2) indent = indent.substring(0, indent.length() - 2);
    return this;
  }

  public CodeWriter writeImport(String i) {
    this.print("import " + i + ";\n");
    return this;
  }

  public CodeWriter code(String line) {
    this.write(indent + line);
    return this;
  }

  public CodeWriter stmt(String line) {
    this.write(indent + line + ";\n");
    return this;
  }

  public CodeWriter newLine() {
    this.write("\n");
    return this;
  }

  public <T> CodeWriter writeArray(String delimiter, List<T> l, Function<T, String> map) {
    if (l.size() == 0) return this;
    int i = 0;
    for (; i < l.size() - 1; i++) {
      write(map.apply(l.get(i)) + delimiter);
    }
    write(map.apply(l.get(i)));
    return this;
  }
}
