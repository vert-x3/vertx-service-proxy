package io.vertx.serviceproxy.codegen;

import io.vertx.codegen.*;
import io.vertx.codegen.type.ClassKind;
import io.vertx.codegen.type.ParameterizedTypeInfo;
import io.vertx.serviceproxy.codegen.future.FuturizedProxy;
import io.vertx.serviceproxy.codegen.proxytestapi.*;
import io.vertx.serviceproxy.generator.model.ProxyMethodInfo;
import io.vertx.serviceproxy.generator.model.ProxyModel;
import io.vertx.serviceproxy.testmodel.Mappers;
import io.vertx.test.codegen.GeneratorHelper;
import org.junit.Test;

import java.time.ZonedDateTime;

import static org.junit.Assert.*;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class ProxyTest {

  public ProxyModel generateProxyModel(Class c, Class... rest) throws Exception {
    return new GeneratorHelper()
      .registerConverter(ZonedDateTime.class, Mappers.class, "serializeZonedDateTime")
      .registerConverter(ZonedDateTime.class, Mappers.class, "deserializeZonedDateTime")
      .generateClass(codegen -> (ProxyModel) codegen.getModel(c.getCanonicalName(), "proxy"), c, rest);
  }


  // Test invalid stuff
  // ------------------

  // Invalid classes

  @Test
  public void testInvalidOverloaded() throws Exception {
    try {
      generateProxyModel(InvalidOverloaded.class);
      fail("Should throw exception");
    } catch (GenException e) {
      // OK
    }
  }

  @Test
  public void testInvalidParams1() throws Exception {
    try {
      generateProxyModel(InvalidParams1.class);
      fail("Should throw exception");
    } catch (GenException e) {
      // OK
    }
  }

  @Test
  public void testInvalidParams2() throws Exception {
    try {
      generateProxyModel(InvalidParams2.class);
      fail("Should throw exception");
    } catch (GenException e) {
      // OK
    }
  }

  @Test
  public void testInvalidParams3() throws Exception {
    try {
      generateProxyModel(InvalidParams3.class);
      fail("Should throw exception");
    } catch (GenException e) {
      // OK
    }
  }

  @Test
  public void testInvalidParams4() throws Exception {
    try {
      generateProxyModel(InvalidParams4.class);
      fail("Should throw exception");
    } catch (GenException e) {
      // OK
    }
  }

  @Test
  public void testInvalidParamsDataObject() throws Exception {
    try {
      generateProxyModel(InvalidParamsDataObject.class);
      fail("Should throw exception");
    } catch (GenException e) {
      // OK
    }
  }

  @Test
  public void testInvalidReturn1() throws Exception {
    try {
      generateProxyModel(InvalidReturn1.class);
      fail("Should throw exception");
    } catch (GenException e) {
      // OK
    }
  }

  @Test
  public void testInvalidReturn2() throws Exception {
    try {
      generateProxyModel(InvalidReturn2.class);
      fail("Should throw exception");
    } catch (GenException e) {
      // OK
    }
  }

  @Test
  public void testInvalidReturn3() throws Exception {
    try {
      generateProxyModel(InvalidReturn3.class);
      fail("Should throw exception");
    } catch (GenException e) {
      // OK
    }
  }

  @Test
  public void testValid() throws Exception {
    ProxyModel model = generateProxyModel(ValidProxy.class);
    assertEquals(ValidProxy.class.getName(), model.getIfaceFQCN());
    assertEquals(ValidProxy.class.getSimpleName(), model.getIfaceSimpleName());
    assertTrue(model.getSuperTypes().isEmpty());
    assertEquals(51, model.getMethods().size());

    // Not going to check all the types are correct as this is already tested in the VertxGen tests
    // but we do want to check the proxyIgnore flag is correctly set
    for (MethodInfo mi: model.getMethods()) {
      ProxyMethodInfo pmi = (ProxyMethodInfo)mi;
      if (pmi.getName().equals("ignored")) {
        assertTrue(pmi.isProxyIgnore());
      } else {
        assertFalse(pmi.isProxyIgnore());
      }
      if (pmi.getName().equals("closeIt")) {
        assertTrue(pmi.isProxyClose());
      } else {
        assertFalse(pmi.isProxyClose());
      }
    }
  }

  @Test
  public void testValidCloseWithFuture() throws Exception {
    ProxyModel model = generateProxyModel(ValidProxyCloseWithFuture.class);
    assertEquals(1, model.getMethods().size());
    assertEquals(MethodKind.CALLBACK, model.getMethods().get(0).getKind());
    ParameterizedTypeInfo handlerType = (ParameterizedTypeInfo) model.getMethods().get(0).getParams().get(0).getType();
    ParameterizedTypeInfo asyncResultType = (ParameterizedTypeInfo) handlerType.getArgs().get(0);
    assertEquals(ClassKind.VOID, asyncResultType.getArgs().get(0).getKind());
  }

  @Test
  public void testInvalidClose1() throws Exception {
    try {
      generateProxyModel(InvalidClose1.class);
      fail("Should throw exception");
    } catch (GenException e) {
      // OK
    }
  }

  @Test
  public void testInvalidClose2() throws Exception {
    try {
      generateProxyModel(InvalidClose2.class);
      fail("Should throw exception");
    } catch (GenException e) {
      // OK
    }
  }

  @Test
  public void testInvalidClose3() throws Exception {
    try {
      generateProxyModel(InvalidClose3.class);
      fail("Should throw exception");
    } catch (GenException e) {
      // OK
    }
  }

  @Test
  public void testValidFuture() throws Exception {
    ProxyModel model = generateProxyModel(FuturizedProxy.class);
  }
}
