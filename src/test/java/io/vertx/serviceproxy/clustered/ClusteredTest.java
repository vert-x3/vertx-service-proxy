package io.vertx.serviceproxy.clustered;

import com.jayway.awaitility.Awaitility;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.serviceproxy.testmodel.SomeEnum;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

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
}
