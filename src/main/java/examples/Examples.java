package examples;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceProxyFactory;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class Examples {

  public void example1(Vertx vertx) {
    // Assume database service is already deployed somewhere....
    // Save some data in the database
    JsonObject message = new JsonObject();
    message.put("collection", "mycollection")
        .put("document", new JsonObject().put("name", "tim"));
    DeliveryOptions options = new DeliveryOptions().addHeader("action", "save");
    vertx.eventBus().send("database-service-address", message, options, res2 -> {
      if (res2.succeeded()) {
        // done
      } else {
        // failure
      }
    });
  }

  public void example2(Vertx vertx) {
    // Assume database service is already deployed somewhere....

    // Create a proxy
    SomeDatabaseService service = SomeDatabaseService.createProxy(vertx,
        "database-service-address");

    // Save some data in the database - this time using the proxy
    service.save("mycollection", new JsonObject().put("name", "tim"), res2 -> {
      if (res2.succeeded()) {
        // done
      }
    });
  }

  public void register(Vertx vertx) {
    // Create an instance of your service implementation
    SomeDatabaseService service = new SomeDatabaseServiceImpl();
    // Register the handler
    new ServiceProxyFactory(vertx)
      .setAddress("database-service-address")
      .register(SomeDatabaseService.class, service);
  }

  public void unregister(Vertx vertx) {
    ServiceProxyFactory factory = new ServiceProxyFactory(vertx);

    // Create an instance of your service implementation
    SomeDatabaseService service = new SomeDatabaseServiceImpl();
    // Register the handler
    MessageConsumer<JsonObject> consumer = factory
      .setAddress("database-service-address")
      .register(SomeDatabaseService.class, service);

    // ....

    // Unregister your service.
    factory.unregister(consumer);
  }

  public void proxyCreation(Vertx vertx, DeliveryOptions options) {
    ServiceProxyFactory factory = new ServiceProxyFactory(vertx).setAddress("database-service-address");

    SomeDatabaseService service = factory.createProxy(SomeDatabaseService.class);
    // or with delivery options:
    SomeDatabaseService service2 = factory.setOptions(options).createProxy(SomeDatabaseService.class);
  }
}
