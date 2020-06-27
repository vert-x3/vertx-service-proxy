package io.vertx.serviceproxy.generator;

import static org.junit.Assert.assertEquals;

import io.vertx.core.Vertx;
import java.util.ArrayList;
import org.junit.Test;

public class GeneratorUtilsTest {

  private final GeneratorUtils utils = new GeneratorUtils();

  @Test
  public void testReplaceStringPlaceholder() {
    String expected = "_json.put(\"test\", \"someString\");";
    String generated = utils.generatedCodeBlock("_json.put(\"test\", $S);", "someString");
    assertEquals(expected, generated);
  }

  @Test
  public void testReplaceTypePlaceholder() {
    String expected = "io.vertx.core.Vertx vertx = new io.vertx.core.Vertx();";
    String generated = utils.generatedCodeBlock("$T vertx = new $T();", Vertx.class, Vertx.class);
    assertEquals(expected, generated);
  }

  @Test
  public void testReplaceLiteralPlaceholder() {
    String expected = "case 1: System.out.println(1);";
    String generated = utils.generatedCodeBlock("case 1: $L;", "System.out.println(1)");
    assertEquals(expected, generated);
  }

  @Test
  public void testReplaceAllTypes() {
    String expected = "_json.put(\"test\", new JsonArray(new java.util.ArrayList<>(test)));";
    String generated = utils.generatedCodeBlock("_json.put($S, new JsonArray(new $T<>($L)));", "test", ArrayList.class, "test");
    assertEquals(expected, generated);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUnknownPlaceholderTypes() {
    utils.generatedCodeBlock("System.out.println($X)", "whatever");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testTooFewArguments() {
    utils.generatedCodeBlock("System.out.println($S)");
  }
}
