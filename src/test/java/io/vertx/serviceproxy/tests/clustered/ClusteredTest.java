package io.vertx.serviceproxy.tests.clustered;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceException;
import io.vertx.serviceproxy.tests.testmodel.*;
import io.vertx.test.fakecluster.FakeClusterManager;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class ClusteredTest {

  private Vertx producerNode;
  private Vertx consumerNode;

  @Before
  public void setUp() throws Exception {
    VertxOptions options = new VertxOptions();
    options.getEventBusOptions().setHost("127.0.0.1");
    producerNode = Vertx.builder()
            .with(options)
            .withClusterManager(new FakeClusterManager())
            .buildClustered()
            .await(20, TimeUnit.SECONDS);
    producerNode.eventBus().registerDefaultCodec(MyServiceException.class, new MyServiceExceptionMessageCodec());
    producerNode.deployVerticle(ServiceProviderVerticle.class.getName());
    producerNode.deployVerticle(LocalServiceProviderVerticle.class.getName());
    consumerNode = Vertx.builder()
            .with(options)
            .withClusterManager(new FakeClusterManager())
            .buildClustered()
            .await(20, TimeUnit.SECONDS);
    consumerNode.eventBus().registerDefaultCodec(MyServiceException.class, new MyServiceExceptionMessageCodec());
  }


  @Test
  public void testHello() throws Exception {
    Service service = Service.createProxy(consumerNode, "my.service");
    String res = service.hello("vert.x").await(20, TimeUnit.SECONDS);
    assertEquals("hello vert.x", res);
  }

  @Test
  public void testEnumAsParameter() throws Exception {
    Service service = Service.createProxy(consumerNode, "my.service");
    Boolean res = service.methodUsingEnum(SomeEnum.WIBBLE).await(20, TimeUnit.SECONDS);
    assertNotNull(res);
    assertTrue(res);
  }

  @Test
  public void testEnumAsResult() throws Exception {
    AtomicReference<SomeEnum> result = new AtomicReference<>();
    Service service = Service.createProxy(consumerNode, "my.service");
    SomeEnum res = service.methodReturningEnum().await(20, TimeUnit.SECONDS);
    assertEquals(SomeEnum.WIBBLE, res);
  }

  @Test
  public void testVertxEnumAsResult() throws Exception {
    Service service = Service.createProxy(consumerNode, "my.service");
    SomeVertxEnum res = service.methodReturningVertxEnum().await(20, TimeUnit.SECONDS);
    assertEquals(SomeVertxEnum.BAR, res);
  }

  @Test
  public void testWithDataObject() throws Exception {
    Service service = Service.createProxy(consumerNode, "my.service");
    TestDataObject data = new TestDataObject().setBool(true).setNumber(25).setString("vert.x");
    TestDataObject out = service.methodWithDataObject(data).await(20, TimeUnit.SECONDS);
    assertThat(out.getNumber()).isEqualTo(25);
    assertThat(out.isBool()).isTrue();
    assertThat(out.getString()).isEqualTo("vert.x");
  }

  @Test
  public void testWithListOfDataObject() throws Exception {
    Service service = Service.createProxy(consumerNode, "my.service");
    TestDataObject data = new TestDataObject().setBool(true).setNumber(25).setString("vert.x");
    TestDataObject data2 = new TestDataObject().setBool(true).setNumber(26).setString("vert.x");
    List<TestDataObject> out = service.methodWithListOfDataObject(Arrays.asList(data, data2)).await(20, TimeUnit.SECONDS);
    assertThat(out.get(0).getNumber()).isEqualTo(25);
    assertThat(out.get(0).isBool()).isTrue();
    assertThat(out.get(0).getString()).isEqualTo("vert.x");
    assertThat(out.get(1).getNumber()).isEqualTo(26);
    assertThat(out.get(1).isBool()).isTrue();
    assertThat(out.get(1).getString()).isEqualTo("vert.x");
  }

  @Test
  public void testWithStringDataObject() throws Exception {
    Service service = Service.createProxy(consumerNode, "my.service");
    StringDataObject data = new StringDataObject().setValue("vert.x");
    StringDataObject out = service.methodWithStringDataObject(data).await(20, TimeUnit.SECONDS);
    assertThat(out.getValue()).isEqualTo("vert.x");
  }

  @Test
  public void testWithListOfStringDataObject() throws Exception {
    Service service = Service.createProxy(consumerNode, "my.service");
    StringDataObject data = new StringDataObject().setValue("vert.x-1");
    StringDataObject data2 = new StringDataObject().setValue("vert.x-2");
    List<StringDataObject> out = service.methodWithListOfStringDataObject(Arrays.asList(data, data2)).await(20, TimeUnit.SECONDS);
    assertThat(out.get(0).getValue()).isEqualTo("vert.x-1");
    assertThat(out.get(1).getValue()).isEqualTo("vert.x-2");
  }

  @Test
  public void testWithListOfJsonObject() throws Exception {
    Service service = Service.createProxy(consumerNode, "my.service");
    TestDataObject data = new TestDataObject().setBool(true).setNumber(25).setString("vert.x");
    TestDataObject data2 = new TestDataObject().setBool(true).setNumber(26).setString("vert.x");
    List<JsonObject> out = service.methodWithListOfJsonObject(Arrays.asList(data.toJson(), data2.toJson())).await(20, TimeUnit.SECONDS);
    TestDataObject out0 = new TestDataObject(out.get(0));
    TestDataObject out1 = new TestDataObject(out.get(1));
    assertThat(out0.getNumber()).isEqualTo(25);
    assertThat(out0.isBool()).isTrue();
    assertThat(out0.getString()).isEqualTo("vert.x");
    assertThat(out1.getNumber()).isEqualTo(26);
    assertThat(out1.isBool()).isTrue();
    assertThat(out1.getString()).isEqualTo("vert.x");
  }

  @Test
  public void testWithJsonObject() throws Exception {
    Service service = Service.createProxy(consumerNode, "my.service");
    TestDataObject data = new TestDataObject().setBool(true).setNumber(25).setString("vert.x");
    TestDataObject out = new TestDataObject(service.methodWithJsonObject(data.toJson()).await(20, TimeUnit.SECONDS));
    assertThat(out.getNumber()).isEqualTo(25);
    assertThat(out.isBool()).isTrue();
    assertThat(out.getString()).isEqualTo("vert.x");
  }

  @Test
  public void testWithJsonArray() throws Exception {
    Service service = Service.createProxy(consumerNode, "my.service");
    TestDataObject data = new TestDataObject().setBool(true).setNumber(25).setString("vert.x");
    JsonArray array = new JsonArray();
    array.add("vert.x").add(data.toJson());
    TestDataObject out = new TestDataObject(service.methodWithJsonArray(array).await(20, TimeUnit.SECONDS).getJsonObject(1));
    assertThat(array.getString(0)).isEqualToIgnoringCase("vert.x");
    assertThat(out.getNumber()).isEqualTo(25);
    assertThat(out.isBool()).isTrue();
    assertThat(out.getString()).isEqualTo("vert.x");
  }

  @Test
  public void testWithFailingResult() throws Exception {
    Service service = Service.createProxy(consumerNode, "my.service");
    try {
      service.methodWthFailingResult("Fail").await(20, TimeUnit.SECONDS);
      fail();
    } catch (ServiceException cause) {
      assertThat(cause.failureCode()).isEqualTo(30);
      assertThat(cause.getDebugInfo()).isEqualTo(new JsonObject().put("test", "val"));
    }
  }

  @Test
  public void testWithFailingResultServiceExceptionSubclass() throws Exception {
    Service service = Service.createProxy(consumerNode, "my.service");
    try {
      service.methodWthFailingResult("cluster Fail").await(20, TimeUnit.SECONDS);
      fail();
    } catch (MyServiceException cause) {
      assertThat(cause.failureCode()).isEqualTo(30);
      assertThat(cause.getExtra()).isEqualTo("some extra");
    }
  }

  @Test
  public void testLocalServiceShouldBeUnreachable() throws Exception {
    Service service = Service.createProxy(consumerNode, "my.local.service");
    try {
      service.hello("vert.x").await(20, TimeUnit.SECONDS);
    } catch (ReplyException cause) {
      assertThat(cause.failureType()).isEqualTo(ReplyFailure.NO_HANDLERS);
    }
  }
}
