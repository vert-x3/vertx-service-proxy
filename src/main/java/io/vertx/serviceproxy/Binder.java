package io.vertx.serviceproxy;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;

import java.util.function.Function;

/**
 * A binder for Service Proxies which state can be reused during the binder lifecycle.
 *
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
@VertxGen
public interface Binder {

  static Binder create(Vertx vertx) {
    return new ServiceBinder(vertx);
  }

  /**
   * Set the address to use on the subsequent proxy creations or service registrations.
   *
   * @param address an eventbus address
   * @return self
   */
  @Fluent
  Binder setAddress(String address) ;

  /**
   * Set if the services to create are a top level services.
   *
   * @param topLevel true for top level (default: true)
   * @return self
   */
  @Fluent
  Binder setTopLevel(boolean topLevel);

  /**
   * Set the default timeout in seconds while waiting for a reply.
   *
   * @param timeoutSeconds the default timeout (default: 5 minutes)
   * @return self
   */
  @Fluent
  Binder setTimeoutSeconds(long timeoutSeconds);

  @GenIgnore
  Binder addInterceptor(Function<Message<JsonObject>, Future<Message<JsonObject>>> interceptor);

  /**
   * Registers a service on the event bus.
   *
   * @param clazz   the service class (interface)
   * @param service the service object
   * @param <T>     the type of the service interface
   * @return the consumer used to unregister the service
   */
  <T> MessageConsumer<JsonObject> register(Class<T> clazz, T service);

  /**
   * Registers a local service on the event bus.
   * The registration will not be propagated to other nodes in the cluster.
   *
   * @param clazz   the service class (interface)
   * @param service the service object
   * @param <T>     the type of the service interface
   * @return the consumer used to unregister the service
   */
  <T> MessageConsumer<JsonObject> registerLocal(Class<T> clazz, T service);

  /**
   * Unregisters a published service.
   *
   * @param consumer the consumer returned by {@link #register(Class, Object)}.
   */
  void unregister(MessageConsumer<JsonObject> consumer);

}

