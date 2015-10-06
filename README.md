# Service Proxies

[![Build Status](https://vertx.ci.cloudbees.com/buildStatus/icon?job=vert.x3-service-proxy)](https://vertx.ci.cloudbees.com/view/vert.x-3/job/vert.x3-service-proxy/)

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

You can also combine `@ProxyGen` with language API code generation (`@VertxGen`) in order to create service stubs
in any of the languages supported by Vert.x - this means you can write your service once in Java and interact with it
through an idiomatic other language API irrespective of whether the service lives locally or is somewhere else on
the eventbus entirely.

Proxy service methods can also asynchronously return references to other proxy services. In other words proxies can
 be factories for other proxies. This is useful, for example, if you want to
return a connection interface, e.g.

    @ProxyGen
    public interface SomeDatabaseService {
    
        // A couple of factory methods to create an instance and a proxy
    
        static SomeDatabaseService create(Vertx vertx) {
           return new SomeDatabaseServiceImpl(vertx);
        }
        
        static SomeDatabaseService createProxy(Vertx vertx, String address) {
          return new SomeDatabaseServiceVertxEBProxy(vertx, address);
        }
        
        // Create a connection
    
        void createConnection(String shoeSize, Handler<AsyncResult<MyDatabaseConnection>> resultHandler);
    }
    
Where:

    @ProxyGen
    @VertxGen
    public interface MyDatabaseConnection {
    
        void insert(JsonObject someData);
        
        void commit(Handler<AsyncResult<Void>> resultHandler);
        
        @ProxyClose
        void close();
    }
    
You can also declare that a particular method unregisters the proxy by annotating it with the `@ProxyClose` annotation.    
        
## Proxy creation

Service interface must define a factory method named `createProxy`. This method returns an instance of the generated 
proxy.

### Java proxy class

A Java proxy class is generated during the compilation and is named as follows: `service_interface_simple_name + 
VertxEBProxy`.

So for instance, if your interface is named `MyService`, the proxy class is named `MyServiceVertxEBProxy`. 

To generate this class you have to launch an initial compilation of the source.

Alternatively, you can create the proxy instance using `ProxyHelper`, but this is **not the recommended way**:

````
static SomeDatabaseService createProxy(Vertx vertx, String address) {
    return ProxyHelper.createProxy(SomeDatabaseService.class, vertx, address);
}
````

### JS proxy client

A JS proxy module is generated during the compilation and is named as follows: `module_name-js/server-interface_simple_name` + `-proxy.js`

So for instance, if your interface is named `MyService`, the proxy module is named `my_service-proxy.js`.

The generated proxy is a *client* proxy and should be used in a remote client (i.e *not* in Vert.x) using an event bus
bridge. At the moment clients work with the _vertx-web_ event bus bridge `vertx-eventbus.js` and can be used in Web browsers
and Node.JS. 

To generate this class you have to launch an initial compilation of the source.

The generated proxy is a JavaScript module compatible with CommonJS, AMD and Webpack. The proxy then just needs to
  instantiated with the EventBus bridge and the service EventBus address:

````
<script src="http://cdn.sockjs.org/sockjs-0.3.4.min.js"></script>
<script src="vertx-eventbus.js"></script>
<script>
  var eb = new EventBus('http://localhost:8080/eventbus');
  eb.onopen = function() {
    var SomeDatabaseService = require('vertx-database-js/some_database_service-proxy.js');
    var someDatabaseService = new SomeDatabaseService(eb, 'someaddress');
  };
</script>
````

## Convention for invoking services over the eventbus

Service Proxies assume that event bus messages follow a certain format so they can be used to invoke services.

Of course, you don't *have to* use client proxies to access remote service if you don't want to. It's perfectly acceptable
to interact with them by just sending messages over the event bus.

In order for services to be interacted with a consistent way the following message formats *must be used* for any
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

For return values the service should use the `message.reply(...)` method to send back a return value - this can be of
any type supported by the event bus. To signal a failure the method `message.fail(...)` should be used.

If you are using service proxies the generated code will handle this for you automatically.

### Restrictions for service methods

There are restrictions on the types and return values that can be used in a service method so that these are easy to
marshall over event bus messages and so they can be used asynchronously. They are:

#### Return types

Must be one of

* `void`
* `@Fluent` and return reference to the service

This is because methods must not block and it's not possible to return a result immediately without blocking if the service is remote.

#### Parameter types

Let `J` = `JsonObject | JsonArray`
Let `B` = Any primitive type or boxed primitive type

Parameters can be any of:

* `J`
* `B`
* `List<J>`
* `List<B>`
* `Set<J>`
* `Set<B>`
* `Map<String, J>`
* `Map<String, B>`
* Any Enum type
* Any `@DataObject` class

If an asynchronous result is required a final parameter of type `Handler<AsyncResult<R>>` can be provided.

`R` can be any of:

* `J`
* `B`
* `List<J>`
* `List<B>`
* `Set<J>`
* `Set<B>`
* `Map<String, J>`
* `Map<String, B>`
* Any `@DataObject` class

#### Overloaded methods

There must be no overloaded service methods. (I.e. more than one with the same name)
