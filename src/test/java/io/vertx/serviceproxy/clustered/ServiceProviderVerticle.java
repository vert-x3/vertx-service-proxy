package io.vertx.serviceproxy.clustered;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ProxyHelper;
import io.vertx.serviceproxy.testmodel.TestService;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class ServiceProviderVerticle extends AbstractVerticle {


  @Override
  public void start() throws Exception {
    ProxyHelper.registerService(Service.class, vertx, new ServiceProvider(), "my.service");
  }
}
