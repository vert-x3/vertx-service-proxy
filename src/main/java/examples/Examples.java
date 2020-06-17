package examples;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.authorization.PermissionBasedAuthorization;
import io.vertx.ext.auth.authorization.RoleBasedAuthorization;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.auth.jwt.authorization.JWTAuthorization;
import io.vertx.serviceproxy.ServiceAuthInterceptor;
import io.vertx.serviceproxy.ServiceBinder;
import io.vertx.serviceproxy.ServiceProxyBuilder;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class Examples {

  public void example1(Vertx vertx) {
    // Assume database service is already deployed somewhere....
    // Save some data in the database
    JsonObject message = new JsonObject();

    message
      .put("collection", "mycollection")
      .put("document", new JsonObject().put("name", "tim"));

    DeliveryOptions options = new DeliveryOptions().addHeader("action", "save");

    vertx.eventBus()
      .request("database-service-address", message, options)
      .onSuccess(msg -> {
        // done
      }).onFailure(err -> {
      // failure
    });
  }

  public void example2(Vertx vertx) {
    // Assume database service is already deployed somewhere....

    // Create a proxy
    SomeDatabaseService service = SomeDatabaseService
      .createProxy(vertx, "database-service-address");

    // Save some data in the database - this time using the proxy
    service.save(
      "mycollection",
      new JsonObject().put("name", "tim"),
      res2 -> {
        if (res2.succeeded()) {
          // done
        }
      });
  }

  public void register(Vertx vertx) {
    // Create an instance of your service implementation
    SomeDatabaseService service = new SomeDatabaseServiceImpl();
    // Register the handler
    new ServiceBinder(vertx)
      .setAddress("database-service-address")
      .register(SomeDatabaseService.class, service);
  }

  public void unregister(Vertx vertx) {
    ServiceBinder binder = new ServiceBinder(vertx);

    // Create an instance of your service implementation
    SomeDatabaseService service = new SomeDatabaseServiceImpl();
    // Register the handler
    MessageConsumer<JsonObject> consumer = binder
      .setAddress("database-service-address")
      .register(SomeDatabaseService.class, service);

    // ....

    // Unregister your service.
    binder.unregister(consumer);
  }

  public void proxyCreation(Vertx vertx, DeliveryOptions options) {
    ServiceProxyBuilder builder = new ServiceProxyBuilder(vertx)
      .setAddress("database-service-address");

    SomeDatabaseService service = builder.build(SomeDatabaseService.class);
    // or with delivery options:
    SomeDatabaseService service2 = builder.setOptions(options)
      .build(SomeDatabaseService.class);
  }

  public void secure(Vertx vertx) {
    // Create an instance of your service implementation
    SomeDatabaseService service = new SomeDatabaseServiceImpl();
    // Register the handler
    new ServiceBinder(vertx)
      .setAddress("database-service-address")
      // Secure the messages in transit
      .addInterceptor(
        new ServiceAuthInterceptor()
          // Tokens will be validated using JWT authentication
          .setAuthenticationProvider(JWTAuth.create(vertx, new JWTAuthOptions()))
          // optionally we can secure permissions too:

          // an admin
          .addAuthorization(RoleBasedAuthorization.create("admin"))
          // that can print
          .addAuthorization(PermissionBasedAuthorization.create("print"))

          // where the authorizations are loaded, let's assume from the token
          // but they could be loaded from a database or a file if needed
          .setAuthorizationProvider(
            JWTAuthorization.create("permissions")))

      .register(SomeDatabaseService.class, service);
  }
}
