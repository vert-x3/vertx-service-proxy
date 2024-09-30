package io.vertx.serviceproxy.tests.clustered;

import io.vertx.core.AbstractVerticle;
import io.vertx.serviceproxy.ServiceBinder;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class ServiceProviderVerticle extends AbstractVerticle {


  @Override
  public void start() throws Exception {
    new ServiceBinder(vertx).setAddress("my.service").register(Service.class, new ServiceProvider());
  }
}
