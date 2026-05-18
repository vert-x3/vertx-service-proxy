package io.vertx.serviceproxy.tests.grpcinterop;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.grpc.client.InvalidStatusException;
import io.vertx.grpc.common.GrpcStatus;
import io.vertx.grpc.common.WireFormat;
import io.vertx.grpc.eventbus.EventBusGrpcClient;
import io.vertx.grpc.eventbus.EventBusGrpcServer;
import io.vertx.serviceproxy.ServiceBinder;
import io.vertx.serviceproxy.ServiceException;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

@RunWith(VertxUnitRunner.class)
public class EventBusServiceProxyInteropTest {

  private static final String ADDRESS = "proxyinterop.Greeter";

  protected Vertx vertx;

  @Before
  public void setUp() {
    vertx = Vertx.vertx();
  }

  @After
  public void tearDown(TestContext should) {
    vertx.close().onComplete(should.asyncAssertSuccess());
  }

  @Test
  public void testProxyClientCallsGrpcServer() throws Exception {
    EventBusGrpcServer server = EventBusGrpcServer.server(vertx);
    server.addService(GreeterGrpcService.of(new GreeterService() {
      @Override
      public Future<HelloReply> sayHello(HelloRequest request) {
        return Future.succeededFuture(HelloReply.newBuilder()
          .setMessage("Hello " + request.getName())
          .build());
      }

      @Override
      public Future<HelloReply> sayHi(HelloRequest request) {
        return Future.succeededFuture(HelloReply.newBuilder()
          .setMessage("Hi " + request.getName())
          .build());
      }
    }));

    GreeterProxy proxy = new ServiceProxyBuilder(vertx)
      .setAddress(ADDRESS)
      .setOptions(new DeliveryOptions().addHeader("grpc-wire-format", WireFormat.JSON.name()))
      .build(GreeterProxy.class);

    HelloJavaReply reply = proxy.SayHello("Julien").await(10, TimeUnit.SECONDS);
    assertEquals("Hello Julien", reply.getMessage());

    HelloJavaReply lowerReply = proxy.sayHi("Julien").await(10, TimeUnit.SECONDS);
    assertEquals("Hi Julien", lowerReply.getMessage());
  }

  @Test
  public void testGrpcClientCallsProxyServer() throws Exception {
    GreeterProxy impl = new GreeterProxy() {
      @Override
      public Future<HelloJavaReply> SayHello(String name) {
        return Future.succeededFuture(new HelloJavaReply().setMessage("Hello " + name));
      }

      @Override
      public Future<HelloJavaReply> sayHi(String name) {
        return Future.succeededFuture(new HelloJavaReply().setMessage("Hi " + name));
      }
    };
    MessageConsumer<JsonObject> consumer = new ServiceBinder(vertx)
      .setAddress(ADDRESS)
      .register(GreeterProxy.class, impl);

    try {
      EventBusGrpcClient client = EventBusGrpcClient.client(vertx);
      GreeterClient greeter = GreeterGrpcClient.create(client, WireFormat.JSON);

      HelloReply reply = greeter
        .sayHello(HelloRequest.newBuilder().setName("Julien").build())
        .await(10, TimeUnit.SECONDS);
      assertEquals("Hello Julien", reply.getMessage());

      HelloReply lowerReply = greeter
        .sayHi(HelloRequest.newBuilder().setName("Julien").build())
        .await(10, TimeUnit.SECONDS);
      assertEquals("Hi Julien", lowerReply.getMessage());
    } finally {
      consumer.unregister().await(10, TimeUnit.SECONDS);
    }
  }

