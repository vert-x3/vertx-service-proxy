package io.vertx.serviceproxy.clustered;

import com.jayway.awaitility.Awaitility;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceException;
import io.vertx.serviceproxy.testmodel.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class ClusteredTest {

  AtomicReference<Vertx> providerNode = new AtomicReference<>();
  AtomicReference<Vertx> consumerNode = new AtomicReference<>();

  @Before
  public void setUp() {
    VertxOptions options = new VertxOptions();
    options.getEventBusOptions().setHost("127.0.0.1");
    Vertx.clusteredVertx(options)
      .onSuccess(vertx -> {
        vertx.eventBus().registerDefaultCodec(MyServiceException.class,
          new MyServiceExceptionMessageCodec());
        providerNode.set(vertx);
        vertx.deployVerticle(ServiceProviderVerticle.class.getName());
        vertx.deployVerticle(LocalServiceProviderVerticle.class.getName());
      });

    Vertx.clusteredVertx(options)
      .onSuccess(vertx -> {
        vertx.eventBus().registerDefaultCodec(MyServiceException.class,
          new MyServiceExceptionMessageCodec());
        consumerNode.set(vertx);
      });

    Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> providerNode.get() != null);
    Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> consumerNode.get() != null);
  }


  @Test
  public void testHello() {
    AtomicReference<String> result = new AtomicReference<>();
    Service service = Service.createProxy(consumerNode.get(), "my.service");
    service.hello("vert.x").onComplete(ar -> {
      result.set(ar.result());
    });

    Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() ->
      "hello vert.x".equalsIgnoreCase(result.get()));
  }

  @Test
  public void testEnumAsParameter() {
    AtomicReference<Boolean> result = new AtomicReference<>();
    Service service = Service.createProxy(consumerNode.get(), "my.service");
    service.methodUsingEnum(SomeEnum.WIBBLE).onComplete(ar -> {
      result.set(ar.result());
    });

    Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> result.get() != null && result.get());
  }

  @Test
  public void testEnumAsResult() {
    AtomicReference<SomeEnum> result = new AtomicReference<>();
    Service service = Service.createProxy(consumerNode.get(), "my.service");
    service.methodReturningEnum().onComplete(ar -> {
      result.set(ar.result());
    });

    Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> result.get() == SomeEnum.WIBBLE);
  }

  @Test
  public void testVertxEnumAsResult() {
    AtomicReference<SomeVertxEnum> result = new AtomicReference<>();
    Service service = Service.createProxy(consumerNode.get(), "my.service");
    service.methodReturningVertxEnum().onComplete(ar -> {
      result.set(ar.result());
    });

    Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> result.get() == SomeVertxEnum.BAR);
  }

  @Test
  public void testWithDataObject() {
    AtomicReference<TestDataObject> result = new AtomicReference<>();
    Service service = Service.createProxy(consumerNode.get(), "my.service");
    TestDataObject data = new TestDataObject().setBool(true).setNumber(25).setString("vert.x");
    service.methodWithDataObject(data).onComplete(ar -> {
      result.set(ar.result());
    });

    Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> result.get() != null);
    TestDataObject out = result.get();
    assertThat(out.getNumber()).isEqualTo(25);
    assertThat(out.isBool()).isTrue();
    assertThat(out.getString()).isEqualTo("vert.x");
  }

  @Test
  public void testWithListOfDataObject() {
    AtomicReference<List<TestDataObject>> result = new AtomicReference<>();
    Service service = Service.createProxy(consumerNode.get(), "my.service");
    TestDataObject data = new TestDataObject().setBool(true).setNumber(25).setString("vert.x");
    TestDataObject data2 = new TestDataObject().setBool(true).setNumber(26).setString("vert.x");
    service.methodWithListOfDataObject(Arrays.asList(data, data2)).onComplete(ar -> {
      if (ar.failed()) {
        ar.cause().printStackTrace();
      }
      result.set(ar.result());
    });

    Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> result.get() != null);
    List<TestDataObject> out = result.get();
    assertThat(out.get(0).getNumber()).isEqualTo(25);
    assertThat(out.get(0).isBool()).isTrue();
    assertThat(out.get(0).getString()).isEqualTo("vert.x");
    assertThat(out.get(1).getNumber()).isEqualTo(26);
    assertThat(out.get(1).isBool()).isTrue();
    assertThat(out.get(1).getString()).isEqualTo("vert.x");
  }

  @Test
  public void testWithStringDataObject() {
    AtomicReference<StringDataObject> result = new AtomicReference<>();
    Service service = Service.createProxy(consumerNode.get(), "my.service");
    StringDataObject data = new StringDataObject().setValue("vert.x");
    service.methodWithStringDataObject(data).onComplete(ar -> {
      result.set(ar.result());
    });

    Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> result.get() != null);
    StringDataObject out = result.get();
    assertThat(out.getValue()).isEqualTo("vert.x");
  }

  @Test
  public void testWithListOfStringDataObject() {
    AtomicReference<List<StringDataObject>> result = new AtomicReference<>();
    Service service = Service.createProxy(consumerNode.get(), "my.service");
    StringDataObject data = new StringDataObject().setValue("vert.x-1");
    StringDataObject data2 = new StringDataObject().setValue("vert.x-2");
    service.methodWithListOfStringDataObject(Arrays.asList(data, data2)).onComplete(ar -> {
      if (ar.failed()) {
        ar.cause().printStackTrace();
      }
      result.set(ar.result());
    });

    Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> result.get() != null);
    List<StringDataObject> out = result.get();
    assertThat(out.get(0).getValue()).isEqualTo("vert.x-1");
    assertThat(out.get(1).getValue()).isEqualTo("vert.x-2");
  }

  @Test
  public void testWithListOfJsonObject() {
    AtomicReference<List<JsonObject>> result = new AtomicReference<>();
    Service service = Service.createProxy(consumerNode.get(), "my.service");
    TestDataObject data = new TestDataObject().setBool(true).setNumber(25).setString("vert.x");
    TestDataObject data2 = new TestDataObject().setBool(true).setNumber(26).setString("vert.x");
    service.methodWithListOfJsonObject(Arrays.asList(data.toJson(), data2.toJson())).onComplete(ar -> {
      if (ar.failed()) {
        ar.cause().printStackTrace();
      }
      result.set(ar.result());
    });

    Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> result.get() != null);
    List<JsonObject> out = result.get();

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
  public void testWithJsonObject() {
    AtomicReference<TestDataObject> result = new AtomicReference<>();
    Service service = Service.createProxy(consumerNode.get(), "my.service");
    TestDataObject data = new TestDataObject().setBool(true).setNumber(25).setString("vert.x");
    service.methodWithJsonObject(data.toJson()).onComplete(ar -> {
      result.set(new TestDataObject(ar.result()));
    });

    Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> result.get() != null);
    TestDataObject out = result.get();
    assertThat(out.getNumber()).isEqualTo(25);
    assertThat(out.isBool()).isTrue();
    assertThat(out.getString()).isEqualTo("vert.x");
  }

  @Test
  public void testWithJsonArray() {
    AtomicReference<JsonArray> result = new AtomicReference<>();
    Service service = Service.createProxy(consumerNode.get(), "my.service");
    TestDataObject data = new TestDataObject().setBool(true).setNumber(25).setString("vert.x");
    JsonArray array = new JsonArray();
    array.add("vert.x").add(data.toJson());

    service.methodWithJsonArray(array).onComplete(ar -> {
      result.set(ar.result());
    });

    Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> result.get() != null);
    TestDataObject out = new TestDataObject(result.get().getJsonObject(1));
    assertThat(array.getString(0)).isEqualToIgnoringCase("vert.x");
    assertThat(out.getNumber()).isEqualTo(25);
    assertThat(out.isBool()).isTrue();
    assertThat(out.getString()).isEqualTo("vert.x");
  }

  @Test
  public void testWithFailingResult() {
    AtomicReference<Throwable> result = new AtomicReference<>();
    Service service = Service.createProxy(consumerNode.get(), "my.service");
    service.methodWthFailingResult("Fail").onComplete(ar -> {
      assertThat(ar.cause() instanceof ServiceException).isTrue();
      assertThat(((ServiceException) ar.cause()).failureCode()).isEqualTo(30);
      assertThat(((ServiceException) ar.cause()).getDebugInfo()).isEqualTo(new JsonObject().put("test", "val"));
      result.set(ar.cause());
    });
    Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> result.get() != null);
  }

  @Test
  public void testWithFailingResultServiceExceptionSubclass() {
    AtomicReference<Throwable> result = new AtomicReference<>();
    Service service = Service.createProxy(consumerNode.get(), "my.service");
    service.methodWthFailingResult("cluster Fail").onComplete(ar -> {
      assertThat(ar.cause() instanceof MyServiceException).isTrue();
      assertThat(((MyServiceException) ar.cause()).failureCode()).isEqualTo(30);
      assertThat(((MyServiceException) ar.cause()).getExtra()).isEqualTo("some extra");
      result.set(ar.cause());
    });
    Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> result.get() != null);
  }

  @Test
  public void testLocalServiceShouldBeUnreachable() {
    AtomicReference<Throwable> result = new AtomicReference<>();
    Service service = Service.createProxy(consumerNode.get(), "my.local.service");
    service.hello("vert.x").onComplete((ar) -> {
      assertThat(ar.succeeded()).isFalse().withFailMessage("Local service should not be accessible from a different node in the cluster");
      assertThat(ar.cause()).isNotNull();
      assertThat(ar.cause()).isInstanceOf(ReplyException.class);
      ReplyException exception = (ReplyException) ar.cause();
      assertThat(exception.failureType()).isEqualTo(ReplyFailure.NO_HANDLERS);
      result.set(ar.cause());
    });
    Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> result.get() != null);
  }
}
