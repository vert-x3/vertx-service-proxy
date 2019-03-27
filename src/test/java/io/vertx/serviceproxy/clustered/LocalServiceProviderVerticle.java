package io.vertx.serviceproxy.clustered;

import io.vertx.core.AbstractVerticle;
import io.vertx.serviceproxy.ServiceBinder;

/**
 * @author <a href="https://github.com/michalboska">Michal Boska</a>
 */
public class LocalServiceProviderVerticle extends AbstractVerticle {

  @Override
  public void start() throws Exception {
    new ServiceBinder(vertx).setAddress("my.local.service").registerLocal(Service.class, new ServiceProvider());
  }
}