  @Test
  public void testProxyClientReceivesGrpcServerFailure() {
    EventBusGrpcServer server = EventBusGrpcServer.server(vertx);
    server.addService(GreeterGrpcService.of(new GreeterService() {
      @Override
      public Future<HelloReply> sayHello(HelloRequest request) {
        return Future.failedFuture(new RuntimeException("boom"));
      }
    }));

    GreeterProxy proxy = new ServiceProxyBuilder(vertx)
      .setAddress(ADDRESS)
      .setOptions(new DeliveryOptions().addHeader("grpc-wire-format", WireFormat.JSON.name()))
      .build(GreeterProxy.class);

    ReplyException expected = assertThrows(ReplyException.class, () -> proxy.SayHello("Julien").await(10, TimeUnit.SECONDS));
    assertEquals(GrpcStatus.UNKNOWN.code, expected.failureCode());
  }

  @Test
  public void testGrpcClientReceivesProxyServerServiceException() throws Exception {
    GreeterProxy impl = new GreeterProxy() {
      @Override
      public Future<HelloJavaReply> SayHello(String name) {
        return Future.failedFuture(new ServiceException(GrpcStatus.NOT_FOUND.code, "nope"));
      }

      @Override
      public Future<HelloJavaReply> sayHi(String name) {
        return Future.failedFuture(new ServiceException(GrpcStatus.NOT_FOUND.code, "nope"));
      }
    };
    MessageConsumer<JsonObject> consumer = new ServiceBinder(vertx)
      .setAddress(ADDRESS)
      .register(GreeterProxy.class, impl);

    try {
      EventBusGrpcClient client = EventBusGrpcClient.client(vertx);
      GreeterClient greeter = GreeterGrpcClient.create(client, WireFormat.JSON);

      InvalidStatusException expected = assertThrows(
        InvalidStatusException.class, () -> greeter.sayHello(HelloRequest.newBuilder().setName("Julien").build()).await(10, TimeUnit.SECONDS)
      );
      assertEquals(GrpcStatus.NOT_FOUND, expected.actualStatus());
    } finally {
      consumer.unregister().await(10, TimeUnit.SECONDS);
    }
  }

  @Test
  public void testGrpcClientReceivesProxyServerRuntimeFailureAsInternal() throws Exception {
    GreeterProxy impl = new GreeterProxy() {
      @Override
      public Future<HelloJavaReply> SayHello(String name) {
        return Future.failedFuture(new RuntimeException("kaboom"));
      }

      @Override
      public Future<HelloJavaReply> sayHi(String name) {
        return Future.failedFuture(new RuntimeException("kaboom"));
      }
    };
    MessageConsumer<JsonObject> consumer = new ServiceBinder(vertx)
      .setAddress(ADDRESS)
      .register(GreeterProxy.class, impl);

    try {
      EventBusGrpcClient client = EventBusGrpcClient.client(vertx);
      GreeterClient greeter = GreeterGrpcClient.create(client, WireFormat.JSON);

      InvalidStatusException expected = assertThrows(
        InvalidStatusException.class, () -> greeter.sayHello(HelloRequest.newBuilder().setName("Julien").build()).await(10, TimeUnit.SECONDS)
      );
      assertEquals(GrpcStatus.INTERNAL, expected.actualStatus());
    } finally {
      consumer.unregister().await(10, TimeUnit.SECONDS);
    }
  }

  @Test
  public void testProxyClientWithoutWireFormatHeaderFails() {
    EventBusGrpcServer server = EventBusGrpcServer.server(vertx);
    server.addService(GreeterGrpcService.of(new GreeterService() {
      @Override
      public Future<HelloReply> sayHello(HelloRequest request) {
        return Future.succeededFuture(HelloReply.newBuilder()
          .setMessage("Hello " + request.getName())
          .build());
      }
    }));

    GreeterProxy proxy = new ServiceProxyBuilder(vertx)
      .setAddress(ADDRESS)
      .build(GreeterProxy.class);

    ReplyException expected = assertThrows(ReplyException.class, () -> proxy.SayHello("Julien").await(10, TimeUnit.SECONDS));
    assertEquals(GrpcStatus.INVALID_ARGUMENT.code, expected.failureCode());
    assertNotNull(expected.getMessage());
    assertTrue("expected message to mention grpc-wire-format, got: " + expected.getMessage(), expected.getMessage().contains("grpc-wire-format"));
  }
}
