# Service Proxies

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
          return ProxyHelper.createProxy(SomeDatabaseService.class, vertx, address);
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

You can also combine `@ProxyGen` with language API code generation (`@VertxGen`) in order to create service stubs
in any of the languages supported by Vert.x - this means you can write your service once in Java and interact with it
through an idiomatic other language API irrespective of whether the service lives locally or is somewhere else on
the eventbus entirely.

## Convention for invoking services over the eventbus

Service Proxies assume that event bus messages follow a certain format so they can be used to invoke services.

Of course, you don't *have to* use client proxies to access remote service if you don't want to. It's perfectly acceptable
to interact with them by just sending messages over the event bus.

In order for services to be interacted with a consistent way the following message formats *should be used* for any
Vert.x services.

The format is very simple:

There should be a header called `action` which gives the name of the action to perform.

The body of the message should be a `JsonObject`, there should be one field in the object for each argument needed by the action.

For example to invoke an action called `save` which expects a String collection and a JsonObject document:

    Headers:
    
        "action": "save"
    Body:
    
        {
            "collection", "mycollection",
            "document", {
                "name": "tim"
            }
        }
        
The above convention should be used whether or not service proxies are used to create services, as it allows services
to be interacted with consistently.

In the case where service proxies are used the "action" value should map to the name of an action method in the 
service interface and each `[key, value]` in the body should map to a `[arg_name, arg_value]` in the action method.

Asynchronous return values are sent as replies over the event bus as messages of the return value type.

### Restrictions for service methods

There are restrictions on the types and return values that can be used in a service method so that these are easy to
marshall over event bus messages and so they can be used asynchronously. They are:

* Return values must be `void` or the method must be marked as `Fluent` and return a reference to itself. This is because
methods must not block and it's not possible to return a result immediately without blocking if the service is remote.
* If a return value is required a parameter of type `Handler<AsyncResult<R>>` must be provided. This will be invoked
asynchronously with the result when it is ready. This must be the last parameter in the list of parameters.
* The type `R` above includes any primitive type (or boxed equivalent), `String`, `JsonObject`, `JsonArray` or any enum type,
or any `List` containing the above types.
* Other than the return handler valid parameter types include any primitive type (or boxed equivalent), `String`, `JsonObject`,
 `JsonArray` or any enum type.
* There must be no overloaded service methods. (I.e. more than one with the same name)