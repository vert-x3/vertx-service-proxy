# Service Proxies

[![Build Status](https://vertx.ci.cloudbees.com/buildStatus/icon?job=vert.x3-service-proxy)](https://vertx.ci.cloudbees.com/view/vert.x-3/job/vert.x3-service-proxy/)

Please see the main documentation on the web-site for a full description:

* [Web-site documentation](https://github.com/vert-x3/vertx-service-proxy/blob/master/src/main/asciidoc/java/index.adoc)

Many Vert.x applications include various services which do useful things and often can be reused from one application
to another. An example would be a database service.

Usually those services run in their own verticle and interact with other verticles by receiving and sending messages, e.g.

    // Assume database service is already deployed somewhere....
    
    // Save some data in the database
    
    JsonObject message = new JsonObject();
    message.putString("collection", "mycollection");
    message.putObject("document", new JsonObject().putString("name", "tim"));
    DeliveryOptions options = new DeliveryOptions().addHeader("action", "save");
    vertx.eventBus().send("database-service-address", message, options, res2 -> {
        if (res2.succeeded()) {
            // done
        }
    }
    

When creating a service there's a certain amount of boiler-plate code to listen on the eventbus for incoming messages,
route them to the appropriate method and return results on the event bus.

With Vert.x service proxies, you can avoid writing all that boiler-plate code and concentrate on writing your service.

You write your service as a Java interface and annotate it with the `@ProxyGen` annotation, for example:

    @ProxyGen
    public interface SomeDatabaseService {
    
        // A couple of factory methods to create an instance and a proxy
    
        static SomeDatabaseService create(Vertx vertx) {
           return new SomeDatabaseServiceImpl(vertx);
        }
        
        static SomeDatabaseService createProxy(Vertx vertx, String address) {
          return new SomeDatabaseServiceVertxEBProxy(vertx, address);
        }
        
        // Actual service operations here...
    
        void save(String collection, JsonObject document, Handler<AsyncResult<Void>> resultHandler);
    }
    
Given the interface, Vert.x will generate all the boiler-plate required to access your service over the event bus, and it
will also generate a *client side proxy* for your service, so your clients can use a rich idiomatic API for your
service instead of having to manually craft event bus messages to send. The client side proxy will work irrespective
of where your service actually lives on the event bus (potentially on a different machine).

That means you can interact with your service like this:

    // Assume database service is already deployed somewhere....
        
    // Create a proxy
    SomeDatabaseService service = SomeDatabaseService.createProxy(vertx, "database-service-address");
    
    // Save some data in the database - this time using the proxy
    service.save("mycollection", new JsonObject().putString("name", "tim"), res2 -> {
        if (res2.succeeded()) {
            // done
        }
    });                
