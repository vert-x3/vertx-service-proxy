package io.vertx.serviceproxy.clustered;

import io.vertx.core.AbstractVerticle;
import io.vertx.serviceproxy.ProxyHelper;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class ServiceProviderVerticle extends AbstractVerticle {


  @Override
  public void start() throws Exception {
    ProxyHelper.registerService(Service.class, vertx, new ServiceProvider(), "my.service");
  }
}
