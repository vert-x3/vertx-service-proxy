package io.vertx.serviceproxy.clustered;

import com.jayway.awaitility.Awaitility;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.serviceproxy.testmodel.SomeEnum;
import io.vertx.serviceproxy.testmodel.TestDataObject;
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
    Vertx.clusteredVertx(new VertxOptions().setClustered(true).setClusterHost("127.0.0.1"), ar -> {
      Vertx vertx = ar.result();
      providerNode.set(vertx);
      vertx.deployVerticle(ServiceProviderVerticle.class.getName());
    });

    Vertx.clusteredVertx(new VertxOptions().setClustered(true).setClusterHost("127.0.0.1"), ar -> {
      Vertx vertx = ar.result();
      consumerNode.set(vertx);
    });

    Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> providerNode.get() != null);
    Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> consumerNode.get() != null);
  }


  @Test
  public void testHello() {
    AtomicReference<String> result = new AtomicReference<>();
    Service service = Service.createProxy(consumerNode.get(), "my.service");
    service.hello("vert.x", ar -> {
      result.set(ar.result());
    });

    Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() ->
        "hello vert.x".equalsIgnoreCase(result.get()));
  }

  @Test
  public void testEnumAsParameter() {
    AtomicReference<Boolean> result = new AtomicReference<>();
    Service service = Service.createProxy(consumerNode.get(), "my.service");
    service.methodUsingEnum(SomeEnum.WIBBLE, ar -> {
      result.set(ar.result());
    });

    Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> result.get() != null && result.get());
  }

  @Test
  public void testEnumAsResult() {
    AtomicReference<SomeEnum> result = new AtomicReference<>();
    Service service = Service.createProxy(consumerNode.get(), "my.service");
    service.methodReturningEnum(ar -> {
      result.set(ar.result());
    });

    Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> result.get() == SomeEnum.WIBBLE);
  }

  @Test
  public void testWithDataObject() {
    AtomicReference<TestDataObject> result = new AtomicReference<>();
    Service service = Service.createProxy(consumerNode.get(), "my.service");
    TestDataObject data = new TestDataObject().setBool(true).setNumber(25).setString("vert.x");
    service.methodWithDataObject(data, ar -> {
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
    service.methodWithListOfDataObject(Arrays.asList(data, data2),ar -> {
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
  public void testWithJsonObject() {
    AtomicReference<TestDataObject> result = new AtomicReference<>();
    Service service = Service.createProxy(consumerNode.get(), "my.service");
    TestDataObject data = new TestDataObject().setBool(true).setNumber(25).setString("vert.x");
    service.methodWithJsonObject(data.toJson(), ar -> {
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

    service.methodWithJsonArray(array, ar -> {
      result.set(ar.result());
    });

    Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> result.get() != null);
    TestDataObject out = new TestDataObject(result.get().getJsonObject(1));
    assertThat(array.getString(0)).isEqualToIgnoringCase("vert.x");
    assertThat(out.getNumber()).isEqualTo(25);
    assertThat(out.isBool()).isTrue();
    assertThat(out.getString()).isEqualTo("vert.x");
  }
}
