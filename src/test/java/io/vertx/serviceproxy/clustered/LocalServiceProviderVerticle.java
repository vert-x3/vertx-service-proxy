package io.vertx.serviceproxy.clustered;

import io.vertx.core.AbstractVerticle;
import io.vertx.serviceproxy.ProxyHelper;

/**
 * @author <a href="https://github.com/michalboska">Michal Boska</a>
 */
public class LocalServiceProviderVerticle extends AbstractVerticle {


  @Override
  public void start() throws Exception {
    ProxyHelper.registerLocalService(Service.class, vertx, new ServiceProvider(), "my.local.service");
  }
}
